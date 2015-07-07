package com.example.christian.chatbluetooth.view.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import com.example.christian.chatbluetooth.R;

import java.util.ArrayList;

public class EmoticonAdapter extends ArrayAdapter<Bitmap> {

    private int layout;

    public EmoticonAdapter(Context context, int resource) {
        super(context, resource);

        this.layout = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;
        if (v == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(layout, parent, false);
        }

        (v.findViewById(R.id.emoticon)).setBackground(new BitmapDrawable(getItem(position)));

        return v;
    }
}
