//
//  FCMPlugin.m
//  App
//
//  Created by zetor on 2018. 07. 04..
//

#import <Foundation/Foundation.h>
#import <Capacitor/Capacitor.h>

CAP_PLUGIN(FCMPlugin, "FCMPlugin",
           CAP_PLUGIN_METHOD(subscribe, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(unsubscribe, CAPPluginReturnPromise);
           CAP_PLUGIN_METHOD(getToken, CAPPluginReturnPromise);
           )
