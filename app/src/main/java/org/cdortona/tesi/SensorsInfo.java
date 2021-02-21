package org.cdortona.tesi;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

/**
 * Cristian D'Ortona
 *
 * TESI DI LAUREA IN INGEGNERIA ELETTRONICA E DELLE TELECOMUNICAZIONI
 *
 */


public class SensorsInfo extends AppCompatActivity {

    //I initialize an object from the class ConnectToGattServer which handles the connection to the GATT server of the ESP32
    ConnectToGattServer connectToGattServer;

    String deviceAddress;
    String deviceName;
    boolean connectedToGatt = false;

    //TextView objects
    ///TextView terminal;
    TextView addressInfo;
    TextView nameInfo;
    TextView connectionState;
    TextView tempValue;
    TextView heartValue;
    TextView brightness;
    TextView position;

    //Toolbar
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensors_info);

        //UI setup
        ///*terminal = findViewById(R.id.terminal_textView);
        addressInfo = findViewById(R.id.address_textView);
        nameInfo = findViewById(R.id.name_textView);
        connectionState = findViewById(R.id.connection_state_textView);
        tempValue = findViewById(R.id.textView_temp);
        heartValue = findViewById(R.id.textView_heart);
        brightness = findViewById(R.id.textView_brightness);
        position = findViewById(R.id.textView_position);


        //I have to retrieve the info from the Intent which called this activity
        Intent receivedIntent = getIntent();
        deviceAddress = receivedIntent.getStringExtra(StaticResources.EXTRA_CHOOSEN_ADDRESS);
        deviceName = receivedIntent.getStringExtra(StaticResources.EXTRA_CHOOSEN_NAME);


        //Setting the values of the TextViews objects
        addressInfo.setTypeface(Typeface.SANS_SERIF);
        addressInfo.setText(deviceAddress);
        nameInfo.setTypeface(Typeface.SANS_SERIF);
        nameInfo.setText(deviceName);
        ///terminal.setText("");
        //hard coded, I must change it later
        connectionState.setTextColor(Color.RED);
        connectionState.setText("Disconnected");

        //calling the constructor in order to build a BluetoothAdaptor object
        connectToGattServer = new ConnectToGattServer(deviceAddress, this);

        //here I'm specifying the intent filters I want to subscribe to in order to get their updates
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(StaticResources.BROADCAST_CONNECTION_STATE);
        intentFilter.addAction(StaticResources.BROADCAST_CHARACTERISTIC_CHANGED);
        intentFilter.addAction(StaticResources.BROADCAST_CHARACTERISTIC_READ);
        intentFilter.addAction(StaticResources.BROADCAST_ESP32_INFO);
        registerReceiver(bleBroadcastReceiver, intentFilter);

        //Toolbar
        toolbar = findViewById(R.id.toolbar_sensors);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    //Toolbar set up
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.toolbar_menu_sensors, menu);
        return true;
    }

    //this is called whenever the user clicks on the back arrow
    //it works as an UP button
    public boolean onSupportNavigateUp() {
        //this is called when the activity detects the user pressed the back button
        onBackPressed();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
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
                startActivity(rssiGraph);
                return true;
            case (R.id.action_mqtt):
                //MQTT
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if(connectedToGatt){
            menu.findItem(R.id.action_connect).setVisible(false);
            menu.findItem(R.id.action_disconnect).setVisible(true);
            return true;
        }
        else if(!connectedToGatt){
            menu.findItem(R.id.action_connect).setVisible(true);
            menu.findItem(R.id.action_disconnect).setVisible(false);
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

    //this is used to receive the broadcast announcements that are being sent from the class ConnectToGattServer()
    //the received broadcasters depend on the intent filters declared above
    final BroadcastReceiver bleBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String broadcastReceived = intent.getAction();
            Log.d("bleBroadCastReceiver", "The received Broadcast is: " + broadcastReceived);

            switch (broadcastReceived) {

                case StaticResources.BROADCAST_CONNECTION_STATE:
                    if(intent.getStringExtra(StaticResources.EXTRA_STATE_CONNECTION).equals(StaticResources.STATE_CONNECTED)) {
                        connectedToGatt = true;
                        invalidateOptionsMenu();
                        connectionState.setTextColor(Color.GREEN);
                        connectionState.setText(intent.getStringExtra(StaticResources.EXTRA_STATE_CONNECTION));
                    }
                    else if(intent.getStringExtra(StaticResources.EXTRA_STATE_CONNECTION).equals(StaticResources.STATE_DISCONNECTED)){
                        connectedToGatt = false;
                        connectionState.setTextColor(Color.RED);
                        connectionState.setText(intent.getStringExtra(StaticResources.EXTRA_STATE_CONNECTION));
                    }
                        break;
                case StaticResources.BROADCAST_CHARACTERISTIC_READ:
                    if(connectedToGatt = true){
                        String tempRead = intent.getStringExtra(StaticResources.EXTRA_TEMP_VALUE);
                        tempValue.setText(tempRead);
                        ///terminal.setText("");
                        ///terminal.setText(temp_value);
                    } else {
                        //hard coded
                        ///terminal.setText("No data available");
                    }
                    break;
                /*case StaticResources.BROADCAST_ESP32_INFO:
                    printOnTerminal(intent);
                    break;*/
            }
        }
    };

    //add an if that checks if the adaptor is connected to the GATT server already
    public void connectToGatt() {
        try {
            connectToGattServer.connectToGatt(deviceAddress);
            //hard coded
            connectionState.setTextColor(Color.YELLOW);
            connectionState.setText("Connecting...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void disconnectFromGatt() {
        connectToGattServer.disconnectGattServer();
        connectedToGatt = false;
        ///terminal.setText("");
        connectionState.setTextColor(Color.RED);
        connectionState.setText("Disconnected");
    }

    //this method prints all the info about the device connected to on a terminal-like TextView
    void printOnTerminal(Intent intent) {
        String serviceUUID = "Service UUID: " + intent.getStringExtra(StaticResources.EXTRA_TERMINAL_SERVICE);
        String charUUID = "Characteristics UUID: " + intent.getStringExtra(StaticResources.EXTRA_TERMINAL_CHARACTERISTIC_TEMP) + '\n' +
                                                     intent.getStringExtra(StaticResources.EXTRA_TERMINAL_CHARACTERISTIC_HEART);
        String toPrint = serviceUUID + '\n' + charUUID;
        ///terminal.setText(toPrint);
    }
}
