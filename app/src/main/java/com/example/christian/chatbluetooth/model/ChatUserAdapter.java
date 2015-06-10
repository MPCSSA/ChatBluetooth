package com.example.christian.chatbluetooth.model;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

/**
 * Created by stefano on 10/06/15.
 */
public class ChatUserAdapter extends ArrayAdapter<ChatUser> {

    public ChatUserAdapter(Context context, int resource) {
        super(context, resource);
    }

    @Override
    public void clear() {
        super.clear();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return super.getView(position, convertView, parent);
    }
}
