package com.example.alex.powy.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.alex.powy.R;
import com.example.alex.powy.controller.TransformBMP;
import com.squareup.picasso.Picasso;

/**
 * Created by alex on 15/02/16.
 */
public class ownerFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.view_owner, container, false);
        ImageView profil = (ImageView) v.findViewById(R.id.profilPicture);
        Picasso.with(getContext()).load(R.drawable.profil_pic).transform(new TransformBMP()).into(profil);
        return v;
    }

}
