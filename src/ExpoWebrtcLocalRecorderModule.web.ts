import { registerWebModule, NativeModule } from 'expo';

import { ExpoWebrtcLocalRecorderModuleEvents } from './ExpoWebrtcLocalRecorder.types';

class ExpoWebrtcLocalRecorderModule extends NativeModule<ExpoWebrtcLocalRecorderModuleEvents> {
  PI = Math.PI;
  async setValueAsync(value: string): Promise<void> {
    this.emit('onChange', { value });
  }
  hello() {
    return 'Hello world! 👋';
  }
}

export default registerWebModule(ExpoWebrtcLocalRecorderModule, 'ExpoWebrtcLocalRecorderModule');
