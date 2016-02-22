package com.example.alex.powy.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
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
import java.util.UUID;

public class settingsFragment extends Fragment implements View.OnClickListener {

    //Graphic
    private ImageView bluetooth_button;
    private ImageView search_button;
    private ImageView server_button;

    //Basic Bluetooth stuff
    private boolean supported;
    //private LeDeviceListAdapter mLeDeviceListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler = new Handler();
    private BluetoothLeScanner mBluetoothLeScanner;
    private int mConnectionState = STATE_DISCONNECTED;
    private BluetoothGatt mBluetoothGatt;


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

    public final static UUID UUID_HEART_RATE_MEASUREMENT = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");


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

        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

        if (mBluetoothLeScanner == null) {
            Toast.makeText(getActivity(), "bluetooth adapter null", Toast.LENGTH_SHORT).show();
            //getActivity().finish();
        }

        mDeviceList = new ArrayList<>();

        //INIT VIEWABLE CONTENT
        bluetooth_button = (ImageView) v.findViewById(R.id.bluetooth);
        search_button = (ImageView) v.findViewById(R.id.search);
        server_button = (ImageView) v.findViewById(R.id.server);
        ListView listViewBluetooth = (ListView) v.findViewById(R.id.listViewBluetooth);

        //INIT LIST VIEW
        mLeDeviceListAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item);
        listViewBluetooth.setAdapter(mLeDeviceListAdapter);

        listViewBluetooth.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BluetoothDevice bluetoothDevice = mDeviceList.get(position);

                Log.d("CONN", "connecting to -> " + bluetoothDevice.getName());
                mBluetoothGatt = bluetoothDevice.connectGatt(getActivity(), false, mGattCallback);
                mBluetoothGatt.connect();
            }
        });

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
            case R.id.server: {
                if (serverState) {
                    serverState = false;
                    blueServ.cancel();
                    visibleOff();
                    server_button.setImageResource(R.drawable.ic_portable_wifi_off_24dp);
                } else {
                    serverState = true;
                    visibleOn();
                    blueServ = new BluetoothServer(mBluetoothAdapter);
                    blueServ.run();
                    server_button.setImageResource(R.drawable.ic_wifi_tethering_24dp);
                }
                break;
            }
        }
    }

    //INIT BUTTON
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

    ScanCallback mLeScanCallBack = new ScanCallback() {
        public void onScanResult(int callbackType, ScanResult result) {
            mLeDeviceListAdapter.add(String.format("%s", result.getDevice().getName()));
            mDeviceList.add(result.getDevice());
            Log.d("BLE", "device found -> " + result.getDevice().getName() + ":" + result.getDevice().getAddress());
            mLeDeviceListAdapter.notifyDataSetChanged();
        }
    };

    // SEARCH LIST OF BLUETOOTH DEVICE
    public void scanLe(final boolean enable) {
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

    private final BluetoothGattCallback mGattCallback =
            new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                                    int newState) {

                    Log.d("BLE", "on Connection State Change");

                    String intentAction;
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        Log.d("BLE", "on Connection State Change + State Connected");
                        intentAction = ACTION_GATT_CONNECTED;
                        mConnectionState = STATE_CONNECTED;
                        broadcastUpdate(intentAction);
                        Log.i("TOTO", "Connected to GATT server.");
                        Log.i("TOTO", "Attempting to start service discovery:" + mBluetoothGatt.discoverServices());

                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        Log.d("BLE", "on Connection State Change + state Disco");
                        intentAction = ACTION_GATT_DISCONNECTED;
                        mConnectionState = STATE_DISCONNECTED;
                        Log.i("TOTO", "Disconnected from GATT server.");
                        broadcastUpdate(intentAction);
                    }
                }

                @Override
                // New services discovered
                public void onServicesDiscovered(BluetoothGatt gatt, int status) {

                    Log.d("BLE", "on Services Discovered");

                    Log.w("TOTO", "onServicesDiscovered received: " + status);

                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                    } else {
                        Log.w("TOTO", "onServicesDiscovered received: " + status);
                    }
                }

                @Override
                // Result of a characteristic read operation
                public void onCharacteristicRead(BluetoothGatt gatt,
                                                 BluetoothGattCharacteristic characteristic,
                                                 int status) {

                    Log.d("BLE", "on Characteristic Read");

                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                    }
                }
            };

    public void sendBroadcast(BluetoothGattCharacteristic characteristic) {
        characteristic.setValue("?");

        boolean status = mBluetoothGatt.writeCharacteristic(characteristic);
        Log.d("STAT", "status d'envoi -> " + status);
    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        //sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        Log.d("BLE", "broadcast update");

        // This is special handling for the Heart Rate Measurement profile. Data
        // parsing is carried out as per profile specifications.
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            int flag = characteristic.getProperties();
            int format = -1;
            if ((flag & 0x01) != 0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16;
                Log.d("TOTO", "Heart rate format UINT16.");
            } else {
                format = BluetoothGattCharacteristic.FORMAT_UINT8;
                Log.d("TOTO", "Heart rate format UINT8.");
            }
            final int heartRate = characteristic.getIntValue(format, 1);
            Log.d("TOTO", String.format("Received heart rate: %d", heartRate));
            intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
        } else {
            // For all other profiles, writes the data formatted in HEX.
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for(byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));
                intent.putExtra(EXTRA_DATA, new String(data) + "\n" +
                        stringBuilder.toString());
            }
        }
        sendBroadcast(characteristic);
    }

}