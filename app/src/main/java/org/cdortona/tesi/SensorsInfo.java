package org.cdortona.tesi;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;

/**
 * Cristian D'Ortona
 *
 * TESI DI LAUREA IN INGEGNERIA ELETTRONICA E DELLE TELECOMUNICAZIONI
 *
 */


public class SensorsInfo extends AppCompatActivity {

    private final String TAG = "SensorInfo";

    //I initialize an object from the class ConnectToGattServer which handles the connection to the GATT server of the ESP32
    ConnectToGattServer connectToGattServer;

    //GATT
    private String deviceAddress;
    private String deviceName;
    boolean connectedToGatt = false;
    private String stateConnection = null;
    private boolean flagDeviceFound = false;

    //flags used to tell which characteristic has changed
    boolean tempChanged = false;
    boolean heartChanged = false;

    TextView addressInfo;
    TextView nameInfo;
    TextView connectionState;
    TextView tempValue;
    TextView heartValue;
    TextView brightnessValue;
    TextView positionValue;


    //Toolbar
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensors_info_new);

        //UI setup
        addressInfo = findViewById(R.id.address_textView);
        nameInfo = findViewById(R.id.name_textView);
        connectionState = findViewById(R.id.connection_state_textView);
        tempValue = findViewById(R.id.textView_temp);
        heartValue = findViewById(R.id.textView_heart);
        brightnessValue = findViewById(R.id.textView_brightness);
        positionValue = findViewById(R.id.textView_position);

        //hard coded, I must change it later
        connectionState.setTextColor(Color.RED);
        connectionState.setText("Disconnected");

        //calling the constructor in order to build a BluetoothAdaptor object
        connectToGattServer = new ConnectToGattServer(deviceAddress, this);

        //location
        locationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationList = new ArrayList<>();

        //here I'm specifying the intent filters I want to subscribe to in order to get their updates
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(StaticResources.ACTION_CONNECTION_STATE);
        intentFilter.addAction(StaticResources.ACTION_CHARACTERISTIC_CHANGED_READ);
        registerReceiver(bleBroadcastReceiver, intentFilter);

        //Toolbar
        toolbar = findViewById(R.id.toolbar_sensors);
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onResume() {
        super.onResume();
        locationUpdate();
    }

    //Toolbar set up
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.toolbar_menu_sensors, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case(R.id.action_connect_to_peripheral):
                Intent scanDevices = new Intent(SensorsInfo.this, MainActivity.class);
                try{
                    startActivityForResult(scanDevices, StaticResources.REQUEST_CODE_SCAN_ACTIVITY);
                } catch (ActivityNotFoundException e){
                    e.printStackTrace();
                    finish();
                }
                return true;
            case (R.id.action_connect):
                connectToGatt();
                return true;
            case (R.id.action_disconnect):
                invalidateOptionsMenu();
                disconnectFromGatt();
                return true;

            case (R.id.action_graph_rssi):
                Intent rssiGraph = new Intent(SensorsInfo.this, GraphRssi.class);
                rssiGraph.putExtra(StaticResources.EXTRA_CHOOSEN_ADDRESS, deviceAddress);
                try{
                    startActivity(rssiGraph);
                } catch (ActivityNotFoundException e){
                    e.printStackTrace();
                    finish();
                }
                return true;

            case (R.id.action_mqtt):
                Toast.makeText(this, "Send data to Cloud", Toast.LENGTH_SHORT).show();
                //MQTT
                return true;

            case (R.id.action_about):
                Intent aboutWebView = new Intent(SensorsInfo.this, AboutWebView.class);
                aboutWebView.putExtra(StaticResources.WEB_PAGE, "https://github.com/CDOrtona/Tesi");
                startActivity(aboutWebView);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (connectedToGatt) {
            menu.findItem(R.id.action_connect).setVisible(false);
            menu.findItem(R.id.action_disconnect).setVisible(true);
            menu.findItem(R.id.action_mqtt).setEnabled(true);
            accessLocation();
            return true;
        } else if (!connectedToGatt) {
            menu.findItem(R.id.action_connect).setVisible(true);
            menu.findItem(R.id.action_disconnect).setVisible(false);
            menu.findItem(R.id.action_mqtt).setEnabled(false);
            return true;
        } else {
            return super.onPrepareOptionsMenu(menu);
        }
    }

    //I override this method to make sure that the GATT server is disconnected if the users goes back to the previous activity
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        disconnectFromGatt();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == StaticResources.REQUEST_CODE_SCAN_ACTIVITY && resultCode != Activity.RESULT_CANCELED){
            deviceAddress = data.getStringExtra(StaticResources.EXTRA_CHOOSEN_ADDRESS);
            deviceName = data.getStringExtra(StaticResources.EXTRA_CHOOSEN_NAME);
            //Setting the values of the TextViews objects
            addressInfo.setTypeface(Typeface.SANS_SERIF);
            addressInfo.setText(deviceAddress);
            nameInfo.setTypeface(Typeface.SANS_SERIF);
            nameInfo.setText(deviceName);
            flagDeviceFound = true;
        } else {
            flagDeviceFound = false;
        }
    }

    //this is used to receive the broadcast announcements that are being sent from the class ConnectToGattServer()
    //the received broadcasters depend on the intent filters declared above
    final BroadcastReceiver bleBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String broadcastReceived = intent.getAction();
            Log.d("bleBroadCastReceiver", "The received Broadcast is: " + broadcastReceived);

            switch (broadcastReceived) {

                case StaticResources.ACTION_CONNECTION_STATE:
                    stateConnection = intent.getStringExtra(StaticResources.EXTRA_STATE_CONNECTION);
                    if (stateConnection.equals(StaticResources.STATE_CONNECTED)) {
                        connectedToGatt = true;
                        invalidateOptionsMenu();
                        connectionStateString(StaticResources.STATE_CONNECTED);
                    } else if (stateConnection.equals(StaticResources.STATE_DISCONNECTED)) {
                        connectedToGatt = false;
                        invalidateOptionsMenu();
                        connectionStateString(StaticResources.STATE_DISCONNECTED);
                    }
                    break;
                //this received broadcast lets the activity that subbed to this intent filter know which is the characteristic that has changed
                case StaticResources.ACTION_CHARACTERISTIC_CHANGED_READ:
                    /*String notifiedCharacteristic = intent.getStringExtra(StaticResources.EXTRA_CHARACTERISTIC_NOTIFIED);
                    Log.d(TAG, "Characteristic notified: " + notifiedCharacteristic);
                    if (notifiedCharacteristic.equals(StaticResources.ESP32_TEMP_CHARACTERISTIC)) {
                        Log.d(TAG, "Characteristic is Temp");
                        tempChanged = true;
                        heartChanged = false;
                    } else if (notifiedCharacteristic.equals(StaticResources.ESP32_HEARTH_CHARACTERISTIC)) {
                        tempChanged = false;
                        heartChanged = true;
                        Log.d(TAG, "Characteristic is Heart");
                    }
                    break;*/
                    Log.d("whichCharChanged", StaticResources.EXTRA_CHARACTERISTIC_CHANGED);
                    switch (intent.getStringExtra(StaticResources.EXTRA_CHARACTERISTIC_CHANGED)){
                        case StaticResources.ESP32_TEMP_CHARACTERISTIC:
                            tempValue.setText(intent.getStringExtra(StaticResources.EXTRA_TEMP_VALUE));
                            break;
                        case StaticResources.ESP32_HEARTH_CHARACTERISTIC:
                            heartValue.setText(intent.getStringExtra(StaticResources.EXTRA_HEART_VALUE));
                            break;
                        case  StaticResources.ESP32_BRIGHTNESS_CHARACTERISTIC:
                            brightnessValue.setText(intent.getStringExtra(StaticResources.EXTRA_BRIGHTNESS_VALUE));
                            break;
                    }
                    /*String onUpdateTempValue = intent.getStringExtra(StaticResources.EXTRA_TEMP_VALUE);
                    String onUpdateHeartValue = intent.getStringExtra(StaticResources.EXTRA_HEART_VALUE);
                    String onUpdateBrightnessValue = intent.getStringExtra(StaticResources.EXTRA_BRIGHTNESS_VALUE);
                    tempValue.setText(onUpdateTempValue);
                    heartValue.setText(onUpdateHeartValue);
                    brightnessValue.setText(onUpdateBrightnessValue);*/
            }

        }
    };



    //add an if that checks if the adaptor is connected to the GATT server already
    public void connectToGatt() {
        if(flagDeviceFound){
            connectToGattServer.connectToGatt(deviceAddress);
            //this makes sure that there is a time out error if it takes more than 10 seconds to connect to the remote peripheral
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(stateConnection == null){
                        Log.w("connectToGatt", "Timeout connection to remote peripheral");
                        Toast.makeText(getApplicationContext(), "The connection has timed out, try again", Toast.LENGTH_SHORT).show();
                        connectionStateString(StaticResources.STATE_DISCONNECTED);
                        disconnectFromGatt();
                    }
                }
            }, 10000);
            connectionStateString(StaticResources.STATE_CONNECTING);
        } else {
            Toast.makeText(this, "Please, connect to remote device first", Toast.LENGTH_SHORT).show();
        }
    }

    public void disconnectFromGatt() {
        stateConnection = null;
        flagDeviceFound = false;
        connectToGattServer.disconnectGattServer();
        connectedToGatt = false;
        connectionStateString(StaticResources.STATE_DISCONNECTED);
        //this is gonna flush the location stored in the location variable
        locationProviderClient.flushLocations();
        locationProviderClient.removeLocationUpdates(locationCallBack);
    }

    //this dynamically changes the string color and text of the string that shows on screen the connection state
    private void connectionStateString(String state){
        switch (state) {
            case StaticResources.STATE_CONNECTED:
                connectionState.setTextColor(Color.GREEN);
                connectionState.setText(StaticResources.STATE_CONNECTED);
                break;
            case StaticResources.STATE_CONNECTING:
                connectionState.setTextColor(Color.YELLOW);
                connectionState.setText(StaticResources.STATE_CONNECTING);
                break;
            case StaticResources.STATE_DISCONNECTED:
                connectionState.setTextColor(Color.RED);
                connectionState.setText(StaticResources.STATE_DISCONNECTED);
                break;
        }
    }
}
