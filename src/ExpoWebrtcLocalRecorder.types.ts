import type { StyleProp, ViewStyle } from 'react-native';
import type { MediaStreamTrack } from 'react-native-webrtc';

export type OnLoadEventPayload = {
  url: string;
};

export type ExpoWebrtcLocalRecorderModuleEvents = {
  onChange: (params: ChangeEventPayload) => void;
};

export type ChangeEventPayload = {
  value: string;
};

export type ExpoWebrtcLocalRecorderViewProps = {
  url: string;
  onLoad: (event: { nativeEvent: OnLoadEventPayload }) => void;
  style?: StyleProp<ViewStyle>;
};

// --- Native module typings ---

export interface ExpoWebrtcLocalRecorderModule {
  /**
   * Start recording. Optionally pass a file path.
   */
  startRecording(options?: { path?: string }): Promise<void>;

  /**
   * Stop recording and return the recorded file path.
   */
  stopRecording(): Promise<{ path: string }>;

  /**
   * Check if recording is currently active.
   */
  isRecording(): Promise<boolean>;

  /**
   * Register a remote WebRTC track for recording.
   */
  registerRemoteTrack(track: MediaStreamTrack): void;

  /**
   * Unregister a remote WebRTC track.
   */
  unregisterRemoteTrack(track: MediaStreamTrack): void;
}