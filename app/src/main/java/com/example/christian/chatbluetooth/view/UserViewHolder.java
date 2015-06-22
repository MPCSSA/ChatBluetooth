package com.example.christian.chatbluetooth.view;


import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.christian.chatbluetooth.R;

public class UserViewHolder extends RecyclerView.ViewHolder{

    protected TextView name;

    public UserViewHolder(View v){
        super(v);

        name = (TextView) v.findViewById(R.id.title);
    }
}
