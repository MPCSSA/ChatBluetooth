package com.example.christian.chatbluetooth.view;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;

import com.example.christian.chatbluetooth.R;
import com.example.christian.chatbluetooth.view.Activities.MainActivity;

public class PasswordWatcher implements TextWatcher {

    private Context context;

    public PasswordWatcher(Context context) {
        this.context = context;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {


    }

    @Override
    public void afterTextChanged(Editable s) {

        boolean bool = s.toString().length() < 4;
        if (bool) {
            ((Activity)context).findViewById(R.id.et_confirm).setEnabled(false);
            ((Activity)context).findViewById(R.id.et_confirm).setAlpha(0.5f);
        }

        else {
            ((Activity)context).findViewById(R.id.et_confirm).setEnabled(true);
            ((Activity)context).findViewById(R.id.et_confirm).setAlpha(1);
        }

        ((MainActivity)context).setOkPass(bool);
    }
}
