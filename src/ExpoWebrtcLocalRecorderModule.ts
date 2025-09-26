// WebrtcLocalRecorder.ts
import { NativeModulesProxy } from 'expo-modules-core';
import type { MediaStreamTrack } from 'react-native-webrtc';

type Options = {
  path?: string;
};

type RecordingResult = {
  path: string;
};

const { WebrtcLocalRecorder } = NativeModulesProxy;

export default class WebrtcLocalRecorderModule {
  static async startRecording(options?: Options): Promise<void> {
    await WebrtcLocalRecorder.startRecording(options || {});
  }

  static async stopRecording(): Promise<RecordingResult> {
    return await WebrtcLocalRecorder.stopRecording();
  }

  static async isRecording(): Promise<boolean> {
    return await WebrtcLocalRecorder.isRecording();
  }

  /**
   * Registers a remote WebRTC audio track to capture its audio.
   * @param track The MediaStreamTrack from react-native-webrtc
   */
  static async registerRemoteTrack(track: MediaStreamTrack): Promise<void> {
    // The native side expects an AudioTrack object from the WebRTC SDK
    // In react-native-webrtc, track._nativeTrack is the underlying native track reference
    if (!track || !(track as any)._nativeTrack) {
      console.warn('Track does not have native reference.');
      return;
    }

    await WebrtcLocalRecorder.registerRemoteTrack((track as any)._nativeTrack);
  }

  static async unregisterRemoteTrack(track: MediaStreamTrack): Promise<void> {
    if (!track || !(track as any)._nativeTrack) return;
    await WebrtcLocalRecorder.unregisterRemoteTrack((track as any)._nativeTrack);
  }
}