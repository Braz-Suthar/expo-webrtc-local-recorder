import { requireNativeView } from 'expo';
import * as React from 'react';

import { ExpoWebrtcLocalRecorderViewProps } from './ExpoWebrtcLocalRecorder.types';

const NativeView: React.ComponentType<ExpoWebrtcLocalRecorderViewProps> =
  requireNativeView('ExpoWebrtcLocalRecorder');

export default function ExpoWebrtcLocalRecorderView(props: ExpoWebrtcLocalRecorderViewProps) {
  return <NativeView {...props} />;
}
