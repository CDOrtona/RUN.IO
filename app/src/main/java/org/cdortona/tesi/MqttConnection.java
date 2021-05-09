package org.cdortona.tesi;

import android.content.Context;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

/**
 * Cristian D'Ortona
 *
 * TESI DI LAUREA IN INGEGNERIA ELETTRONICA E DELLE TELECOMUNICAZIONI
 *
 */

public class MqttConnection {

    String TAG = "MqttConnection";

    SensorModel sensorsReadValue;
    Context mContex;

    //this id identifies this client in the MQTT protocol
    String clientId = MqttClient.generateClientId();
    MqttAndroidClient client = new MqttAndroidClient(mContex, "IP address of the broker", clientId);

    try{
        IMqttToken token = client.connect();
        token.setActionCallback(new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                     Log.d(TAG, "onSuccess");
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                Log.d(TAG, "onFailure");
            }
        });
    } catch (MqttException e){
        e.printStackTrace();
    }

    public MqttConnection(SensorModel receivedSensorValues, Context context){
        try{
            this.sensorsReadValue = receivedSensorValues;
            this.mContex = context;
        } catch (NullPointerException e){
            e.printStackTrace();
            Log.w(TAG, "SensorMode object passed to the constructor is empty");
        }
    }
}
