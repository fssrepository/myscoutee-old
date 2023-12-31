package com.raxim.capacitor.plugins.social.mqtt;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONException;

import java.nio.charset.Charset;
import java.util.Random;

//local service can be used instead of messenger
@NativePlugin()
public class MQTTPlugin extends Plugin {
    private static final String MQTT_LISTENER = "MQTT_LISTENER";

    private boolean mBound = false;
    private final Random mGenerator = new Random();

    private MQTTService mService;

    @Override
    protected void handleOnStop() {
        super.handleOnStop();

        getActivity().unbindService(mConnection);
        mBound = false;
    }

    @Override
    protected void handleOnStart() {
        super.handleOnStart();

        Intent intent = new Intent(getActivity(), MQTTService.class);
        //put extra for the token update
        getActivity().startService(intent);
        getActivity().bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    private class MessageReceiver extends BroadcastReceiver {
        // Prevents instantiation
        private MessageReceiver() {
        }

        // Called when the BroadcastReceiver gets an Intent it's registered to receive
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                byte[] payload = intent.getByteArrayExtra("payload");
                String msgStr = new String(payload, Charset.forName("UTF-8"));
                Log.d(MQTT_LISTENER, msgStr);
                JSObject jsonObject = new JSObject(msgStr);
                notifyListeners(MQTT_LISTENER, jsonObject);
            } catch (JSONException e) {
                Log.e(MQTT_LISTENER, e.getMessage(), e.getCause());
            }
        }
    }

    public void load() {
        IntentFilter statusIntentFilter = new IntentFilter(
                MQTT_ACTION.MESSAGE_RECEIVED.name());
        MessageReceiver messageReceiver = new MessageReceiver();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                messageReceiver, statusIntentFilter);
    }

    @PluginMethod()
    public void publish(PluginCall call) {
        try {
            String topic = call.getString("topic");
            String msg = call.getString("msg");
            mService.publish(topic, msg);
            call.success();
        } catch (MqttException e) {
            e.printStackTrace();
            call.reject(e.getMessage());
        }
    }

    @PluginMethod()
    public void subscribe(PluginCall call) {
        try {
            String topic = call.getString("topic");
            mService.subscribe(topic);
            call.success();
        } catch (MqttException e) {
            e.printStackTrace();
        }
        call.success();
    }

    @PluginMethod()
    public void unsubscribe(PluginCall call) {
        try {
            String topic = call.getString("topic");
            mService.unsubscribe(topic);
            call.success();
        } catch (MqttException e) {
            e.printStackTrace();
        }
        call.success();
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            MQTTService.LocalBinder binder = (MQTTService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
}
