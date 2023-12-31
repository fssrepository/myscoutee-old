//
//  MQTTPlugin.m
//  App
//
//  Created by zetor on 2018. 07. 04..
//

#import <Foundation/Foundation.h>
#import <Capacitor/Capacitor.h>

CAP_PLUGIN(MQTTPlugin, "MQTTPlugin",
           CAP_PLUGIN_METHOD(subscribe, CAPPluginReturnPromise);
           )
