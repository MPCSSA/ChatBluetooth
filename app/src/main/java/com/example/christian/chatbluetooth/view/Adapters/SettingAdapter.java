package com.example.christian.chatbluetooth.view.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.example.christian.chatbluetooth.R;

/**
 * Created by christian on 05/07/15.
 */
public class SettingAdapter extends ArrayAdapter<String> {

    private int layout;

    public SettingAdapter(Context context, int resource) {
        super(context, resource);

        layout = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null){
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(layout, parent, false);
        }

        ((TextView) view.findViewById(R.id.set_item)).setText(getItem(position));

        return view;
    }


}
