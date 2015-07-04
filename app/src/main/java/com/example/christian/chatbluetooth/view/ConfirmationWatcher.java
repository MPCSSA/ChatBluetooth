package com.example.christian.chatbluetooth.view;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.example.christian.chatbluetooth.R;
import com.example.christian.chatbluetooth.controller.BlueCtrl;
import com.example.christian.chatbluetooth.view.Activities.MainActivity;

public class ConfirmationWatcher implements TextWatcher {

    private Context context;

    public ConfirmationWatcher(Context context) {

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

        boolean bool = ((EditText)((Activity) context).findViewById(R.id.et_reg_password)).getText().toString().equals(s.toString());

        if (bool)
            if (BlueCtrl.version) ((Activity) context).findViewById(R.id.iv_confirm).setBackground(context.getDrawable(R.mipmap.check));
            /*DEBUG ONLY*/else ((Activity) context).findViewById(R.id.iv_confirm).setBackgroundColor(0x8BC34A);
        else
            if (BlueCtrl.version) ((Activity) context).findViewById(R.id.iv_confirm).setBackground(context.getDrawable(R.mipmap.close));
            /*DEBUG ONLY*/else ((Activity) context).findViewById(R.id.iv_confirm).setBackgroundColor(0xF44336);

        ((MainActivity)context).setOkConf(bool);
    }
}
