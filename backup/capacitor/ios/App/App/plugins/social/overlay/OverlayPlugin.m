//
//  OverlayPlugin.m
//  App
//
//  Created by zetor on 2018. 07. 03..
//

#import <Foundation/Foundation.h>
#import <Capacitor/Capacitor.h>

CAP_PLUGIN(OverlayPlugin, "OverlayPlugin",
           CAP_PLUGIN_METHOD(show, CAPPluginReturnPromise);
           )
