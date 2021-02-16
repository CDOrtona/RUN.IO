package org.cdortona.tesi;

import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
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
    TextView addressInfo;
    TextView nameInfo;
    TextView terminal;
    TextView temp_value;
    TextView connectionState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensors_info);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        //UI setup
        addressInfo = findViewById(R.id.address_textView);
        nameInfo = findViewById(R.id.name_textView);
        terminal = findViewById(R.id.terminal_textView);
        temp_value = findViewById(R.id.temp_textView);
        connectionState = findViewById(R.id.connection_state_textView);

        //I have to retrieve the info from the Intent which called this activity
        Intent receivedIntent = getIntent();
        deviceAddress = receivedIntent.getStringExtra("address");
        deviceName = receivedIntent.getStringExtra("name");

        //Setting the values of the TextViews objects
        addressInfo.setText(deviceAddress);
        nameInfo.setText(deviceName);
        terminal.setText("");
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
                        String temp_value = intent.getStringExtra(StaticResources.EXTRA_TEMP_VALUE);
                        terminal.setText("");
                        terminal.setText(temp_value);
                    } else {
                        //hard coded
                        terminal.setText("No data available");
                    }
                    break;
                case StaticResources.BROADCAST_ESP32_INFO:
                    printOnTerminal(intent);
                    break;
            }
        }
    };

    //add an if that checks if the adaptor is connected to the GATT server already
    public void connectToGatt(View v) {
        try {
            connectToGattServer.connectToGatt(deviceAddress);
            //hard coded
            connectionState.setTextColor(Color.YELLOW);
            connectionState.setText("Connecting...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void disconnectFromGatt(View v) {
        connectToGattServer.disconnectGattServer();
        connectedToGatt = false;
        terminal.setText("");
        connectionState.setTextColor(Color.RED);
        connectionState.setText("Disconnected");
    }

    //this method prints all the info about the device connected to on a terminal-like TextView
    void printOnTerminal(Intent intent) {
        String serviceUUID = "Service UUID: " + intent.getStringExtra(StaticResources.EXTRA_TERMINAL_SERVICE);
        String charUUID = "Characteristics UUID: " + intent.getStringExtra(StaticResources.EXTRA_TERMINAL_CHARACTERISTIC_TEMP) + '\n' +
                                                     intent.getStringExtra(StaticResources.EXTRA_TERMINAL_CHARACTERISTIC_HEART);
        String toPrint = serviceUUID + '\n' + charUUID;
        terminal.setText(toPrint);
    }
}
