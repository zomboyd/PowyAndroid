package com.example.alex.powy.fragment;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.alex.powy.R;
import com.example.alex.powy.bluetoothThread;
import com.example.alex.powy.controller.connectionController;

import java.util.ArrayList;

public class dashboardFragment extends Fragment implements View.OnClickListener {

    private static final int REQUEST_ENABLE_BT = 0;
    private BluetoothAdapter mBluetoothAdapter;
    private Button mButton;
    private View mView;
    private ArrayList<BluetoothDevice> mDeviceList;
    private ArrayAdapter<String> mArrayAdapter;
    private bluetoothThread mBluetoothThread;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mView = inflater.inflate(R.layout.view_dashboard, container, false);

        mDeviceList = new ArrayList<>();

        mArrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1);

        ListView listView = (ListView) mView.findViewById(R.id.list_bluetooth);
        listView.setAdapter(mArrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                connect(mDeviceList.get(position));

            }
        });

        mButton = (Button) mView.findViewById(R.id.startB);
        mButton.setOnClickListener(this);
        mButton = (Button) mView.findViewById(R.id.stopB);
        mButton.setOnClickListener(this);
        mButton = (Button) mView.findViewById(R.id.start_scan);
        mButton.setOnClickListener(this);
        return mView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.startB:
                turnOn();
                break;
            case R.id.stopB:
                turnOff();
                break;
            case R.id.start_scan:
                scan();
                break;
            case R.id.stop_co:
                stopCo();
        }
    }

    private void stopCo() {
        mBluetoothThread.cancel();
    }

    public void connect(BluetoothDevice device) {
        new bluetoothThread(device).run(mBluetoothAdapter);
        Log.d("CONN", "connect to " + device.getName());
    }

    public void scan() {
        final BroadcastReceiver mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                // When discovery finds a device
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Get the BluetoothDevice object from the Intent
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    // Add the name and address to an array adapter to show in a ListView
                    mDeviceList.add(device);
                    mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                    mArrayAdapter.notifyDataSetChanged();
                }
            }
        };
        mBluetoothAdapter.startDiscovery();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        getActivity().registerReceiver(mReceiver, filter);
    }

    public boolean turnOn() {
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
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
}