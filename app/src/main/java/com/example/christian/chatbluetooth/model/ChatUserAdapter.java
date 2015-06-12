package com.example.christian.chatbluetooth.model;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


public class ChatUserAdapter extends ArrayAdapter<ChatUser> {

    public ChatUserAdapter(Context context, int resource) {
        super(context, resource);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ChatUser chatUser = getItem(position);

        //TODO: building view from chatUser informations

        return  convertView;
    }

}
