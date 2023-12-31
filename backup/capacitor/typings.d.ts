/* SystemJS module definition */
declare var module: NodeModule;
interface NodeModule {
  id: string;
}

//capacitor plugin
export interface PermissionsRequestResult {
  results: any[];
}

export interface PluginListenerHandle {
  remove: () => void;
}

export interface Plugin {
  addListener?: (eventName: string, listenerFunc: Function) => PluginListenerHandle;
  removeListener?: (eventName: string, listenerFunc: Function) => void;
  requestPermissions?: () => Promise<PermissionsRequestResult>;
}

declare global {
  interface PluginRegistry {
    EchoPlugin?: EchoPlugin;
    MQTTPlugin?: MQTTPlugin;
    OverlayPlugin?: OverlayPlugin;
    GoogleMapPlugin?: GoogleMapPlugin;
    MLVisionPlugin?: MLVisionPlugin;
    WebRTCPlugin?: WebRTCPlugin;
    FCMPlugin?: FCMPlugin;
  }
}

export interface EchoPlugin extends Plugin {
  echo(options: { value : string }): Promise<{value}>;
}

export interface WebRTCPlugin extends Plugin {

}

export interface MLVisionPlugin extends Plugin {
  show(options: { bound: any, type : string }): Promise<void>;
  hide(options: { isDestroy: boolean }): Promise<void>;
  verify(options: { data: string }): Promise<void>;
}

export interface MQTTPlugin extends Plugin {
  publish(options: { topic: string, msg: string }): Promise<void>;
  subscribe(options: { topic: string }): Promise<void>;
  unsubscribe(options: { topic: string }): Promise<void>;
}

export interface OverlayPlugin extends Plugin {
  show(options: { overlays: Array<any> }): Promise<void>;
  hide(options: { isDestroy: boolean }): Promise<void>;
}

export interface GoogleMapPlugin extends Plugin {
  show(options: { bound: any }): Promise<void>;
  hide(options: { isDestroy: boolean }): Promise<void>;
  addMarkers(options: { markers: Array<any> }): Promise<void>;
}

//addListener - onTokenRefresh / onNotification
export interface FCMPlugin extends Plugin {
  subscribe(options: { topic: any }): Promise<void>;
  unsubscribe(options: { topic: any }): Promise<void>;
  getToken() : Promise<string>; //it might not be needed
}

declare const Plugins: PluginRegistry;
