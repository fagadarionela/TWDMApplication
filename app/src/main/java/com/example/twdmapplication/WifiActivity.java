package com.example.twdmapplication;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class WifiActivity extends AppCompatActivity implements View.OnClickListener {

    WifiManager wifi;
    int PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION = 1;
    List<ScanResult> results;

    Button buttonScan;
    ListView listOfNetworks;

    String ITEM_KEY = "key";
    ArrayList<HashMap<String, String>> arraylist = new ArrayList<>();
    SimpleAdapter adapter;

    int size = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonScan = findViewById(R.id.buttonScan);
        buttonScan.setOnClickListener(this);
        listOfNetworks = findViewById(R.id.list);

        wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (!wifi.isWifiEnabled()) {
            Toast.makeText(getApplicationContext(), "Wifi is disabled..making it enabled", Toast.LENGTH_LONG).show();
            wifi.setWifiEnabled(true);
        }
        this.adapter = new SimpleAdapter(this, arraylist, R.layout.row, new String[]{ITEM_KEY}, new int[]{R.id.list_value});
        listOfNetworks.setAdapter(this.adapter);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.CHANGE_WIFI_STATE) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.CHANGE_WIFI_STATE}, PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION);
        }
        registerReceiver(new BroadcastReceiver() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onReceive(Context c, Intent intent) {
                results = wifi.getScanResults();
                size = results.size();
                for (ScanResult item : results) {
                    System.out.println(item);
                }
                getScanningResults();
            }
        }, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        //do something, permission was previously granted; or legacy device
    }

    public void scan() {
        arraylist.clear();
        wifi.startScan();
        Toast.makeText(this, "Scanning....", Toast.LENGTH_SHORT).show();
    }

    public void getScanningResults() {
        arraylist.clear();
        if (size != 1) {
            Toast.makeText(getApplicationContext(), "Found " + size + " results", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Found " + size + " result", Toast.LENGTH_SHORT).show();
        }
        while (size > 0) {
            size--;
            HashMap<String, String> item = new HashMap<>();
            StringBuilder capabilities = new StringBuilder();
            for (String cap : results.get(size).capabilities.split("]")) {
                capabilities.append(cap.substring(1)).append(", ");
            }
            item.put(ITEM_KEY, "SSID: " + results.get(size).SSID + "\n" +
                    "BSSID: " + results.get(size).BSSID + "\n" +
                    "Level/RSSI: " + results.get(size).level + " dBm \n" +
                    "Frequency: " + results.get(size).frequency + " Hz \n" +
                    "Capabilities: " + capabilities.substring(0, capabilities.length() - 2) + "\n"
            );

            arraylist.add(item);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onClick(View v) {
        scan();
    }
}