import * as React from 'react';

import { ExpoWebrtcLocalRecorderViewProps } from './ExpoWebrtcLocalRecorder.types';

export default function ExpoWebrtcLocalRecorderView(props: ExpoWebrtcLocalRecorderViewProps) {
  return (
    <div>
      <iframe
        style={{ flex: 1 }}
        src={props.url}
        onLoad={() => props.onLoad({ nativeEvent: { url: props.url } })}
      />
    </div>
  );
}
