package com.raxim.capacitor.plugins.social.mqtt;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.raxim.capacitor.R;

import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

import java.nio.charset.Charset;
import java.util.Random;

public class MQTTService extends Service {
    private static final String CHANNEL_CHAT_ID = "com.raxim.capacitor.plugins.mqtt.CHAT";
    private final Random mGenerator = new Random();
    private static final int DEFAULT_QOS = 1;

    private static final String DEFAULT_TOPIC = "mqtt/chat";

    private Thread mqttThread;
    private IMqttAsyncClient mqttClient;

    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        MQTTService getService() {
            return MQTTService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            this.mqttClient.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannel();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_CHAT_ID);
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        builder.setSound(soundUri);
        builder.setOnlyAlertOnce(true);

        Context context = getApplicationContext();
        Resources resources = context.getResources();
        builder.setSmallIcon(resources.getIdentifier("ic_stat_heart_outline", "drawable", context.getPackageName()));

        int note_id = mGenerator.nextInt(9999 - 1000) + 1000;
        startForeground(note_id, builder.build());

        String url = getResources().getString(R.string.mqtt_url);
        String username = getResources().getString(R.string.mqtt_username);
        String password = getResources().getString(R.string.mqtt_password);
        MqttClientPersistence persistence = new MqttDefaultFilePersistence(getCacheDir().getAbsolutePath());
        try {
            IMqttAsyncClient mqttClient = new MqttAsyncClient(url, MqttAsyncClient.generateClientId(), persistence);
            mqttClient.setCallback(new MqttCallbackExtended() {
                @Override
                public void connectComplete(boolean bReconnect, String host) {
                    final String METHOD = "connectComplete";

                    Log.d(MQTTService.class.getName(), METHOD + " Connected to " + host + " Auto reconnect ? " + bReconnect);
                    try {
                        subscribe(DEFAULT_TOPIC);
                    } catch (MqttException ex) {
                        ex.printStackTrace();
                    }
                }

                @Override
                public void connectionLost(Throwable thrwbl) {
                    Log.d(MQTTService.class.getName(), "connection lost");
                }

                @Override
                public void messageArrived(String string, MqttMessage mm) throws Exception {
                    Log.d("message_arrived", "message arrived");

                    Intent messageIntent = new Intent(MQTT_ACTION.MESSAGE_RECEIVED.name());
                    messageIntent.putExtra("payload", mm.getPayload());
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(messageIntent);
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                    //should respond back, when message has been sent
                }
            });

            this.mqttClient = mqttClient;

            this.mqttThread = new Thread(new MQTTRunnable(url, username, password, this.mqttClient));
            this.mqttThread.start();
            //maybe it will be OAUTH, when app started at first time
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    public void publish(String topic, String msg) throws MqttException {
        MqttMessage message = new MqttMessage(msg.getBytes(Charset.forName("UTF-8")));
        message.setQos(DEFAULT_QOS);
        this.mqttClient.publish(topic, message);
    }

    public void subscribe(String topic) throws MqttException {
        this.mqttClient.subscribe(topic, DEFAULT_QOS);
    }

    public void unsubscribe(String topic) throws MqttException {
        this.mqttClient.unsubscribe(topic);
    }

    //API 26+ (oreo only)
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_CHAT_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
