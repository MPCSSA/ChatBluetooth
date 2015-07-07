package com.example.christian.chatbluetooth.view.Adapters;


import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Space;
import android.widget.TableLayout;
import android.widget.TextView;

import com.example.christian.chatbluetooth.R;
import com.example.christian.chatbluetooth.controller.BlueCtrl;
import com.example.christian.chatbluetooth.model.ChatMessage;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MessageAdapter extends ArrayAdapter<ChatMessage>{

    private String address; //MAC address of user whose history is currently shown

    public String getAddress() { return this.address; }
    public void setAddress(String address) { this.address = address; }

    public MessageAdapter(Context context, int resource) {
        super(context, resource);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (position == 0 && getItem(position) == null) {
            //Special view that shows older messages when pressed; it has its own layout and disappears when
            //there are no more messages to show

            int layout = (BlueCtrl.version) ? R.layout.item_more_history : R.layout.item_more_history_nomat;
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(layout, parent, false);

            return view;
        }

        View view;
        ChatMessage message = getItem(position);

        int layout = (BlueCtrl.version) ? R.layout.listitem_discuss : R.layout.listitem_discuss_nomat;
        int item = (message.isEmo()) ? R.layout.item_emoticon : layout; //is it a message or an emoticon?
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(item, parent, false);

        TextView date = (TextView) view.findViewById(R.id.date); //message timestamp

        if (message.isEmo()) {
            //Emoticons are shown in their own balloons; they require a specific layout containing an ImageView

            LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.wrapper);
            int i = Integer.parseInt(message.getMsg());
            //Emoticon code; it is its position in the GridView inside the PopupWindow; drawable resource contains a
            //5x5 emoticons table, the right one is in (code MOD 5) row and (code / 5) column

            if (message.getSender()) {
                //Emoticon sent by other user

                linearLayout.setBackground(getContext().getResources().getDrawable(R.drawable.white_bubbles));
                linearLayout.setGravity(Gravity.END);

                Bitmap emoBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.red_emoticons);
                //Other users balloons are white, therefore a red emoticon is picked from the right drawable resource
                emoBitmap.setDensity(DisplayMetrics.DENSITY_HIGH);
                int w = emoBitmap.getWidth() / 5, h = emoBitmap.getHeight() / 5;

                ImageView emoticon = (ImageView) view.findViewById(R.id.emoticon);
                emoticon.setBackground(new BitmapDrawable(Bitmap.createBitmap(emoBitmap, (i % 5) * w, (i / 5) * h, w, h)));
                //Crop the right emoticon

                date.setGravity(Gravity.END);
                if (BlueCtrl.version)
                    date.setTextColor(getContext().getResources().getColor(R.color.divider));

                ((RelativeLayout) view.findViewById(R.id.listLayout)).setGravity(Gravity.START);
                //shift balloon to the left and its content to the right
            }
            else {
                //emoticon sent by this device

                linearLayout.setBackground(getContext().getResources().getDrawable(R.drawable.red_bubbles));
                linearLayout.setGravity(Gravity.START);

                Bitmap emoBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.white_emoticons);
                //This device balloons are red, therefore a white emoticon is picked from the right drawable resource
                emoBitmap.setDensity(DisplayMetrics.DENSITY_HIGH);
                int w = emoBitmap.getWidth() / 5, h = emoBitmap.getHeight() / 5;

                ImageView emoticon = (ImageView) view.findViewById(R.id.emoticon);
                emoticon.setBackground(new BitmapDrawable(Bitmap.createBitmap(emoBitmap, (i % 5) * w, (i / 5) * h, w, h)));
                //Crop the right emoticon

                date.setGravity(Gravity.START);
                if (BlueCtrl.version)
                    date.setTextColor(getContext().getResources().getColor(R.color.divider));

                ((RelativeLayout) view.findViewById(R.id.listLayout)).setGravity(Gravity.END);
                //shift balloon to the right and its content to the left
            }

        }

        else {
            //Text message

            LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.wrapper);

            TextView comment = (TextView) view.findViewById(R.id.comment);

            if (getItem(position).getSender()) {
                //message sent by other user

                linearLayout.setBackground(getContext().getResources().getDrawable(R.drawable.white_bubbles));
                linearLayout.setGravity(Gravity.END);

                comment.setGravity(Gravity.END);
                if (BlueCtrl.version)
                    comment.setTextColor(getContext().getResources().getColor(R.color.background));

                date.setGravity(Gravity.END);
                if (BlueCtrl.version)
                    date.setTextColor(getContext().getResources().getColor(R.color.divider));

                ((RelativeLayout) view.findViewById(R.id.listLayout)).setGravity(Gravity.START);
                //shift balloon to the left and its content to the right
            }
            else {
                //message sent by this device

                linearLayout.setBackground(getContext().getResources().getDrawable(R.drawable.red_bubbles));
                //99 Luftballons!
                linearLayout.setGravity(Gravity.START);

                comment.setGravity(Gravity.START);
                if (BlueCtrl.version)
                    comment.setTextColor(getContext().getResources().getColor(R.color.accent));

                date.setGravity(Gravity.START);
                if (BlueCtrl.version)
                    date.setTextColor(getContext().getResources().getColor(R.color.divider));

                ((RelativeLayout) view.findViewById(R.id.listLayout)).setGravity(Gravity.END);
                //shift balloon to the left and its content to the right
            }

            comment.setText(getItem(position).getMsg());
            //write message into balloon
        }

        String str = (message.getSender()) ? "received " : "sent ";
        //building up message timestamp

        Date date1 = getItem(position).getDate();
        String when;
        long ago = date1.getTime() - (new Date()).getTime();
        //timestamp gets a different format depending on how old is the message

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

        date.setText(str + when);

        return view;
    }
}
