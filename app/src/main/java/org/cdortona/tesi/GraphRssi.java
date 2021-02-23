package org.cdortona.tesi;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewParent;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

/**
 * Cristian D'Ortona
 *
 * TESI DI LAUREA IN INGEGNERIA ELETTRONICA E DELLE TELECOMUNICAZIONI
 *
 */


public class GraphRssi extends AppCompatActivity {

    static final String TAG = "GraphRssi";

    //Toolbar
    Toolbar toolbar;

    //Graph
    LineGraphSeries<DataPoint> series;
    int xAxis = 0;

    //BLE
    BluetoothManager manager;
    BluetoothAdapter adapter;
    BluetoothDevice deviceToGetRssiFrom;
    //I need to instantiate a scanner object which I'll use to start a new scan
    //The phone will constantly scan for nearby devices with the sole purpose of gathering RSSI info of the selected device
    BluetoothLeScanner bluetoothLeScanner;
    String deviceAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph_rssi);

        //Toolbar
        toolbar = findViewById(R.id.toolbar_graph_rssi);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Graph
        GraphView graph = findViewById(R.id.graph);
        series = new LineGraphSeries<>();
        graph.addSeries(series);
        Viewport viewport = graph.getViewport();
        viewport.setYAxisBoundsManual(true);
        viewport.setMinX(0);
        viewport.setMinY(0);
        viewport.setScrollable(true);

        //BLE
        manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        adapter = manager.getAdapter();
        Intent intent = getIntent();
        deviceToGetRssiFrom = adapter.getRemoteDevice(intent.getStringExtra(StaticResources.EXTRA_CHOOSEN_ADDRESS));
        bluetoothLeScanner = adapter.getBluetoothLeScanner();
        bluetoothLeScanner.startScan(leScanCallBack);
    }


    //callBack for the scanning process
    ScanCallback leScanCallBack = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            valueToGraph(result);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.w(TAG, "Scan failed with errorCode: " + errorCode);
        }
    };

    void valueToGraph(ScanResult result){
        /*try {
            Thread.sleep(500);
        } catch (InterruptedException e){
            e.printStackTrace();
        }*/
        Log.d(TAG, "Result from scan: " + result.getDevice().getAddress() + ", chosen address: " + deviceAddress);
        if(result.getDevice().getAddress().equals(deviceAddress)){
            Log.d(TAG, "Device found: " + result.getDevice().getName() + ", updating RSSI");
            series.appendData(new DataPoint(xAxis,result.getRssi()), true, 20);
        }
    }

    //Toolbar
    public boolean onSupportNavigateUp() {
        //this is called when the activity detects the user pressed the back button
        onBackPressed();
        return true;
    }

    //I'm overriding this since I want to stop the scan as the user navigates up
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        bluetoothLeScanner.stopScan(leScanCallBack);
    }
}
