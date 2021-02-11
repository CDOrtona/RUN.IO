package org.cdortona.tesi;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

//class made in order to menage the BLE advertisers which have been scanned
class DeviceScanned {

    //this is a map of address:BluetoothDevice
    private HashMap<String, BluetoothDevice> bluetoothDeviceHashMap = new HashMap<>();

    //method called to add entries in the HashMap
    void add(ScanResult scanResult){
        this.bluetoothDeviceHashMap.put(scanResult.getDevice().getAddress(), scanResult.getDevice());
        Log.d("addHashMap", "entry added to HashMap correctly");
    }

    String getDeviceName(String address){
       try {
           return bluetoothDeviceHashMap.get(address).getName();
       } catch (NullPointerException e){
           e.getStackTrace();
           return "error";
       }
    }

    String getAlias(String address){
        try {
            return bluetoothDeviceHashMap.get(address).getAlias();
        } catch (NullPointerException e){
            e.getStackTrace();
            return "error";
        }
    }

    String getBleAddress(String address){
        try {
            return bluetoothDeviceHashMap.get(address).getAddress();
        } catch (NullPointerException e){
            e.getStackTrace();
            return "error";
        }
    }

    //this method is used to retrieve the value from the key
    BluetoothDevice getBleDevice(String address){
        //this checks whether the input key is valid or not
        if(bluetoothDeviceHashMap.containsKey(address)){
            Log.d("getBleDevice", "input key is consistent");
            return bluetoothDeviceHashMap.get(address);
        } else{
            Log.d("getBleDevice", "input key not found");
            return null;
        }

    }

    //this method deletes all the entries of the map
    void flush(){
        bluetoothDeviceHashMap.clear();
        Log.d("flush", "map has been flushed from all the entries");
    }

    void printDeviceInfo(){
        if(bluetoothDeviceHashMap.isEmpty()){
            Log.w("printDeviceInfo", "HashMap is empty");
        } else {
            for(Map.Entry<String, BluetoothDevice> entry : bluetoothDeviceHashMap.entrySet()){
                System.out.println( '\n' + "Address: " + entry.getKey() + '\n'
                        + "Device Name: " + entry.getValue().getName() + '\n'
                        + "Alias: " + entry.getValue().getAlias() + '\n'
                        + "Bound State: " + entry.getValue().getBondState());
            }
        }
    }

    //eventually I can add the rssi implementation
}
