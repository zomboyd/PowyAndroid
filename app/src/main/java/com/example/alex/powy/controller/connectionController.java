package com.example.alex.powy.controller;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Intent;

/**
 * Created by alex on 16/02/16.
 */
public class connectionController extends Activity{

    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    boolean isSupported;
    Activity mActivity;


    public connectionController(Activity activity){
        mActivity = activity;
        mBluetoothAdapter  = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            isSupported = false;
        }
        else
            isSupported = true;
    }

    public boolean turnOn() {
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
        return false;
    }

    public boolean getIsSupported() {
        return isSupported;
    }

}