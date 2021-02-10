package org.cdortona.tesi;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private final int REQUEST_ENABLE_BT = 1;
    private final int REQUEST_FINE_LOCATION = 2;

    private BluetoothManager bluetoothManager;
    private BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        LinearLayout linearLayout = findViewById(R.id.linear_layout);

        hasBle();
        checkBleStatus();
        grantLocationPermissions();


        //eventually this'll be replaced with a ListView and adapters
        //this is used to add several buttons programmatically
        for(int i=0; i<15; i++) {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

            Button bleAdvertiser = new Button(this);
            bleAdvertiser.setLayoutParams(params);
            bleAdvertiser.setText("Name: something");
            bleAdvertiser.setId(i);

            bleAdvertiser.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //do something when button pressed
                }
            });

            linearLayout.addView(bleAdvertiser);

        }

    }

    //this checks at run time whether or not the device has a BLE adapter
    private void hasBle() {
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






}