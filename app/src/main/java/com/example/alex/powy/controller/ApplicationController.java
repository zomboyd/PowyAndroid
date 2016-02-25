package com.example.alex.powy.controller;

import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.example.alex.powy.DeviceActivity;
import com.example.alex.powy.service.BluetoothLeService;

/**
 * Created by alex on 15/02/16.
 */
public class ApplicationController extends Application {

    private final static String TAG = DeviceActivity.class.getSimpleName();

    private String DeviceAddress;
    private Intent serviceIntent;
    private BluetoothLeService mBluetoothLeService;
    private static ApplicationController sInstance;


    /**
     *manage the service live circle
     */
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(DeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        // initialize the singleton
        sInstance = this;
    }

    /**
     * @return ApplicationController singleton instance
     */
    public static synchronized ApplicationController getInstance() {
        return sInstance;
    }

    /**
     * set the deviceAddress var
     * @param deviceAddress
     */
    public void setDeviceAddress(String deviceAddress) {
        DeviceAddress = deviceAddress;
    }

    /**
     * @return serviceIntent value
     */
    public Intent getServiceIntent() {
        return serviceIntent;
    }

    /**
     * set the serviceIntent value
     * @param serviceIntent
     */
    public void setServiceIntent(Intent serviceIntent) {
        this.serviceIntent = serviceIntent;
    }
}
