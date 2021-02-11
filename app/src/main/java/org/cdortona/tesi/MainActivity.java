package org.cdortona.tesi;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private final int REQUEST_ENABLE_BT = 1;
    private final int REQUEST_FINE_LOCATION = 2;

    BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private boolean scanning = false;
    DeviceScanned deviceScanned;

    //this end the scan every 10 seconds
    //it's very important as in a LE application we want to reduce battery-intensive tasks
    private static final long SCAN_PERIOD = 200000;

    //GATT connection
    BluetoothGatt gatt;

    //debug garbage
    EditText editText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //debug garbage
        editText = findViewById(R.id.editText);

        //initialize bluetooth adapter
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        //initialize scan dependencies
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        deviceScanned = new DeviceScanned();

        checkBleStatus();
        grantLocationPermissions();

    }

    @Override
    protected  void onResume(){
        super.onResume();

        //this checks on whether the BLE adapter is integrated in the device or not
        //the app won't work with classic Bluetooth
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    //method used to check on whether the BLE adapter is enabled or not
    //if it's not enabled then a new implicit intent, whose action is a BLE activation request, is instantiated
    //this'll prompt a window where the user'll be able to either agree or not on the activation of the BLE
    //the REQUEST_ENABLE_BT will be returned in OoActivityResult() if greater than 0
    private void checkBleStatus(){
        if(bluetoothAdapter == null || !bluetoothAdapter.isEnabled()){
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            //startActivityForResult throws the following exception
            try{
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                Log.d("request to turn BLE on", "Request to turn BLE sent");
            } catch (ActivityNotFoundException e){
                Log.e("request to turn BLE on", "the activity wasn't found");
            }
        }
    }

    //in order to use BLE I have to make sure fine_location permissions are enabled
    //it's considered a dangerous permission so I have to ask for it at run-time
    private void grantLocationPermissions(){
        if(!(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)){
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_FINE_LOCATION);
            Log.d("request fine_location", "request sent");
            //add builder eventually
        }
    }

    //callback method to catch the result of startActivityForResult
    //this method receives the response of the user if the activity that was launched exists
    //The requestCode used to lunch the activity as well as the resultCode are returned, the requestCode is used to identify the who this result come from
    //The resultCode will be RESULT_CANCELED if the activity explicitly returned that, didn't return any result, or crashed during its operation
    protected void onActivityResult(int requestCode, int resultCode, Intent data ){
        if(requestCode == REQUEST_ENABLE_BT){
            if(resultCode == RESULT_OK){
                //hard coded
                Toast.makeText(this,"Bluetooth Enabled", Toast.LENGTH_LONG).show();
            }
            else if(resultCode == RESULT_CANCELED){
                //hard coded
                Toast.makeText(this,"Bluetooth wasn't enabled", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    //this callback method is called to catch the result of the method requestPermissions launched in order to grand fine_location permissions
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if(requestCode == REQUEST_FINE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //hard coded
                Toast.makeText(this, "Location services enabled", Toast.LENGTH_SHORT).show();
                Log.d("result location request", "fine location has been granted");
            } else if(grantResults[0] == PackageManager.PERMISSION_DENIED){
                //hard coded
                Toast.makeText(this, "Location services must be enabled to use the app", Toast.LENGTH_LONG).show();
                Log.d("result location request", "fine location wasn't granted");
                finish();
            }
        }
    }

    //this starts the scan of the BLE advertiser nearby
    public void startScan(View v){
        if(!scanning){

            //this removes all the entries in the map from the previous scans if they occurred
            deviceScanned.flush();

            disconnectGattServer();

            //the handler is used to schedule an event to happen at some point in the future
            //in this case the method postDelayed causes the runnable to be added to the message queue
            // and it will be executed(run) after a given time SCAN_PERIOD
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanning = false;
                    //this's going to stop the scan if the user doesn't stop it before SCAN_PERIOD
                    bluetoothLeScanner.stopScan(leScanCallBack);
                    Log.d("startScan","scanning has stopped after time elapsed");
                }
            }, SCAN_PERIOD);

            scanning = true;
            //this is executed during the x time before the thread is activated
            bluetoothLeScanner.startScan(leScanCallBack);
            Log.d("startScan", "scan has started");
        }
    }

    //this stops the scan before the time elapses
    // when the button is pressed
    public void stopScan(View v){
        if(scanning){
            bluetoothLeScanner.stopScan(leScanCallBack);
            Log.d("stopScan", "BLE scanner has been stopped");
            //the following code is debug garbage, it needs to be deleted
            deviceScanned.printDeviceInfo();
        }
    }

    //callBack method used to catch the result of startScan()
    //this is an abstract class
    private ScanCallback leScanCallBack = new ScanCallback() {
        @Override
        //ScanResult is the result of the BLE scan
        //its method getDevice() permits to retrieve a BluetoothDevice object
        // which I use to gain info about the BLE advertisers scanned
        //callBackType determines how this callback was triggered
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            deviceScanned.add(result);
        }

        public void onScanFailed(int errorCode) {
            Log.e("leScanCallBack" ,"scan call back has failed with errorCode: " + errorCode);
        }
    };

    //this method will create a new connection with the GATT server of the BLE device specified
    public void connectToSelectedDevice(View v){
        //hard coded
        Toast.makeText(this, "Connecting...", Toast.LENGTH_SHORT).show();
        BluetoothDevice selectedBleDevice = deviceScanned.getBleDevice(editText.getText().toString());
        if(selectedBleDevice != null) {
            gatt = selectedBleDevice.connectGatt(getBaseContext(), true, gattCallBack);
            Log.d("connectToSelectedDevice", "connecting...");
        }
        else {
            //hard coded
            Toast.makeText(this, "incorrect address, device not found!", Toast.LENGTH_LONG).show();
            Log.w("connectToSelectedDevice", "input address doesn't match any key in the map");
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
                //setConnected(true);
                Log.d("onConnectionStateChange", "connected to Bluetooth successfully");
                //hardCoded
                gatt.discoverServices();
                Log.d("onConnectionStateChange", "discoverServices started for results");
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d("onConnectionStateChange", "disconnecting from Bluetooth");
                //hardCoded
                Log.d("onConnectionStateChange", "disconnecting GATT server");
                disconnectGattServer();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);

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


    private void disconnectGattServer(){
        if(gatt != null){
            gatt.disconnect();
            gatt.close();
        }
    }



    //debug garbage
    /*private void showListOfBle(){

        LinearLayout linearLayout = findViewById(R.id.linear_layout);

        //eventually this'll be replaced with a ListView and adapters
        //this is used to add several buttons programmatically
        for(int i=0; i<listScannedDevices.size(); i++) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

            Button bleAdvertiser = new Button(this);
            bleAdvertiser.setLayoutParams(params);
            bleAdvertiser.setText(deviceScanned.getBleAddress(i));
            bleAdvertiser.setId(i);

            bleAdvertiser.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });

            linearLayout.addView(bleAdvertiser);
        }


    }*/






}


/*comments
 *readCharacteristic(BluetoothGattCharacteristic characteristic)  result is caught by onCharacteristicRead() that is part of the abstract class BluetoothGattCallback https://developer.android.com/reference/android/bluetooth/BluetoothGatt#readCharacteristic(android.bluetooth.BluetoothGattCharacteristic)
 *
 * onCharacteristicChanged (BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) this notifies a characteristic has changed
 *
 * onCharacteristicChanged (BluetoothGatt gatt,BluetoothGattCharacteristic characteristic)

       private final BluetoothGattCallback btleGattCallback = new BluetoothGattCallback() {
           @override
           onCharacteristicChanged (BluetoothGatt gatt,BluetoothGattCharacteristic characteristic){

               gatt.readCharacteristic(characteristic)
            }
           @override
           onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic){
              string temperature = characteristic;
           }
        }
        *
        *


 */