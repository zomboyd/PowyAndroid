package com.example.alex.powy.fragment;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.example.alex.powy.R;
import com.example.alex.powy.bluetoothServer;
import com.example.alex.powy.thread.BluetoothServer;

public class settingsFragment extends Fragment implements View.OnClickListener {

    //Graphic
    private ImageView bluetooth_button;
    private ImageView search_button;
    private ImageView server_button;
    private ListView listViewBluetooth;

    //Basic Bluetooth stuff
    private boolean supported;
    private BluetoothAdapter mBluetoothAdapter;

    //SERVER BLUETOOTH STUFF
    private boolean serverState = false;
    private BluetoothServer blueServ;

    //ListView Bluetooth stuff
    private BroadcastReceiver mReceiver;
    private ArrayAdapter<String> adapter;
    private boolean bluetoothState;
    private boolean searchState = false;

    //SERVER
    Intent intent;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.view_settings, container, false);

        //Bluetooth state
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        supported = mBluetoothAdapter != null;

        //INIT VIEWABLE CONTENT
        bluetooth_button = (ImageView) v.findViewById(R.id.bluetooth);
        search_button = (ImageView) v.findViewById(R.id.search);
        server_button = (ImageView) v.findViewById(R.id.server);
        listViewBluetooth = (ListView) v.findViewById(R.id.listViewBluetooth);

        //INIT LIST VIEW
        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item);
        listViewBluetooth.setAdapter(adapter);

        //SERVER BLUETOOTH
        //visibleOn();
        //blueServ = new BluetoothServer(mBluetoothAdapter);
        //blueServ.run();

        //INIT BUTTONS
        initButton();

        //TEST BLUETOOTH SERVER
        intent = new Intent(getActivity(), bluetoothServer.class);

        return v;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bluetooth:
                if (bluetoothState == true) {
                    bluetoothState = false;
                    turnOff();
                    bluetooth_button.setImageResource(R.drawable.ic_bluetooth_disabled_24dp);
                } else if (bluetoothState == false) {
                    bluetoothState = true;
                    turnOn();
                    bluetooth_button.setImageResource(R.drawable.ic_bluetooth_24dp);
                }
                break;
            case R.id.search: {
                if (searchState == true) {
                    searchState = false;
                    adapter.clear();
                    adapter.notifyDataSetChanged();
                    mBluetoothAdapter.cancelDiscovery();
                    getActivity().unregisterReceiver(mReceiver);
                    search_button.setImageResource(R.drawable.ic_leak_remove_24dp);
                } else if (searchState == false) {
                    searchState = true;
                    adapter.clear();
                    adapter.notifyDataSetChanged();
                    discoverable();
                    search_button.setImageResource(R.drawable.ic_leak_add_24dp);
                }
                break;
            }
            case R.id.server: {
                if (serverState == true) {
                    serverState = false;
                    //blueServ.cancel();
                    //visibleOff();
                    server_button.setImageResource(R.drawable.ic_portable_wifi_off_24dp);
                } else if (serverState == false) {
                    serverState = true;
                    startActivity(intent);
                    //coucou = new BluetoothServerActivity();
                    //startActivity(coucou.getIntent());
                    //visibleOn();
                    //blueServ = new BluetoothServer(mBluetoothAdapter);
                    //blueServ.run();
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

    //CHECK IF BLUETOOTH IS SUPPORTED BY THE DEVICE
    public boolean getIsSupported() {
        return supported;
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
        if (supported && mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.disable();
            return true;
        }
        return false;
    }

    // SET DEVICE DISCOVER OR NOT
    public boolean visibleOn() {
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

    // SEARCH LIST OF BLUETOOTH DEVICE
    public void discoverable() {
        mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                // When discovery finds a device
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    // Add the name and address to an array adapter to show in a ListView
                    if (device.getName() == null) {
                        adapter.add("Unknown Device: " + device.getAddress());
                        Log.d("TEST", "Unknown Device: " + device.getAddress() + " \n " + device.getUuids());
                    }
                    else {
                        adapter.add(device.getName() + ": " + device.getAddress());
                        Log.d("TEST", device.getName() + ": " + device.getAddress() + " \n " + device.getUuids());
                    }
                    adapter.notifyDataSetChanged();
                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                    adapter.clear();
                    adapter.notifyDataSetChanged();
                    Log.d("Bluetooth", "relauch");
                    mBluetoothAdapter.startDiscovery();
                }
            }
        };
        mBluetoothAdapter.startDiscovery();
        // Register the BroadcastReceiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        getActivity().registerReceiver(mReceiver, filter);
    }
}