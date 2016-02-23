package com.example.alex.powy.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
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
import android.widget.Toast;

import com.example.alex.powy.R;
import com.example.alex.powy.service.BluetoothLeService;
import com.example.alex.powy.thread.BluetoothServer;

import java.util.ArrayList;
import java.util.Arrays;

public class settingsFragment extends Fragment implements View.OnClickListener {

    //OS
    Integer os_version;

    //Graphic
    private ImageView bluetooth_button;
    private ImageView search_button;

    //Basic Bluetooth stuff
    private boolean supported;
    //private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler = new Handler();
    private BluetoothLeScanner mBluetoothLeScanner;
    private int mConnectionState = STATE_DISCONNECTED;

    //private Void mLeScanCallBack;

    //SERVER BLUETOOTH STUFF
    private boolean serverState = false;
    private BluetoothServer blueServ;

    //ListView Bluetooth stuff
    private ArrayList<BluetoothDevice> mDeviceList;
    private BroadcastReceiver mReceiver;
    private ArrayAdapter<String> mLeDeviceListAdapter;
    private boolean bluetoothState;
    private boolean searchState = false;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    private static final int REQUEST_ENABLE_BT = 1;
    // Stops scanning after 10 seconds.
    private static final long SCAN_PERIOD = 10000;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.view_settings, container, false);

        os_version = android.os.Build.VERSION.SDK_INT;

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

        if (!(supported = getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))) {
            Toast.makeText(getActivity(), "BLE not supported", Toast.LENGTH_SHORT).show();
            //getActivity().finish();
        }

        final BluetoothManager mBluetoothManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();


        mDeviceList = new ArrayList<>();

        //INIT VIEWABLE CONTENT
        bluetooth_button = (ImageView) v.findViewById(R.id.bluetooth);
        search_button = (ImageView) v.findViewById(R.id.search);
        ListView listViewBluetooth = (ListView) v.findViewById(R.id.listViewBluetooth);

        //INIT LIST VIEW
        mLeDeviceListAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item);
        listViewBluetooth.setAdapter(mLeDeviceListAdapter);

        ListView listView = (ListView) getActivity().findViewById(R.id.listViewBluetooth);
        // listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        //  @Override
        //    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //       BluetoothDevice bluetoothDevice = mDeviceList.get(position);

        //bluetoothDevice.connectGatt(getActivity(), false, new BluetoothLeService());
        //   }
        //});

        //INIT BUTTONS
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
                    //if (os_version >= 21) {
                    //    mBluetoothLeScanner.stopScan(mLeScanCallBack);
                    //}
                    mDeviceList.clear();
                    mLeDeviceListAdapter.clear();
                    mLeDeviceListAdapter.notifyDataSetChanged();
                    mBluetoothAdapter.cancelDiscovery();
                    search_button.setImageResource(R.drawable.ic_leak_remove_24dp);
                } else {
                    searchState = true;
                    if (mBluetoothLeScanner == null || !mBluetoothAdapter.isEnabled()) {
                        Toast.makeText(getActivity(), "Bluetooth Adapter null", Toast.LENGTH_SHORT).show();
                    }
                    mDeviceList.clear();
                    mLeDeviceListAdapter.clear();
                    mLeDeviceListAdapter.notifyDataSetChanged();
                    chooseScan(mBluetoothAdapter.enable());
                    search_button.setImageResource(R.drawable.ic_leak_add_24dp);
                }
                break;
            }
        }
    }

    //INIT BUTTON
    public void initButton() {
        bluetooth_button.setOnClickListener(this);
        search_button.setOnClickListener(this);
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
            Intent enableIntent = new Intent(mBluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
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

    // SET DEVICE DISCOVER OR NOT
    public boolean visibleOn() {
        Log.d("ONOFF", String.format("supported = %s , isEnabled = %s", supported, mBluetoothAdapter.isEnabled()));
        if (supported && mBluetoothAdapter.isEnabled()) {
            Intent getVisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            startActivity(getVisible);
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

    // SEARCH LIST OF BLUETOOTH DEVICE FOR API 21+
    public void scanLe(final boolean enable) {

        final ScanCallback mLeScanCallBack = new ScanCallback() {
            public void onScanResult(int callbackType, ScanResult result) {
                if (mDeviceList.indexOf(result.getDevice()) == -1) {
                    if (result.getDevice().getName() == null) {
                        mLeDeviceListAdapter.add(String.format("%s", "Unknown Device"));
                    } else {
                        mLeDeviceListAdapter.add(String.format("%s", result.getDevice().getName()));
                    }
                    mDeviceList.add(result.getDevice());
                    Log.d("BLE", "device found -> " + result.getDevice().toString() + " " + result.getDevice().getName());
                    mLeDeviceListAdapter.notifyDataSetChanged();
                }
            }
        };

        if (enable) {
            Log.d("SCAN", "in enable");
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothLeScanner.stopScan(mLeScanCallBack);
                }
            }, SCAN_PERIOD);
            mScanning = true;
            mBluetoothLeScanner.startScan(mLeScanCallBack);
        } else {
            mScanning = false;
            mBluetoothLeScanner.stopScan(mLeScanCallBack);
        }
    }

    //SCAN API UP 18/20
    // SEARCH LIST OF BLUETOOTH for API 18-20
    public void scanLE_KK(final boolean enable) {

        final BluetoothAdapter.LeScanCallback LeScanCallBack = new BluetoothAdapter.LeScanCallback() {
            public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mDeviceList.indexOf(device) == -1) {
                            if (device.getName() == null) {
                                mLeDeviceListAdapter.add(String.format("%s", "Unknown Device"));
                            } else {
                                mLeDeviceListAdapter.add(String.format("%s", device.getName()));
                            }
                            mDeviceList.add(device);
                            Log.d("BLE", "device found -> " + device.toString() + " " + device.getName());
                            mLeDeviceListAdapter.notifyDataSetChanged();
                        }
                    }
                });
            }
        };

        if (enable) {
            Log.d("SCAN", "in enable");
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.stopLeScan(LeScanCallBack);
                }
            }, SCAN_PERIOD);
            mScanning = true;
            mBluetoothAdapter.startLeScan(LeScanCallBack);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(LeScanCallBack);
        }
    }

    public void chooseScan(final boolean enable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Log.d("TEST", "LOLLIPOP +");
            if (mBluetoothLeScanner == null) {
                mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
            }
            scanLe(mBluetoothAdapter.enable());
        } else {
            Log.d("TEST", "KITKAT");
            scanLE_KK(mBluetoothAdapter.enable());
        }
    }

}