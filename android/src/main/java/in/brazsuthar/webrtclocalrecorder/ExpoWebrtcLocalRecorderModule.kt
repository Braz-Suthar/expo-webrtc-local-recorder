package in.brazsuthar.webrtclocalrecorder

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.core.content.ContextCompat
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import org.webrtc.AudioTrack
import org.webrtc.AudioSink
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

class WebrtcLocalRecorderModule : Module() {

    private val isRecording = AtomicBoolean(false)
    private val outputPath = AtomicReference<String?>(null)
    private var audioRecord: AudioRecord? = null
    private var recordingThread: Thread? = null
    private var fileOutputStream: FileOutputStream? = null

    // Map of remote AudioTrack -> latest PCM buffer
    private val remoteBuffers = ConcurrentHashMap<AudioTrack, ByteArray>()

    override fun definition() = ModuleDefinition {
        Name("WebrtcLocalRecorder")

        AsyncFunction("startRecording") { options: Map<String, Any?> ->
            if (isRecording.get()) throw Exception("ALREADY_RECORDING")
            if (!checkPermissions()) throw Exception("PERMISSION_DENIED")

            val path = (options["path"] as? String) ?: getDefaultOutputPath()
            outputPath.set(path)

            val file = File(path)
            file.parentFile?.mkdirs()
            fileOutputStream = FileOutputStream(file)

            writeWavHeader(fileOutputStream!!, 48000, 16, 1)

            val bufferSize = AudioRecord.getMinBufferSize(
                48000,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                48000,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufferSize
            )

            if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                throw Exception("AUDIO_INIT_ERROR")
            }

            isRecording.set(true)
            audioRecord?.startRecording()

            recordingThread = Thread {
                val micBuffer = ByteArray(bufferSize)
                while (isRecording.get()) {
                    val micBytes = audioRecord?.read(micBuffer, 0, micBuffer.size) ?: 0

                    // Mix remote audio
                    val mixedBuffer = ByteArray(micBytes)
                    for (i in 0 until micBytes step 2) {
                        // mic sample
                        val micSample = if (i + 1 < micBytes) {
                            ((micBuffer[i].toInt() and 0xFF) or (micBuffer[i + 1].toInt() shl 8))
                        } else 0

                        // sum remote samples
                        var remoteSampleSum = 0
                        for (buf in remoteBuffers.values) {
                            if (i + 1 < buf.size) {
                                remoteSampleSum += ((buf[i].toInt() and 0xFF) or (buf[i + 1].toInt() shl 8))
                            }
                        }

                        // mix
                        var mixedSample = micSample + remoteSampleSum
                        // clamp
                        if (mixedSample > Short.MAX_VALUE) mixedSample = Short.MAX_VALUE.toInt()
                        if (mixedSample < Short.MIN_VALUE) mixedSample = Short.MIN_VALUE.toInt()

                        mixedBuffer[i] = (mixedSample and 0xFF).toByte()
                        mixedBuffer[i + 1] = ((mixedSample shr 8) and 0xFF).toByte()
                    }

                    try {
                        fileOutputStream?.write(mixedBuffer, 0, mixedBuffer.size)
                    } catch (e: IOException) {
                        Log.e("WebrtcLocalRecorder", "Error writing audio data", e)
                    }
                }
            }
            recordingThread?.start()

            null
        }

        AsyncFunction("stopRecording") {
            if (!isRecording.get()) throw Exception("NOT_RECORDING")

            isRecording.set(false)
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null

            recordingThread?.join()
            recordingThread = null

            fileOutputStream?.close()
            fileOutputStream = null

            val path = outputPath.get()
            if (path != null) {
                updateWavHeader(path)
                return@AsyncFunction mapOf("path" to path)
            } else {
                throw Exception("NO_OUTPUT_PATH")
            }
        }

        Function("isRecording") {
            return@Function isRecording.get()
        }

        // Register a remote WebRTC AudioTrack to capture its PCM
        AsyncFunction("registerRemoteTrack") { track: AudioTrack ->
            val sink = object : AudioSink {
                override fun onData(audioData: ShortArray, bitsPerSample: Int, sampleRate: Int, channels: Int, frames: Int) {
                    val buf = ByteArray(frames * 2)
                    var idx = 0
                    for (s in audioData) {
                        buf[idx++] = (s.toInt() and 0xFF).toByte()
                        buf[idx++] = ((s.toInt() shr 8) and 0xFF).toByte()
                    }
                    remoteBuffers[track] = buf
                }
            }
            track.addSink(sink)
        }

        AsyncFunction("unregisterRemoteTrack") { track: AudioTrack ->
            remoteBuffers.remove(track)
            track.removeSink { }
        }
    }

    private fun checkPermissions(): Boolean {
        val context = appContext.reactContext ?: return false
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun getDefaultOutputPath(): String {
        val context = appContext.reactContext ?: throw Exception("No context")
        val timestamp = System.currentTimeMillis()
        val fileName = "webrtc_recording_$timestamp.wav"
        return context.filesDir.absolutePath + "/" + fileName
    }

    private fun writeWavHeader(outputStream: FileOutputStream, sampleRate: Int, bitsPerSample: Int, channels: Int) {
        try {
            val byteRate = sampleRate * channels * bitsPerSample / 8
            val blockAlign = channels * bitsPerSample / 8
            outputStream.write("RIFF".toByteArray())
            outputStream.write(intToByteArray(0))
            outputStream.write("WAVE".toByteArray())
            outputStream.write("fmt ".toByteArray())
            outputStream.write(intToByteArray(16))
            outputStream.write(shortToByteArray(1))
            outputStream.write(shortToByteArray(channels.toShort()))
            outputStream.write(intToByteArray(sampleRate))
            outputStream.write(intToByteArray(byteRate))
            outputStream.write(shortToByteArray(blockAlign.toShort()))
            outputStream.write(shortToByteArray(bitsPerSample.toShort()))
            outputStream.write("data".toByteArray())
            outputStream.write(intToByteArray(0))
        } catch (e: IOException) {}
    }

    private fun updateWavHeader(filePath: String) {
        try {
            val file = File(filePath)
            if (file.exists() && file.length() > 44) {
                val fileSize = file.length().toInt()
                val dataSize = fileSize - 44
                val raf = java.io.RandomAccessFile(file, "rw")
                raf.seek(4)
                raf.write(intToByteArray(fileSize - 8))
                raf.seek(40)
                raf.write(intToByteArray(dataSize))
                raf.close()
            }
        } catch (e: IOException) {}
    }

    private fun intToByteArray(value: Int): ByteArray {
        return byteArrayOf(
            (value and 0xFF).toByte(),
            ((value shr 8) and 0xFF).toByte(),
            ((value shr 16) and 0xFF).toByte(),
            ((value shr 24) and 0xFF).toByte()
        )
    }

    private fun shortToByteArray(value: Short): ByteArray {
        return byteArrayOf(
            (value.toInt() and 0xFF).toByte(),
            ((value.toInt() shr 8) and 0xFF).toByte()
        )
    }
}