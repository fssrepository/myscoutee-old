package com.raxim.capacitor.plugins.social.fcm;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import static com.getcapacitor.Bridge.TAG;

public class FCMMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());

            Intent messageIntent = new Intent(FCM_ACTION.FCM_MESSAGE.name());
            messageIntent.putExtra("type", FCM_TYPE.MESSAGE);
            messageIntent.putExtra("value", remoteMessage.getNotification().getBody());
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(messageIntent);
        }
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);

        Intent messageIntent = new Intent(FCM_ACTION.FCM_MESSAGE.name());
        messageIntent.putExtra("type", FCM_TYPE.TOKEN);
        messageIntent.putExtra("value", token);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(messageIntent);
    }
}
