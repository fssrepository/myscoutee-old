//
//  MQTTPlugin.swift
//  App
//
//  Created by zetor on 2018. 07. 04..
//

import Foundation
import Capacitor

//It might work in development mode, and web-rtc needs to be attached because of VOIP background mode (it won't be allowed without it)
//add background mode Voip manually to Info.plist - XCode UI has no option for that
//PushKit needs to be implemented - MQTT is not an option

@objc(MQTTPlugin)
public class MQTTPlugin: CAPPlugin {
    
    @objc func subscribe(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
        }
    }
}
