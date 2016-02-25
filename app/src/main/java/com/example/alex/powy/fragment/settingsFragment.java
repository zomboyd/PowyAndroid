package com.example.alex.powy.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.alex.powy.DeviceActivity;
import com.example.alex.powy.R;

import java.util.ArrayList;

public class settingsFragment extends Fragment implements View.OnClickListener {

    /**
     * Graphic
     */
    private ImageView bluetooth_button;
    private ImageView search_button;
    private ImageView server_button;
    private TextView mConnectionState;

    /**
     * Bluetooth objects
     */
    private boolean supported;
    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler = new Handler();
    private BluetoothLeScanner mBluetoothLeScanner;
    private BluetoothGatt mBluetoothGatt;

    private ArrayList<BluetoothDevice> mDeviceList = new ArrayList<>();
    private ArrayAdapter<String> mLeDeviceListAdapter;
    private boolean bluetoothState;
    private boolean searchState = false;


    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.view_settings, container, false);


        /**
         * verification of the SDK
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check 
            if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("This app needs location access");
                builder.setMessage("Please grant location access so this app can detect beacons.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                    }
                });
                builder.show();
            }
        }

        /**
         * verify if BLE is supported
         */
        if (!(supported = getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))) {
            Toast.makeText(getActivity(), "BLE not supported", Toast.LENGTH_SHORT).show();
            //getActivity().finish();
        }

        /**
         * setting the BluetoothAdapter and BluetoothLeScanner for API min 21
         */
        final BluetoothManager mBluetoothManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

        /**
         * verify if the bluetooth scanner is set
         */
        if (mBluetoothLeScanner == null) {
            Toast.makeText(getActivity(), "bluetooth adapter null", Toast.LENGTH_SHORT).show();
            //getActivity().finish();
        }

        //INIT VIEWABLE CONTENT
        /**
         * initialising the view in the fragment
         */
        mConnectionState = (TextView) v.findViewById(R.id.connection_state);
        bluetooth_button = (ImageView) v.findViewById(R.id.bluetooth);
        search_button = (ImageView) v.findViewById(R.id.search);
        server_button = (ImageView) v.findViewById(R.id.server);
        ListView listViewBluetooth = (ListView) v.findViewById(R.id.listViewBluetooth);


        mLeDeviceListAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item);
        listViewBluetooth.setAdapter(mLeDeviceListAdapter);

        listViewBluetooth.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BluetoothDevice bluetoothDevice = mDeviceList.get(position);

                final Intent intent = new Intent(getActivity(), DeviceActivity.class);
                intent.putExtra(DeviceActivity.EXTRAS_DEVICE_NAME, bluetoothDevice.getName());
                intent.putExtra(DeviceActivity.EXTRAS_DEVICE_ADDRESS, bluetoothDevice.getAddress());
                startActivity(intent);
            }
        });

        /**
         * Adding on click action to the buttons
         */
        initButton();

        return v;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bluetooth:
                if (bluetoothState) {
                    bluetoothState = false;
                    if (turnOff()) {
                        bluetooth_button.setImageResource(R.drawable.ic_bluetooth_disabled_24dp);
                    }
                } else {
                    bluetoothState = true;
                    if (turnOn()) {
                        bluetooth_button.setImageResource(R.drawable.ic_bluetooth_24dp);
                    }
                }
                break;
            case R.id.search: {
                if (searchState) {
                    searchState = false;
                    mLeDeviceListAdapter.clear();
                    mLeDeviceListAdapter.notifyDataSetChanged();
                    mBluetoothAdapter.cancelDiscovery();
                    if (mBluetoothGatt != null) {
                        mBluetoothGatt.close();
                        mBluetoothGatt = null;
                    }
                    search_button.setImageResource(R.drawable.ic_leak_remove_24dp);
                } else {
                    searchState = true;
                    mLeDeviceListAdapter.clear();
                    mLeDeviceListAdapter.notifyDataSetChanged();
                    scanLe(mBluetoothAdapter.enable());
                    search_button.setImageResource(R.drawable.ic_leak_add_24dp);
                }
                break;
            }
        }
    }

    public void initButton() {
        bluetooth_button.setOnClickListener(this);
        search_button.setOnClickListener(this);
        server_button.setOnClickListener(this);
        if (mBluetoothAdapter.isEnabled()) {
            bluetooth_button.setImageResource(R.drawable.ic_bluetooth_24dp);
            bluetoothState = true;
        } else {
            bluetooth_button.setImageResource(R.drawable.ic_bluetooth_disabled_24dp);
            bluetoothState = false;
        }
    }

    // TURN ON/OFF BLUETOOTH
    public boolean turnOn() {
        if (supported && !mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableIntent);
            return true;
        }
        return false;
    }

    public boolean turnOff() {
        Log.d("ONOFF", String.format("supported = %s , isEnabled = %s", supported, mBluetoothAdapter.isEnabled()));
        if (supported && mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.disable();
            return true;
        }
        return false;
    }


    /**
     * only supported in SDK 21+
     */
    ScanCallback mLeScanCallBack = new ScanCallback() {
        public void onScanResult(int callbackType, ScanResult result) {
            if (!mDeviceList.contains(result.getDevice())) {
                mLeDeviceListAdapter.add(String.format("%s", result.getDevice().getName()));
                mDeviceList.add(result.getDevice());
                Log.d("BLE", "device found -> " + result.getDevice().getName() + ":" + result.getDevice().getAddress());
                mLeDeviceListAdapter.notifyDataSetChanged();
            }
        }
    };

    // SEARCH LIST OF BLUETOOTH DEVICE
    public void scanLe(final boolean enable) {
        if (enable) {
            Log.d("SCAN", "in enable");
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBluetoothLeScanner.stopScan(mLeScanCallBack);
                }
            }, SCAN_PERIOD);
            mBluetoothLeScanner.startScan(mLeScanCallBack);
        } else {
            mBluetoothLeScanner.stopScan(mLeScanCallBack);
        }
    }

    private void updateConnectionState(final int resourceId) {
        Log.d("UPDATE", "connection state updated");
        mConnectionState = (TextView) getActivity().findViewById(R.id.connection_state);
        mConnectionState.setText(resourceId);
    }
}