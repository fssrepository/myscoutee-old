import { Injectable } from '@angular/core';
import mqtt from 'mqtt';

@Injectable({
  providedIn: 'root',
})
export class MqttService {
  private client: mqtt.MqttClient;

  private handlers: Array<any>;

  private topicsDeleted: Array<string>;

  private isConnected: boolean;

  constructor() {
    this.handlers = new Array();
    this.topicsDeleted = new Array();
    this.isConnected = false;
  }

  init(mqttInfo) {
    if (!mqttInfo
      || !mqttInfo.url
      || !mqttInfo.options
      || !mqttInfo.options.username
      || !mqttInfo.options.password) {
      console.log("mqtt parameter missing!");
      return;
    }

    this.client = mqtt.connect(mqttInfo.url, mqttInfo.options);

    this.client.on("error", (error) => {
      console.log("MQTT: " + error);
    });

    this.client.on("connect", (connack) => {
      console.log("connected");
      this.isConnected = true;

      for (const topic in this.handlers) {
        this.client.subscribe(topic);
        console.log("subscibed to: " + topic);
      }

      for (const topic of this.topicsDeleted) {
        this.client.unsubscribe(topic);
      }

      this.topicsDeleted = [];
    });

    this.client.on("message", (topic, payload) => {
      if (this.handlers && this.handlers[topic]) {
        let str = this.convert(payload);
        this.handlers[topic](str);
      }
    });
  }

  register(topic, callback) {
    this.handlers[topic] = callback;
    if (this.isConnected) {
      this.client.subscribe(topic);
    }
  }

  unregister(topic) {
    delete this.handlers[topic];
    this.topicsDeleted.push(topic);
    if (this.isConnected) {
      this.client.unsubscribe(topic);
    }
  }

  publish(topic, msg, callback?) {
    if (this.isConnected) {
      this.client.publish(topic, msg, callback)
    }
  }

  private convert(array) {
    var out, i, len, c;
    var char2, char3;

    out = "";
    len = array.length;
    i = 0;
    while (i < len) {
      c = array[i++];
      switch (c >> 4) {
        case 0: case 1: case 2: case 3: case 4: case 5: case 6: case 7:
          // 0xxxxxxx
          out += String.fromCharCode(c);
          break;
        case 12: case 13:
          // 110x xxxx   10xx xxxx
          char2 = array[i++];
          out += String.fromCharCode(((c & 0x1F) << 6) | (char2 & 0x3F));
          break;
        case 14:
          // 1110 xxxx  10xx xxxx  10xx xxxx
          char2 = array[i++];
          char3 = array[i++];
          out += String.fromCharCode(((c & 0x0F) << 12) |
            ((char2 & 0x3F) << 6) |
            ((char3 & 0x3F) << 0));
          break;
      }
    }

    return out;
  }
}