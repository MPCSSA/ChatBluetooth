package com.example.christian.chatbluetooth.view;


import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.text.Layout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Space;
import android.widget.TableLayout;
import android.widget.TextView;

import com.example.christian.chatbluetooth.R;
import com.example.christian.chatbluetooth.model.ChatMessage;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MessageAdapter extends ArrayAdapter<ChatMessage>{

    private int layout;
    private String address;

    public String getAddress() { return this.address; }
    public void setAddress(String address) { this.address = address; }

    public MessageAdapter(Context context, int resource) {
        super(context, resource);
        this.layout = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;

        if (view == null){
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(layout, parent, false);
        }

        int sender = getItem(position).getSender();
        LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.wrapper);

        TextView comment =  (TextView) view.findViewById(R.id.comment);
        TextView date = (TextView) view.findViewById(R.id.date);

        String str;

        if (sender == 1){
            str = "received ";
            linearLayout.setBackground(getContext().getResources().getDrawable(R.drawable.bubble_yellow));
            linearLayout.setGravity(Gravity.END);
            comment.setGravity(Gravity.END);
            date.setGravity(Gravity.END);
        }
        else{
            str = "sent ";
            linearLayout.setBackground(getContext().getResources().getDrawable(R.drawable.bubble_green));
            linearLayout.setGravity(Gravity.START);
            comment.setGravity(Gravity.START);
            date.setGravity(Gravity.START);
        }

        Date date1 = getItem(position).getDate();
        String when;
        long ago = date1.getTime() - (new Date()).getTime();

        if (ago < 86400000) {
            //this day
            when = "at " + (new SimpleDateFormat("HH:mm")).format(date1);

        }
        else if (ago < 172800000) {
            //yesterday
            when = "yesterday";
        }
        else if (ago < 604800000) {
            //this week
            when = "on " + (new SimpleDateFormat("EEE 'at' HH:mm")).format(date1);
        }
        else if (ago < 2629743830l) {
            //this month
            when = "on " + (new SimpleDateFormat("EEE dd")).format(date1);
        }
        else {
            //later on
            when = "on " + (new SimpleDateFormat("yy MM dd")).format(date1);
        }
        comment.setText(getItem(position).getMsg());
        date.setText(str + when);

        return view;
    }
}
