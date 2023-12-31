import { AfterViewInit, Component, OnInit, Renderer2 } from '@angular/core';
import { Plugins } from '@capacitor/core';
import { info } from './global.var';

/*Plugins.EchoPlugin.addListener("SOMETHING", this.echo);

1) server return a value
JSObject ret = new JSObject();
ret.put("value", value);
call.success(ret);

Plugins.EchoPlugin.echo({ value: "aaa" }).then((value) => {
  console.log(value);
});

typing.d.ts
echo(options: { value: string }): Promise<{value: string}>;

MainActivity...:
this.init(savedInstanceState, new ArrayList<Class<? extends Plugin>>() {{
            add(EchoPlugin.class);
        }});
*/

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html'
})
export class AppComponent implements OnInit, AfterViewInit {
  title = 'app';

  constructor(private renderer: Renderer2) {
  }

  public messageReceived(payload: any) {
    console.log(payload);
  }

  updateIndicator() {
    console.log(navigator.onLine);
  }

  ngAfterViewInit() {
    //this.renderer.removeClass(document.body, "preload");
  }

  ngOnInit() {
    console.log("hide");
    Plugins.Device.getInfo().then((deviceInfo) => {
      info.isPwa = deviceInfo.platform === "pwa" ? true : false;
      if (!info.isPwa) {
        Plugins.SplashScreen.hide();

        Plugins.Network.addListener('networkStatusChange', (status) => {
          console.log("Network status changed", status);
        });

      } else {
        window.addEventListener('online', this.updateIndicator);
        window.addEventListener('offline', this.updateIndicator);
        this.updateIndicator();
      }

      Plugins.Storage.set({
        key: "user",
        value: JSON.stringify({
          name: "aladar"
        })
      }).then(() => {
        console.log("saved");
      });

      Plugins.Storage.get({ key: "user" }).then((value) => {
        console.log(value);
      });
    });

    /*
    Geolocation plugin change android studio
    private void startWatch(PluginCall call) {
      String provider = getBestProviderForCall(call);

      int timeout = call.getInt("timeout", 30000);
      int maximumAge = call.getInt("maximumAge", 0);

      locationManager.requestLocationUpdates(provider, timeout, 0, locationListener);
      watchingCalls.put(call.getCallbackId(), call);
    }
    */
    //SERVICE_BRIDGE.register("position", 0, )
    /*Plugins.Geolocation.watchPosition({ timeout: 300000 }, (loc, err) => {
      if (!MSG_BRIDGE.isSubscribed("position")) {
        MSG_BRIDGE.subscribe("position", [Channel.DATA], (channel, msg) => {
          switch (channel) {
            case Channel.DATA:
              console.log(msg);
            break;
          }
        });
      }
      //SERVICE_BRIDGE.request("position", );
      console.log(loc.coords);
      //send location every 5 minutes 60 sec * 5 = 300 (timeout value)
      //get back popup or just update if it's not differing too much
    });*/

    /*Plugins.EchoPlugin.addListener("SOMETHING", this.echo);
    Plugins.EchoPlugin.echo({ value: "aaa" }).then((value) => {
      console.log(value);
    });*/

    /*************** FCM ******************/
    /*Plugins.FCMPlugin.addListener("FCM_LISTENER", this.messageReceived);
    Plugins.FCMPlugin.getToken().then((value) => {
      console.log(value); //register device token to my server, if i need to send messages individualy, but mostly i'm sending to a topic
    });
    Plugins.FCMPlugin.subscribe({topic : "alert"});*/

    /*************** MQTT ******************/
    /*let mqttInfo = {url : 'ws://localhost', options : { port: 8083, username: "myscoutee", password: "Mixtar1Drang2!" }};
    MQTTWrapper.init(mqttInfo);*/
  }
}
