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
import android.widget.Space;
import android.widget.TableLayout;
import android.widget.TextView;

import com.example.christian.chatbluetooth.R;
import com.example.christian.chatbluetooth.model.ChatMessage;

public class MessageAdapter extends ArrayAdapter<ChatMessage>{

    private int layout;
    private String address;

    public MessageAdapter(Context context, int resource, String address) {
        super(context, resource);
        this.layout = resource;
        this.address = address;
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

        if (linearLayout == null){
            System.out.println("ciao");
        }
        TextView comment =  (TextView) view.findViewById(R.id.comment);
        TextView date = (TextView) view.findViewById(R.id.date);
        //Space space = new Space(getContext());
        //space.setLayoutParams(new TableLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1f));

        if (sender == 1){
            // remote user = sender
            //((LinearLayout) view.findViewById(R.id.listLayout)).addView(space, 1);
            linearLayout.setBackground(getContext().getResources().getDrawable(R.drawable.bubble_yellow));
            linearLayout.setGravity(Gravity.END);
            comment.setGravity(Gravity.END);
            date.setGravity(Gravity.END);
        }
        else{
            // this user = sender
            //((LinearLayout) view.findViewById(R.id.listLayout)).addView(space, 0);
            linearLayout.setBackground(getContext().getResources().getDrawable(R.drawable.bubble_green));
            linearLayout.setGravity(Gravity.START);
            comment.setGravity(Gravity.START);
            date.setGravity(Gravity.START);
        }

        comment.setText(getItem(position).getMsg());
        date.setText(getItem(position).getDate());

        return view;
    }

    public String getAddress(){
        return this.address;
    }
}
