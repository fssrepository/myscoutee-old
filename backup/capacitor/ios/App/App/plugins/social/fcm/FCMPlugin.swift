//
//  FCMPlugin.swift
//  App
//
//  Created by zetor on 2018. 07. 04..
//

import Foundation
import Capacitor
import Firebase

@objc(FCMPlugin)
public class FCMPlugin: CAPPlugin {
    
    let FCM_LISTENER = "FCM_LISTENER";
    
    public override func load() {
        //FCMToken is coming from AppDelegate -> Messaging NotificationCenter.default.post
        NotificationCenter.default.addObserver(self, selector: #selector(self.receive(notification:)),
                                               name: Notification.Name("FCMToken"), object: nil)
        NotificationCenter.default.addObserver(self, selector: #selector(self.receive(notification:)),
                                               name: Notification.Name("FCMMessage"), object: nil)
    }
    
    @objc func subscribe(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            let topic = call.getString("topic");
            Messaging.messaging().subscribe(toTopic: topic!) { error in
                call.reject("Cannot subscribe to \(topic as String?) !");
            }
            call.resolve();
        }
    }
    
    @objc func unsubscribe(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            let topic = call.getString("topic");
            Messaging.messaging().unsubscribe(fromTopic: topic!) { error in
                call.reject("Cannot unsubscribe from \(topic as String?) !");
            }
            call.resolve();
        }
    }
    
    @objc func getToken(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            InstanceID.instanceID().instanceID { (result, error) in
                if let error = error {
                    print("Error fetching remote instange ID: \(error)")
                } else if let result = result {
                    print("Remote instance ID token: \(result.token)")
                    call.resolve(["token" : result.token]);
                }
            }
        }
    }
    
    @objc func receive(notification: NSNotification){
        guard let userInfo = notification.userInfo else {return}
        if let fcmToken = userInfo["token"] as? String {
            //self.fcmTokenMessage.text = "Received FCM token: \(fcmToken)"
        }
    }
}
