package org.cdortona.tesi;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.util.List;
import java.util.UUID;

/**
 * Cristian D'Ortona
 *
 * TESI DI LAUREA IN INGEGNERIA ELETTRONICA E DELLE TELECOMUNICAZIONI
 *
 */

class ConnectToGattServer {

    private Context mContext;

    private BluetoothAdapter bluetoothAdapter;

    //GATT variables
    private BluetoothGatt gatt;
    private BluetoothGattService gattService;
    private List<BluetoothGattService> gattServicesList;
    private BluetoothGattCharacteristic gattCharacteristicTemp;
    private BluetoothGattCharacteristic gattCharacteristicHearth;
    private List<BluetoothGattCharacteristic> gattCharacteristicsList;

    //Constructor which initializes the dependencies needed for the connection to the GATT server of the remote device(ESP32)
    ConnectToGattServer(String deviceAddress, Context context){
        mContext = context;
        final BluetoothManager bluetoothManager =(BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        //connectToGatt(deviceAddress);
    }

    //method called whenever the "connect" button is pressed
    void connectToGatt(String deviceAddress){
        //since I know which is the address of the device I want to connect to, I use the bluetoothAdapter object
        //in order to get to connect to the remote BLE advertiser
        try {
            BluetoothDevice bleAdvertiser = bluetoothAdapter.getRemoteDevice(deviceAddress);
            Log.d("connectToGatt", "found device with the following MAC address: " + deviceAddress);
            //this creates a bond with the remote device once the connection is set
            //bleAdvertiser.createBond();
            //this method is gonna connect to the remote GATT server and the result will be handled by the callBack method
            //the auto-connect is set to true, which means the phone will automatically connect to the remote device when nearby
            //the auto-connect only works if the device is bounded to the gatt server, hence only if there is a secure connection between the two parties
            gatt = bleAdvertiser.connectGatt(mContext, true, gattCallBack);
        } catch(IllegalArgumentException e){
            e.getStackTrace();
            Log.e("connectToGatt", "the address is not associated to any BLE advertiser nearby");
            Toast.makeText(mContext, "Error, the address doesn't match any BLE advertiser nearby", Toast.LENGTH_LONG).show();
        }
    }

    //this method is invoked whenever there is the necessity to disconnect from the GATT server
    void disconnectGattServer(){
        if(gatt != null){
            Log.d("disconnectGattServer", "GATT server is disconnecting...");
            gatt.disconnect();
            gatt.close();
        }
    }

    //anonymous inner class
    private final BluetoothGattCallback gattCallBack = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            //I'm creating an intent which contains the intent-filter used to identify the connection state of the GATT server
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d("onConnectionStateChange", "connected to Bluetooth successfully");
                discoverServicesDelay(gatt);
                updateBroadcast(StaticResources.STATE_CONNECTED);
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d("onConnectionStateChange", "disconnecting GATT server");
                disconnectGattServer();
                updateBroadcast(StaticResources.STATE_DISCONNECTED);
            } else if(newState == BluetoothProfile.STATE_CONNECTING){
                Log.d("onConnectionStateChange", "Connecting...");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            gattServicesList = gatt.getServices();
            gattService = gatt.getService(UUID.fromString(StaticResources.ESP32_SERVICE));
            gattCharacteristicsList = gattService.getCharacteristics();

            //debug
            for(int i=1; i<gattServicesList.size(); i++){
                Log.i("onServicesDiscovered", "I'm printing the list of the services:" + gattServicesList.get(i).getUuid().toString() + '\n');
            }

            assignCharacteristics(gattCharacteristicsList);
        }

        //this method works if the GATT server has one or more characteristic with the property NOTIFY or INDICATE
        //whenever the value of that characteristic changes, then the GATT server notifies the client through this method
        //so the new value of the characteristic can be read and the result of the reading will be caught by the callBack method onCharacteristicRead
        //this permits an asynchronous communication between the server and the client
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.d("onCharacteristicChanged", "method has been called");
            super.onCharacteristicChanged(gatt, characteristic);

