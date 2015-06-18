package com.example.christian.chatbluetooth.view;

import android.app.Activity;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.christian.chatbluetooth.R;


public class RegistrationFragment extends Fragment {

    private View.OnClickListener listener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_registration, container, false);

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
