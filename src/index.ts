// Reexport the native module. On web, it will be resolved to ExpoWebrtcLocalRecorderModule.web.ts
// and on native platforms to ExpoWebrtcLocalRecorderModule.ts
export { default } from './ExpoWebrtcLocalRecorderModule';
export { default as ExpoWebrtcLocalRecorderView } from './ExpoWebrtcLocalRecorderView';
export * from  './ExpoWebrtcLocalRecorder.types';
