package com.example.alex.powy.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.alex.powy.R;
import com.example.alex.powy.controller.connectionController;

public class dashboardFragment extends Fragment {

    private connectionController mConnectionController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mConnectionController = new connectionController(getActivity());
        return inflater.inflate(R.layout.view_dashboard, container, false);
    }

    public void ButtonOnClick(View v) {
        switch (v.getId()) {
            case R.id.startB:
                mConnectionController.turnOn();
                break;
            case R.id.stopB:
                mConnectionController.turnOff();
                break;
        }
    }
}