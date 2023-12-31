package com.raxim.capacitor.plugins.social.mqtt;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MQTTRunnable implements Runnable {
    private final String url;
    private final String username;
    private final String password;

    private final IMqttAsyncClient mqttAsyncClient;

    public MQTTRunnable(final String url, final String username, final String password, final IMqttAsyncClient mqttAsyncClient) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.mqttAsyncClient = mqttAsyncClient;
    }

    @Override
    public void run() {
        //https://github.com/jpmens/mosquitto-auth-plug
        try {
            MqttConnectOptions mqttOptions = new MqttConnectOptions();
            mqttOptions.setCleanSession(true);
            mqttOptions.setAutomaticReconnect(true);
            mqttOptions.setUserName(username);
            mqttOptions.setPassword(password.toCharArray());

            IMqttToken mqttToken = this.mqttAsyncClient.connect(mqttOptions);
            mqttToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.i("mqtt_connected", "MQTT is connected!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.i("mqtt_failed", "MQTT cannot connect!");
                }
            });
            Log.d("waitFor1", "waitFor1");
            mqttToken.waitForCompletion();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
