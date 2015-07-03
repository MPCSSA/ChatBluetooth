package com.example.christian.chatbluetooth.view;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.example.christian.chatbluetooth.R;
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
            ((Activity) context).findViewById(R.id.iv_confirm).setBackground(context.getDrawable(R.mipmap.check));
        else
            ((Activity) context).findViewById(R.id.iv_confirm).setBackground(context.getDrawable(R.mipmap.close));

        ((MainActivity)context).setOkConf(bool);
    }
}
