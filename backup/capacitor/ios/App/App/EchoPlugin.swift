//
//  EchoPlugin.swift
//  App
//
//  Created by zetor on 2018. 03. 22..
//

import Foundation
import Capacitor

@objc(EchoPlugin)
public class EchoPlugin: CAPPlugin {
    private var timer : Timer!;
    
    public override func load() {
        timer = Timer(timeInterval: 10, repeats: false, block: { (timer : Timer) -> Void in
            self.notifyListeners("ECHO_LISTER", data: ["value":"proba"]);
        });
        //timer = Timer.scheduledTimer(timeInterval: 10, target: self, selector: #selector(handleMyFunction), userInfo: nil, repeats: false)
    }
    
    @objc func handleMyFunction(timer : Timer) -> Void {
        notifyListeners("ECHO_LISTER", data: ["value":"proba"]);
    }
    
    @objc func echo(_ call: CAPPluginCall) {
        let value = call.getString("value") ?? ""
        call.success([
            "value": value
            ])
    }
}
