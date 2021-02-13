package org.cdortona.tesi;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

public class SensorsInfo extends AppCompatActivity {

    //GATT connection
    BluetoothGatt gatt;
    List<BluetoothGattService> gattServices;
    BluetoothGattService gattMainService;
    List<BluetoothGattCharacteristic> gattCharacteristics;
    BluetoothGattCharacteristic gattCharacteristicTemp, gattCharacteristicHearth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensors_info);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

    }


    //this is an abstract method which handles back the results from connecting to the specified Gatt Server
    BluetoothGattCallback gattCallBack = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            /*if (status == BluetoothGatt.GATT_FAILURE) {
                disconnectGattServer();
                return;
            } else if (status != BluetoothGatt.GATT_SUCCESS) {
                // handle anything not SUCCESS as failure
                disconnectGattServer();
                return;
            }*/

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                //Toast.makeText(getBaseContext(), "Device Connected", Toast.LENGTH_SHORT).show();
                Log.d("onConnectionStateChange", "connected to Bluetooth successfully");
                discoverServicesDelay(gatt);
                Log.d("onConnectionStateChange", "discoverServices started for results");
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
            if(status == BluetoothGatt.GATT_SUCCESS){
                Log.d("onServicesDiscovered", "remote device has been explored successfully for services");
                //this is a list of all the services found
                gattServices = gatt.getServices();
                //method invoked to check if the ESP32 ATT service specified as a static constant actually exists
                gattMainService = checkServiceExists(gattServices);
                if(gattMainService != null){
                    //since the ESP32 service exists, I'm saving all its characteristics in the list
                    gattCharacteristics = gattMainService.getCharacteristics();
                    //method invoked in order to gain the characteristics needed and assign them to the correct variables
                    assignCharacteristics(gattCharacteristics);
                } else {
                    Log.e("onServicesDiscovered", "no service matches with the predefined one");
                    //Toast.makeText(getBaseContext(), "No service found matches the predefined one", Toast.LENGTH_SHORT).show();
                }
            }
            else if(status == BluetoothGatt.GATT_FAILURE){
                Log.e("onServicesDiscovered", "remote device hasn't been explored successfully for services");
                //Toast.makeText(getBaseContext(), "There has been a problem with the services discovery of the remote device", Toast.LENGTH_LONG).show();
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
            Toast.makeText(this, "Could not find BLE services.", Toast.LENGTH_SHORT).show();

        }
    }

    //this method checks if the service exists and return it if it does exist
    private BluetoothGattService checkServiceExists(List<BluetoothGattService> foundServices){
        for(int i=0; i<foundServices.size(); i++){
            if(foundServices.get(i).getUuid().toString().equals(StaticResources.ESP32_SERVICE)) {
                gattMainService = foundServices.get(i);
                //Toast.makeText(getBaseContext(), "Service found", Toast.LENGTH_SHORT).show();
                Log.d("checkServiceExists", "Service has been found, " + "UUID: " + foundServices.get(i).getUuid());
                return gattMainService;
            }
        }
        return null;
    }

    //this method is used to find the predefined characteristics and then assign them to their BluetoothGattCharacteristic object
    private void assignCharacteristics(List<BluetoothGattCharacteristic> foundCharacteristics){
        for(int i=0; i<foundCharacteristics.size(); i++){
            switch(foundCharacteristics.get(i).getUuid().toString()) {
                case StaticResources.ESP32_TEMP_CHARACTERISTIC:
                    gattCharacteristicTemp = foundCharacteristics.get(i);
                    Log.e("assignCharacteristics" , "charactersitic has been assigned correctly, " +
                            + '\n' + "UUID: " + foundCharacteristics.get(i).getUuid());
                    break;
                case StaticResources.ESP32_HEARTH_CHARACTERISTIC:
                    gattCharacteristicHearth = foundCharacteristics.get(i);
                    Log.e("assignCharacteristics" , "charactersitic has been assigned correctly, " +
                            + '\n' + "UUID: " + foundCharacteristics.get(i).getUuid());
                    break;
                default:
                    //this happens when there is a characteristic in the service that isn't part of the predefined ones
                    Log.e("assignCharacteristics", "Characteristic not listed in the predefined ones"
                            + '\n' + "UUID: " + foundCharacteristics.get(i).getUuid());
            }
        }
    }

    private void disconnectGattServer(){
        if(gatt != null){
            gatt.disconnect();
            gatt.close();
        }
    }
}
