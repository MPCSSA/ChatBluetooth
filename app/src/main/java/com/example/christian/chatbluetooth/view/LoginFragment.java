package com.example.christian.chatbluetooth.view;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.christian.chatbluetooth.R;

public class LoginFragment extends Fragment {

    private View.OnClickListener listener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof View.OnClickListener) listener = (View.OnClickListener) activity;
        else listener = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_login, container, false);

        Button btn = (Button) view.findViewById(R.id.reg_btn);
        btn.setOnClickListener(listener);

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
