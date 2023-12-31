//
//  EchoPlugin.m
//  App
//
//  Created by zetor on 2018. 03. 22..
//

#import <Foundation/Foundation.h>
#import <Capacitor/Capacitor.h>

CAP_PLUGIN(EchoPlugin, "EchoPlugin",
           CAP_PLUGIN_METHOD(echo, CAPPluginReturnPromise);
           )
