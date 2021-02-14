package org.cdortona.tesi;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.util.List;
import java.util.UUID;

class ConnectToGattServer {

    private Context mContext;
    private final String TAG = "ConnectToGattServer";

    private BluetoothAdapter bluetoothAdapter;

    //GATT variables
    private BluetoothGatt gatt;
    private BluetoothGattService gattService;
    private List<BluetoothGattService> gattServicesList;
    private BluetoothGattCharacteristic gattCharacteristicTemp;
    private BluetoothGattCharacteristic gattCharacteristicHearth;
    private List<BluetoothGattCharacteristic> gattCharacteristicsList;

    //Initializing dependencies needed for the connection to the GATT server of the remote device(ESP32)
    ConnectToGattServer(String deviceAddress, Context context){
        mContext = context;
        final BluetoothManager bluetoothManager =(BluetoothManager) context.getSystemService(context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        //connectToGatt(deviceAddress);
    }

    void connectToGatt(String deviceAddress){
        //since I know which is the address of the device I want to connect to, I use the bluetoothAdapter object
        //in order to get to connect to the remote BLE advertiser
        //the try catch blocks is used in order to check if there is a device with that address
        try {
            BluetoothDevice bleAdvertiser = bluetoothAdapter.getRemoteDevice(deviceAddress);
            Log.d(TAG, "found device with the following MAC address: " + deviceAddress);
            //this method is gonna connect to the remote GATT server and the result will be handled by the callBack method
            gatt = bleAdvertiser.connectGatt(mContext, true, gattCallBack);
        } catch(IllegalArgumentException e){
            e.getStackTrace();
            Log.e(TAG, "the address is not associated to any BLE advertiser nearby");
            Toast.makeText(mContext, "Error, address doesn't match any BLE advertiser nearby", Toast.LENGTH_LONG).show();
        }
    }

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

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                //Toast.makeText(getBaseContext(), "Device Connected", Toast.LENGTH_SHORT).show();
                Log.d("onConnectionStateChange", "connected to Bluetooth successfully");
                discoverServicesDelay(gatt);
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                //Toast.makeText(getBaseContext(), "Device Disconnected", Toast.LENGTH_SHORT).show();
                Log.d("onConnectionStateChange", "disconnecting GATT server");
                disconnectGattServer();
            } else if(newState == BluetoothProfile.STATE_CONNECTING){
                //Toast.makeText(getBaseContext(), "Connecting...", Toast.LENGTH_SHORT).show();
                Log.d("onConnectionStateChange", "Connecting...");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            gattServicesList = gatt.getServices();
            gattService = gatt.getService(UUID.fromString(StaticResources.ESP32_SERVICE));
            gattCharacteristicsList = gattService.getCharacteristics();
            assignCharacteristics(gattCharacteristicsList);


            //make this a logCat that prints all the info about discovered services and characteristics
            for(int i=1; i<gattServicesList.size(); i++){
                System.out.println("I'm printing the list of the services:" + gattServicesList.get(i).getUuid().toString() + '\n');
            }

        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
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
            }
        }
    }
}