            sensorValueBroadcast(characteristic);
        }
    };

    //background process which is going to cause the current thread to sleep for the specified time
    //it's used to avoid problems during connection
    private void discoverServicesDelay(BluetoothGatt gatt) {
        try {
            Thread.sleep(600);
            Log.d("discoverServicesDelay", "delay before discovering services...");
            gatt.discoverServices();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Toast.makeText(mContext, "Could not find BLE services.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateBroadcast(String value){
        Intent intent = new Intent(StaticResources.ACTION_CONNECTION_STATE);
        intent.putExtra(StaticResources.EXTRA_STATE_CONNECTION, value);
        mContext.sendBroadcast(intent);
    }

    private void sensorValueBroadcast(BluetoothGattCharacteristic characteristic){
        Intent intent = new Intent(StaticResources.ACTION_CHARACTERISTIC_CHANGED_READ);
        switch (characteristic.getUuid().toString()){
            case StaticResources.ESP32_TEMP_CHARACTERISTIC:
                byte[] tempData = characteristic.getValue();
                String tempMessage = new String(tempData);
                intent.putExtra(StaticResources.EXTRA_TEMP_VALUE, tempMessage);
                mContext.sendBroadcast(intent);
                break;
            case StaticResources.ESP32_HEARTH_CHARACTERISTIC:
                byte[] heartRate = characteristic.getValue();
                String heartMessage = new String(heartRate);
                intent.putExtra(StaticResources.EXTRA_HEART_VALUE, heartMessage);
                mContext.sendBroadcast(intent);
                break;
            case StaticResources.ESP32_BRIGHTNESS_CHARACTERISTIC:
                byte[] brightnessData = characteristic.getValue();
                String brightnessMessage = new String(brightnessData);
                intent.putExtra(StaticResources.EXTRA_BRIGHTNESS_VALUE, brightnessMessage);
                mContext.sendBroadcast(intent);
                break;
        }
    }

    //this method is used to find the predefined characteristics and then assign them to their BluetoothGattCharacteristic objects
    private void assignCharacteristics(List<BluetoothGattCharacteristic> foundCharacteristics){

        for(int i=0; i<foundCharacteristics.size(); i++){

            switch(foundCharacteristics.get(i).getUuid().toString()) {
                case StaticResources.ESP32_TEMP_CHARACTERISTIC:
                    gattCharacteristicTemp = foundCharacteristics.get(i);
                    Log.d("assignCharacteristics" , "charactersitic has been assigned correctly, " +
                            + '\n' + "UUID: " + foundCharacteristics.get(i).getUuid());

                    setCharacteristicNotification(foundCharacteristics.get(i));
                    break;
                case StaticResources.ESP32_HEARTH_CHARACTERISTIC:
                    gattCharacteristicHearth = foundCharacteristics.get(i);
                    Log.d("assignCharacteristics" , "charactersitic has been assigned correctly, " +
                            + '\n' + "UUID: " + foundCharacteristics.get(i).getUuid());

                    setCharacteristicNotification(foundCharacteristics.get(i));
                    break;
                default:
                    //this happens when there is a characteristic in the service that isn't part of the predefined ones
                    Log.d("assignCharacteristics", "Characteristic not listed in the predefined ones"
                            + '\n' + "UUID: " + foundCharacteristics.get(i).getUuid());
                    break;
            }
        }
    }

    //in order to use the notify property of the characteristic, a descriptor has been defined which lets the client
    //decide whether enabling the notify property of the GATT server characteristic or not
    // but setting CCCD value is the only way you can tell the API whether you are going to turn on notification
    private void setCharacteristicNotification(BluetoothGattCharacteristic characteristic){
        gatt.setCharacteristicNotification(characteristic, true);
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(characteristic.getUuid());
        gatt.writeDescriptor(descriptor);
        Log.d("setNotification", "Notification Enabled");
    }
}
