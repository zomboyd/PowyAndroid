package com.example.alex.powy.controller;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;


public class connectionController extends Activity {

    private static final int REQUEST_ENABLE_BT = 0;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private ArrayList<String> disco = new ArrayList<>();
    boolean supported;
    Context mContext;


    public connectionController(Context context){
        mContext = context;
        mBluetoothAdapter  = BluetoothAdapter.getDefaultAdapter();
        supported = mBluetoothAdapter != null;
    }

    public boolean turnOn() {
        if (supported && !mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            mContext.startActivity(enableIntent);
            return true;
        }
        return false;
    }

    public boolean turnOff() {
        if (supported && mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.disable();
            return true;
        }
        return false;
    }

    public boolean visibleOn() {
        if (supported && mBluetoothAdapter.isEnabled()) {
            Intent getVisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            mContext.startActivity(getVisible);
            return true;
        }
        return false;
    }

    public boolean visibleOff() {
        if (supported && mBluetoothAdapter.isDiscovering() && mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.cancelDiscovery();
            return true;
        }
        return false;
    }

    public void discoverable() {
        final BroadcastReceiver mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                // When discovery finds a device
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    // Add the name and address to an array adapter to show in a ListView
                    disco.add(device.getName() + "\n" + device.getAddress());
                }
            }
        };
        mBluetoothAdapter.startDiscovery();
        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        mContext.registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
        //mContext.unregisterReceiver(mReceiver);
    }

    public ArrayList<String> getDiscoverable() {
        return disco;
    }

    public boolean getIsSupported() {
        return supported;
    }

}