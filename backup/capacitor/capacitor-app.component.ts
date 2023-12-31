import { Component, OnInit, ElementRef, ViewChild, ViewChildren, QueryList, AfterViewInit, ViewContainerRef } from '@angular/core';
import { Plugins } from '@capacitor/core';

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
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit, AfterViewInit {
  title = 'app';

  @ViewChild("map") mapRef: ElementRef;
  @ViewChild("overlay") overlayRefs: ElementRef;

  echo(msg) {
    console.log(msg);
  }

  messageArrived(msg) {
    console.log(msg);
  }

  markerClicked(marker) {
    console.log(marker);
  }

  private toJSON(clientRect: ClientRect) {
    return { left: clientRect.left, right: clientRect.right, top: clientRect.top, bottom: clientRect.bottom };
  }

  ngOnInit() {

  }

  rand(min, max) {
    return Math.floor(Math.random() * (max - min + 1) + min);
  }

  randLocation() {
    let id = "" + Math.round(Math.random() * Math.pow(10, 7));
    let lat = this.rand(4740, 4750) / 100.0;
    let lng = this.rand(1900, 1930) / 100.0;
    return { id: id, lat: lat, lng: lng };
  }

  ngAfterViewInit() {
    Plugins.SplashScreen.hide();

    /*Plugins.EchoPlugin.addListener("ECHO_LISTER", this.echo);
    Plugins.EchoPlugin.echo({value : "proba"});*/
    
    //Plugins.MQTTPlugin.addListener("MQTT_LISTENER", this.messageArrived);

    console.log("afterViewInit");

    let mapRect = this.mapRef.nativeElement.getBoundingClientRect();

    let overlayRects = [];
    let children = this.overlayRefs.nativeElement.children;
    for (let i = 0; i < children.length; i++) {
      let overlayRect = children[i].getBoundingClientRect();
      overlayRects.push(this.toJSON(overlayRect));
    }

    /*Plugins.GoogleMapPlugin.addListener("MARKER_CLICK_LISTENER", this.markerClicked);
    Plugins.GoogleMapPlugin.show({ bound: this.toJSON(mapRect) }).then(() => {
      let markers = [];
      for (let i = 0; i < 30; i++) {
        markers.push(this.randLocation());
      }

      Plugins.GoogleMapPlugin.addMarkers({ markers: markers });
    });*/

    Plugins.MLVisionPlugin.show({ bound: this.toJSON(mapRect), type: "face" });

    Plugins.OverlayPlugin.show({ overlays: overlayRects });
  }
}
