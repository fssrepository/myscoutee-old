package com.raxim.capacitor.plugins.social.fcm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;

import java.nio.charset.Charset;

@NativePlugin()
public class FCMPlugin extends Plugin {

    private static final String FCM_LISTENER = "FCM_LISTENER";

    private class MessageReceiver extends BroadcastReceiver {
        private MessageReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            JSObject jsonObject = new JSObject();

            String value = intent.getStringExtra("value");
            jsonObject.put("value", value);

            String typeStr = intent.getStringExtra("type");
            jsonObject.put("type", typeStr);

            Log.d(FCM_LISTENER, jsonObject.toString());
            notifyListeners(FCM_LISTENER, jsonObject);
        }
    }

    public void load() {
        IntentFilter statusIntentFilter = new IntentFilter(
                FCM_ACTION.FCM_MESSAGE.name());
        FCMPlugin.MessageReceiver messageReceiver = new FCMPlugin.MessageReceiver();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                messageReceiver, statusIntentFilter);
    }

    @PluginMethod()
    public void getToken(final PluginCall call) throws JSONException {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String token = FirebaseInstanceId.getInstance().getToken();

                JSObject ret = new JSObject();
                ret.put("token", token);
                call.success(ret);
            }
        });
    }

    @PluginMethod()
    public void subscribe(final PluginCall call) throws JSONException {
        final String topic = call.getString("topic");

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                FirebaseMessaging.getInstance().subscribeToTopic(topic).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            call.success();
                        } else {
                            call.reject("Not able to subscribe!");
                        }
                    }
                });
            }
        });
    }

    @PluginMethod()
    public void unsubscribe(final PluginCall call) throws JSONException {
        final String topic = call.getString("topic");

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                FirebaseMessaging.getInstance().unsubscribeFromTopic(topic).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            call.success();
                        } else {
                            call.reject("Not able to subscribe!");
                        }
                    }
                });
            }
        });
    }
}
