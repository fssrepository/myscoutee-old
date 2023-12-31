//
//  OverlayPlugin.swift
//  App
//
//  Created by zetor on 2018. 07. 03..
//

import Foundation
import Capacitor

@objc(OverlayPlugin)
public class OverlayPlugin: CAPPlugin {
    
    @objc func show(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            let webView = self.bridge.viewController.view;
            webView!.isOpaque = false
            webView!.backgroundColor = UIColor.clear
            webView!.scrollView.backgroundColor = UIColor.clear
            
            let window = webView!.window;
            
            let overlayView = OverlayView.init(webView!.frame, Array());
            overlayView.isUserInteractionEnabled = true;
            overlayView.addSubview(webView!);
            window!.addSubview(overlayView);
            
            let zoomScale = webView!.scrollView.zoomScale;
            print(zoomScale);
        }
    }
}

class OverlayView: UIView {
    var overlays : Array<CGRect>;
    
    public init(_ frame: CGRect, _ overlays : Array<CGRect>){
        self.overlays = overlays;
        super.init(frame: frame);
    }
    
    public required init?(coder aDecoder: NSCoder){
        self.overlays = Array();
        super.init(coder : aDecoder);
    }
    
    override func hitTest(_ point: CGPoint, with event: UIEvent?) -> UIView? {
        let hitView = super.hitTest(point, with: event);
        //CGRectConstraint to check whether point is in a overlays' rectangle
        return hitView;
    }
}
