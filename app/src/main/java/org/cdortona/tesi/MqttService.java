package org.cdortona.tesi;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.nio.charset.StandardCharsets;

public class MqttService extends Service {

    String TAG = "MqttService";
    MqttClient client;
    private final String serverUri = "tcp://192.168.1.45:1883";

    String temp;
    String heartBeat;
    String humidity;
    String locations;
    String pressure;
    String altitude;
    Boolean sos;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    //this runs only the first time the service is created
    @Override
    public void onCreate() {
        super.onCreate();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(StaticResources.ACTION_CONNECTION_STATE);
        intentFilter.addAction(StaticResources.ACTION_CHARACTERISTIC_CHANGED_READ);
        registerReceiver(bleBroadcastReceiver, intentFilter);

        String clientId = MqttClient.generateClientId();

        //client = new MqttAndroidClient(getApplicationContext(), serverUri, clientId);


        try {
            MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
            mqttConnectOptions.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
            mqttConnectOptions.setCleanSession(false);
            //mqttConnectOptions.setUserName("test");
            //mqttConnectOptions.setPassword("test".toCharArray());
            client = new MqttClient(serverUri,clientId, null);
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    Log.e(TAG, "Connection Lost");
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    Log.e(TAG, "Message arrived");

                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    Log.e(TAG, "Delivery Complete");
                }
            });

            client.connect(mqttConnectOptions);

        }  catch (MqttException ex){
            ex.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(temp != null)
            pub(StaticResources.TEMP_TOPIC, temp);

        return super.onStartCommand(intent, flags, startId);
    }


    final BroadcastReceiver bleBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String broadcastReceived = intent.getAction();
            Log.d(TAG, "The received Broadcast is: " + broadcastReceived);

            assert broadcastReceived != null;
            if(broadcastReceived.equals(StaticResources.ACTION_CHARACTERISTIC_CHANGED_READ)){
                switch (intent.getStringExtra(StaticResources.EXTRA_CHARACTERISTIC_CHANGED)){
                    case StaticResources.ESP32_TEMP_CHARACTERISTIC:
                        temp = intent.getStringExtra(StaticResources.EXTRA_TEMP_VALUE);
                        break;
                    case StaticResources.ESP32_HEARTH_CHARACTERISTIC:
                        heartBeat = intent.getStringExtra(StaticResources.EXTRA_HEART_VALUE);
                        break;
                    case  StaticResources.ESP32_HUMIDITY_CHARACTERISTIC:
                        humidity = intent.getStringExtra(StaticResources.EXTRA_HUMIDITY_VALUE);
                        break;
                    case StaticResources.ESP32_PRESSURE_CHARACTERISTIC:
                        pressure = intent.getStringExtra(StaticResources.EXTRA_PRESSURE_VALUE);
                        break;
                    case StaticResources.ESP32_ALTITUDE_CHARACTERISTIC:
                        altitude = intent.getStringExtra(StaticResources.EXTRA_ALTITUDE_VALUE);
                        break;
                }
            }
        }
    };

    //this handles the publishing of the messages
    void pub(String topic, String payload){
        byte[] encodedPayload;
        try {
            encodedPayload = payload.getBytes(StandardCharsets.UTF_8);
            MqttMessage message = new MqttMessage(encodedPayload);
            client.publish(topic, message);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}