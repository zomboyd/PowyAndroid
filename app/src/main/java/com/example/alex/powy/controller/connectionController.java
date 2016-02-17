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


public class connectionController extends Activity {

    private static final int REQUEST_ENABLE_BT = 0;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    boolean isSupported;
    Context mContext;


    public connectionController(Context context){
        mContext = context;
        mBluetoothAdapter  = BluetoothAdapter.getDefaultAdapter();
        isSupported = mBluetoothAdapter != null;
    }

    public boolean turnOn() {
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            mContext.startActivity(enableIntent);
        }
        return false;
    }

    public boolean turnOff() {
        if (mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.disable();
            return true;
        }
        return false;
    }

    public boolean visibleOn() {
        if (mBluetoothAdapter.isEnabled()) {
            Intent getVisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            mContext.startActivity(getVisible);
        }
        return false;
    }

    public boolean visibleOff() {
        if (mBluetoothAdapter.isDiscovering() && mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.cancelDiscovery();
            return true;
        }
        return false;
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a ListView
                //mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        }
    };
    // Register the BroadcastReceiver
    IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
    //registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy

    public boolean getIsSupported() {
        return isSupported;
    }

}