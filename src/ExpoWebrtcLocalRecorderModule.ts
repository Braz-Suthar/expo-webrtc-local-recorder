import { NativeModule, requireNativeModule } from 'expo';

import { ExpoWebrtcLocalRecorderModuleEvents } from './ExpoWebrtcLocalRecorder.types';

declare class ExpoWebrtcLocalRecorderModule extends NativeModule<ExpoWebrtcLocalRecorderModuleEvents> {
  PI: number;
  hello(): string;
  setValueAsync(value: string): Promise<void>;
}

// This call loads the native module object from the JSI.
export default requireNativeModule<ExpoWebrtcLocalRecorderModule>('ExpoWebrtcLocalRecorder');
