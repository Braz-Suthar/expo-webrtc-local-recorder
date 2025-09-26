
# webrtc-local-recorder

**Expo / React Native module for recording local and remote WebRTC audio streams on Android.**

`webrtc-local-recorder` allows you to record:

- **Local microphone audio**  
- **Remote WebRTC audio tracks** from multiple peers  
- Handles muted/active tracks automatically  
- Saves recordings in WAV format  

> **Note:** Currently, this package is **Android-only**. iOS support is planned for future releases.

---

## Features

- Record local device audio to WAV files  
- Register/unregister remote WebRTC audio tracks  
- Automatically skips inactive or muted tracks  
- Supports multiple remote tracks simultaneously  
- TypeScript definitions included  
- Optimized for **Expo-managed** and **bare React Native** projects  

---

## Installation

### 1. Install the package

```bash
npm install webrtc-local-recorder
# or
yarn add webrtc-local-recorder
```

### 2. Configure Android Permissions

**AndroidManifest.xml**

```xml
<uses-permission android:name="android.permission.RECORD_AUDIO" />
```

**Expo `app.json`**

```json
{
  "expo": {
    "android": {
      "permissions": ["RECORD_AUDIO"]
    }
  }
}
```

> This ensures your app can request microphone access at runtime.

---

## Usage

### 1. Import module

```ts
import {
  startRecording,
  stopRecording,
  isRecording,
  registerRemoteTrack,
  unregisterRemoteTrack
} from 'webrtc-local-recorder';

import { MediaStreamTrack } from 'react-native-webrtc';
```

---

### 2. Record local audio

```ts
// Start recording (optional path)
await startRecording({ path: 'my_recording.wav' });

// Check recording status
const recording = await isRecording();
console.log('Is recording:', recording);

// Stop recording
const result = await stopRecording();
console.log('Recording saved at:', result.path);
```

> If no path is provided, the module will create a default file in the app’s internal storage.

---

### 3. Register remote WebRTC tracks

```ts
const remoteTrack: MediaStreamTrack = remoteStream.getAudioTracks()[0];

// Register the track to include it in the recording
registerRemoteTrack(remoteTrack);

// Unregister when track is no longer needed
unregisterRemoteTrack(remoteTrack);
```

> Muted tracks are skipped automatically. Recording resumes when the track becomes active.

---

## API

| Function | Description |
|----------|-------------|
| `startRecording(options?: { path?: string }): Promise<void>` | Start recording audio. Optionally provide a file path. |
| `stopRecording(): Promise<{ path: string }>` | Stop recording and return the saved file path. |
| `isRecording(): Promise<boolean>` | Check if recording is currently active. |
| `registerRemoteTrack(track: MediaStreamTrack): void` | Add a remote WebRTC track for recording. |
| `unregisterRemoteTrack(track: MediaStreamTrack): void` | Remove a remote track from recording. |

---

## TypeScript Support

Type definitions are included in `ExpoWebrtcLocalRecorder.types.ts`.

```ts
import type { ExpoWebrtcLocalRecorderModule } from 'webrtc-local-recorder';

const recorder: ExpoWebrtcLocalRecorderModule;
```

---

## Examples

### Recording multiple remote tracks

```ts
remoteStreams.forEach(stream => {
  stream.getAudioTracks().forEach(track => registerRemoteTrack(track));
});

await startRecording();
...
const result = await stopRecording();
console.log(result.path);
```

### Handling muted tracks automatically

Muted tracks are not written to the recording file. When they become active again, their audio is recorded automatically.

---

## Limitations

- Android-only (iOS not supported yet)  
- Each remote track consumes memory; registering too many tracks simultaneously may affect performance  
- WAV format only (no mp3/aac conversion included)  

---

## Contributing

Contributions are welcome! Please submit pull requests or report issues via GitHub.

---

## License

MIT © Braz Suthar
