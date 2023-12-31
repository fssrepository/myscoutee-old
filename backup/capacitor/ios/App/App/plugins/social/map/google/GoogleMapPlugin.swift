//
//  GoogleMapPlugin.swift
//  App
//
//  Created by zetor on 2018. 07. 03..
//

import Foundation
import Capacitor
import GoogleMaps

@objc(GoogleMapPlugin)
public class GoogleMapPlugin: CAPPlugin {
    
    public override func load() {
    }
    
    @objc func show(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            let webView = self.bridge.viewController.view;
            let window = webView!.window;
            
            let statusbarHeight = UIApplication.shared.statusBarFrame.height;
            
            let boundObj = call.getObject("bound");
            let zoomScale = webView!.scrollView.zoomScale;
            var bound = ConvertUtils.convert(bound: boundObj as? [String:Double], zoomScale: zoomScale, deviation : statusbarHeight);

            if bound == nil {
               bound = webView!.frame;
            }
            
            let mapView = GMSMapView.map(withFrame: bound!, camera: GMSCameraPosition.camera(withLatitude: 51.050657, longitude: 10.649514, zoom: 5.5))
            window!.addSubview(mapView);
        }
    }
}
