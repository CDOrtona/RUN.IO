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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.nio.ByteBuffer;
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
        final BluetoothManager bluetoothManager =(BluetoothManager) context.getSystemService(context.BLUETOOTH_SERVICE);
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
            //this method is gonna connect to the remote GATT server and the result will be handled by the callBack method
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

    private BluetoothGattCallback gattCallBack = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            //I'm creating an intent which contains the intent-filter used to identify the connection state of the GATT server
            Intent intent = new Intent(StaticResources.BROADCAST_CONNECTION_STATE);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d("onConnectionStateChange", "connected to Bluetooth successfully");
                intent.putExtra(StaticResources.EXTRA_STATE_CONNECTION, StaticResources.STATE_CONNECTED);
                discoverServicesDelay(gatt);
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d("onConnectionStateChange", "disconnecting GATT server");
                intent.putExtra(StaticResources.EXTRA_STATE_CONNECTION, StaticResources.STATE_DISCONNECTED);
                disconnectGattServer();
            } else if(newState == BluetoothProfile.STATE_CONNECTING){
                Log.d("onConnectionStateChange", "Connecting...");
            }
            //I'm broadcasting the intent to the class which subscribed to receive them
            //that class has to define which are the intent filters it wants to subscribe to in order to get the updates
            //this works similarly to a publish-subscribe pattern
            mContext.sendBroadcast(intent);
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            gattServicesList = gatt.getServices();
            gattService = gatt.getService(UUID.fromString(StaticResources.ESP32_SERVICE));
            gattCharacteristicsList = gattService.getCharacteristics();

            Intent intent = new Intent(StaticResources.BROADCAST_ESP32_INFO);
            intent.putExtra(StaticResources.EXTRA_TERMINAL_SERVICE, gattService.getUuid().toString());

            //debug
            for(int i=1; i<gattServicesList.size(); i++){
                Log.i("onServicesDiscovered", "I'm printing the list of the services:" + gattServicesList.get(i).getUuid().toString() + '\n');
            }

            assignCharacteristics(gattCharacteristicsList);
            intent.putExtra(StaticResources.EXTRA_TERMINAL_CHARACTERISTIC_TEMP, gattCharacteristicTemp.getUuid().toString());
            mContext.sendBroadcast(intent);

            //this works if I'm not using notify property
            //this is an asynchronous operation and the result is reported by the callBack method onCharacteristicRead
            //Log.d("onServiceDiscovered", "started characteristic reading...");
            //gatt.readCharacteristic(gattCharacteristicTemp);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Intent intent = new Intent(StaticResources.BROADCAST_CHARACTERISTIC_READ);
            //right now the temperature is a string because the physical sensor hasn't been implemented yet
            //and I'm using the serialMonitor to send strings
            byte[] rawData = characteristic.getValue();
            String message = new String(rawData);

            System.out.println("output: " + message + '\n');
            //this is printing to terminal now, it'll need to be changed
            intent.putExtra(StaticResources.EXTRA_TEMP_VALUE, message);
            mContext.sendBroadcast(intent);
        }

        //this method works if the GATT server has one or more characteristic with the property NOTIFY or INDICATE
        //whenever the value of that characteristic changes, then the GATT server notifies the client through this method
        //so the new value of the characteristic can be read and the result of the reading will be caught by the callBack method onCharacteristicRead
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.d("onCharacteristicChanged", "method has been called");
            super.onCharacteristicChanged(gatt, characteristic);
            Intent intent = new Intent(StaticResources.BROADCAST_CHARACTERISTIC_CHANGED);
            gatt.readCharacteristic(characteristic);
            mContext.sendBroadcast(intent);
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

    //this method is used to find the predefined characteristics and then assign them to their BluetoothGattCharacteristic object
    //this shall be removed as the characteristics are known and defined as static resources
    private void assignCharacteristics(List<BluetoothGattCharacteristic> foundCharacteristics){
        for(int i=0; i<foundCharacteristics.size(); i++){
            switch(foundCharacteristics.get(i).getUuid().toString()) {
                case StaticResources.ESP32_TEMP_CHARACTERISTIC:
                    gattCharacteristicTemp = foundCharacteristics.get(i);
                    gatt.setCharacteristicNotification(gattCharacteristicTemp, true);
                    //for some reason I've to declare a descriptor to make the notify function work
                    BluetoothGattDescriptor descriptor = gattCharacteristicTemp.getDescriptor(UUID.fromString(StaticResources.ESP32_DESCRIPTOR));
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    gatt.writeDescriptor(descriptor);
                    Log.d("assignCharacteristics" , "charactersitic has been assigned correctly, " +
                            + '\n' + "UUID: " + foundCharacteristics.get(i).getUuid());
                    break;
                case StaticResources.ESP32_HEARTH_CHARACTERISTIC:
                    gattCharacteristicHearth = foundCharacteristics.get(i);
                    Log.d("assignCharacteristics" , "charactersitic has been assigned correctly, " +
                            + '\n' + "UUID: " + foundCharacteristics.get(i).getUuid());
                    break;
                default:
                    //this happens when there is a characteristic in the service that isn't part of the predefined ones
                    Log.d("assignCharacteristics", "Characteristic not listed in the predefined ones"
                            + '\n' + "UUID: " + foundCharacteristics.get(i).getUuid());
                    break;
            }
        }
    }
}
