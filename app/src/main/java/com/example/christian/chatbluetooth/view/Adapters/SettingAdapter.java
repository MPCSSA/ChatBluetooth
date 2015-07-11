package com.example.christian.chatbluetooth.view.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TextView;

import com.example.christian.chatbluetooth.R;
import com.example.christian.chatbluetooth.view.Activities.SettingActivity;


public class SettingAdapter extends ArrayAdapter<String> {

    private int layout;

    public SettingAdapter(Context context, int resource) {
        super(context, resource);

        layout = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view;

        if (position < 3) {

            view = inflater.inflate(layout, parent, false);
            ((TextView) view.findViewById(R.id.set_item)).setText(getItem(position));

            if (position == 0) {
                view.findViewById(R.id.visible_cb).setEnabled(false);
                view.findViewById(R.id.visible_cb).setVisibility(View.INVISIBLE);
            }
        }
        else {

            view = inflater.inflate(R.layout.item_gender, parent, false);
            if (((SettingActivity)getContext()).gender == 1)
                ((RadioButton) view.findViewById(R.id.male_settings)).setChecked(true);
            else if (((SettingActivity)getContext()).gender == 2)
                ((RadioButton) view.findViewById(R.id.fem_settings)).setChecked(true);
        }

        if (position > 0) ((CheckBox) view.findViewById(R.id.visible_cb)).setChecked(((SettingActivity)getContext()).checked[position - 1]);

        return view;
    }
}
