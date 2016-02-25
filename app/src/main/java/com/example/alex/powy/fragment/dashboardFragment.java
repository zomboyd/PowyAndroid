package com.example.alex.powy.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.alex.powy.R;
import com.example.alex.powy.service.BluetoothLeService;

/**
 * Created by alex on 15/02/16.
 */
public class dashboardFragment extends Fragment {

    Intent intent = new Intent(getActivity(), BluetoothLeService.class);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.view_dashboard, container, false);
        Button button = (Button) v.findViewById(R.id.button2);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.button2)
                    getActivity().startService(intent);
                else if (v.getId() == R.id.button3)
                    getActivity().stopService(intent);
            }
        });
        return v;
    }



}
