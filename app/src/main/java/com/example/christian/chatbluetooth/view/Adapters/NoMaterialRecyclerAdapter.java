package com.example.christian.chatbluetooth.view.Adapters;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.christian.chatbluetooth.R;
import com.example.christian.chatbluetooth.controller.BlueCtrl;
import com.example.christian.chatbluetooth.model.ChatMessage;
import com.example.christian.chatbluetooth.model.ChatUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;


public class NoMaterialRecyclerAdapter extends ArrayAdapter<ChatUser> {

    private int layout;

    public NoMaterialRecyclerAdapter(Context context, int resource) {
        super(context, resource, BlueCtrl.userList);
        this.layout = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;

        if (view == null){
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            System.out.println("can");
            view = inflater.inflate(layout, parent, false);
        }


        TextView tv = (TextView) view.findViewById(R.id.tv_username);
        ChatUser user = getItem(position);
        String name = user.getName();
        if (name == null) name = "Unknown";
        tv.setText(name);

        return view;
    }
}
