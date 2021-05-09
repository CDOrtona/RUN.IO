package org.cdortona.tesi;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttToken;
import org.eclipse.paho.client.mqttv3.internal.wire.MqttDisconnect;

/**
 * Cristian D'Ortona
 *
 * TESI DI LAUREA IN INGEGNERIA ELETTRONICA E DELLE TELECOMUNICAZIONI
 *
 */

class MqttConnection {

    String TAG = "MqttConnection";

    SensorModel sensorsReadValue;
    Context mContex;

    //this id identifies this client in the MQTT protocol
    String clientId = MqttClient.generateClientId();
    MqttAndroidClient client = new MqttAndroidClient(mContex, "IP address of the broker", clientId);

    public MqttConnection(SensorModel receivedSensorValues, Context context){
        try{
            this.sensorsReadValue = receivedSensorValues;
            this.mContex = context;
            mqttConnect();
        } catch (NullPointerException e){
            e.printStackTrace();
            Log.w(TAG, "SensorMode object passed to the constructor is empty");
        }
    }

    private void mqttConnect(){
        MqttConnectOptions options = new MqttConnectOptions();
        byte[] payload = StaticResources.LWT_MESSAGE.getBytes();
        String LWTTopic = StaticResources.LWT_TOPIC;
        options.setWill(LWTTopic, payload, StaticResources.QOS_1, false);

        options.setUserName("USERNAME");
        options.setPassword("PASSWORD".toCharArray());

        try{
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @SuppressLint("ShowToast")
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "MQTT Client successfully connected");
                    Toast.makeText(mContex, "MQTT connection succeeded", Toast.LENGTH_SHORT);
                }

                @SuppressLint("ShowToast")
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w(TAG, "MQTT Client unsuccessfully connected");
                    Toast.makeText(mContex, "MQTT connection failed", Toast.LENGTH_SHORT);
                }
            });

        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    void sendMessage(){
        publishMessage(StaticResources.TEMP_TOPIC, sensorsReadValue.getTemp(), StaticResources.QOS_0);
        publishMessage(StaticResources.HUMIDITY_TOPIC, sensorsReadValue.getHumidity(), StaticResources.QOS_0);
        publishMessage(StaticResources.ALTITUDE_TOPIC, sensorsReadValue.getAltitude(), StaticResources.QOS_0);
        publishMessage(StaticResources.PRESSURE_TOPIC, sensorsReadValue.getPressure(), StaticResources.QOS_0);
        publishMessage(StaticResources.HEART_TOPIC, sensorsReadValue.getHeart(), StaticResources.QOS_0);
        publishMessage(StaticResources.GPS_TOPIC, sensorsReadValue.getGps(), StaticResources.QOS_0);
        publishMessage(StaticResources.SOS_TOPIC, sensorsReadValue.getSos(), StaticResources.QOS_2);
    }

    private void publishMessage(String topic, byte[] payload, int qos){
        try{
            MqttMessage message = new MqttMessage();
            message.setPayload(payload);
            message.setQos(qos);
            client.publish(topic, message);
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    Log.w(TAG, "Mqtt connection lost due to the following cause : " + cause);
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    Log.d(TAG, "Message arrived with topic: " + topic + " and QOS: " + message.getQos());
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    Log.d(TAG, "Message successfully delivered" );
                }
            });
        } catch (MqttException e){
            e.printStackTrace();
        }
    }

    //callback for when the client publish the messages
    void mqttDisconnect(){
        try {
            IMqttToken disconnect = client.disconnect();
            disconnect.setActionCallback(new IMqttActionListener() {
                @SuppressLint("ShowToast")
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d(TAG, "MQTT Client successfully disconnected");
                    Toast.makeText(mContex, "MQTT disconnection succeeded", Toast.LENGTH_SHORT);
                }

                @SuppressLint("ShowToast")
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w(TAG, "MQTT Client unsuccessfully disconnected");
                    Toast.makeText(mContex, "MQTT disconnection failed", Toast.LENGTH_SHORT);
                }
            });
        }catch (MqttException e){
            e.printStackTrace();
        }
    }
}
