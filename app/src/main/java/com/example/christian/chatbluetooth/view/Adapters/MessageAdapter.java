package com.example.christian.chatbluetooth.view.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.christian.chatbluetooth.R;
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

            int layout = R.layout.item_more_history;
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            return inflater.inflate(layout, parent, false);
        }

        View view;
        ChatMessage message = getItem(position);

        int item = (message.isEmo()) ? R.layout.item_emoticon : R.layout.listitem_discuss; //is it a message or an emoticon?
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(item, parent, false);
        //inflate view

        TextView date = (TextView) view.findViewById(R.id.date); //message timestamp

        if (message.isEmo()) {
            //Emoticons are shown in their own balloons; they require a specific layout containing an ImageView

            LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.wrapper);
            int i = Integer.parseInt(message.getMsg());
            //Emoticon code; it is its position in the GridView inside the PopupWindow; drawable resource contains a
            //5x5 emoticons table, the right one is in (code / 5) row and (code MOD 5) column

            if (message.getSender()) {
                //Emoticon sent by other user

                linearLayout.setBackground(getContext().getResources().getDrawable(R.drawable.white_bubbles));
                linearLayout.setGravity(Gravity.END);
                //white balloons

                Bitmap emoBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.red_emoticons);
                //Other users balloons are white, therefore a red emoticon is picked from the right drawable resource
                emoBitmap.setDensity(DisplayMetrics.DENSITY_HIGH);
                int w = emoBitmap.getWidth() / 5, h = emoBitmap.getHeight() / 5;

                ImageView emoticon = (ImageView) view.findViewById(R.id.emoticon);
                emoticon.setBackground(new BitmapDrawable(Bitmap.createBitmap(emoBitmap, (i % 5) * w, (i / 5) * h, w, h)));
                //Crop the right emoticon

                date.setGravity(Gravity.END);
                date.setTextColor(getContext().getResources().getColor(R.color.divider));

                ((RelativeLayout) view.findViewById(R.id.listLayout)).setGravity(Gravity.START);
                //shift balloon to the left and its content to the right
            }
            else {
                //emoticon sent by this device

                linearLayout.setBackground(getContext().getResources().getDrawable(R.drawable.red_bubbles));
                linearLayout.setGravity(Gravity.START);
                //99 Luftballons!

                Bitmap emoBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.white_emoticons);
                //This device balloons are red, therefore a white emoticon is picked from the right drawable resource
                emoBitmap.setDensity(DisplayMetrics.DENSITY_HIGH);
                int w = emoBitmap.getWidth() / 5, h = emoBitmap.getHeight() / 5;

                ImageView emoticon = (ImageView) view.findViewById(R.id.emoticon);
                emoticon.setBackground(new BitmapDrawable(Bitmap.createBitmap(emoBitmap, (i % 5) * w, (i / 5) * h, w, h)));
                //Crop the right emoticon

                date.setGravity(Gravity.START);
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
                comment.setTextColor(getContext().getResources().getColor(R.color.background));

                date.setGravity(Gravity.END);
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
                comment.setTextColor(getContext().getResources().getColor(R.color.accent));

                date.setGravity(Gravity.START);
                date.setTextColor(getContext().getResources().getColor(R.color.divider));

                ((RelativeLayout) view.findViewById(R.id.listLayout)).setGravity(Gravity.END);
                //shift balloon to the left and its content to the right
            }

            comment.setText(getItem(position).getMsg());
            //write message into balloon
        }

        String str = (message.getSender()) ? getContext().getString(R.string.received) : getContext().getString(R.string.sent);
        //building up message timestamp

        Date date1 = getItem(position).getDate();
        String when;
        long ago = date1.getTime() - (new Date()).getTime();
        //timestamp gets a different format depending on how old is the message

        if (ago < 86400000) {
            //this day
            when = getContext().getString(R.string.at) + (new SimpleDateFormat("HH:mm")).format(date1);

        }
        else if (ago < 172800000) {
            //yesterday
            when = getContext().getString(R.string.yesterday);
        }
        else if (ago < 604800000) {
            //this week
            when = getContext().getString(R.string.on_) + (new SimpleDateFormat("EEE")).format(date1) + " " +
                    getContext().getString(R.string.at) +  (new SimpleDateFormat("HH:mm")).format(date1);
        }
        else if (ago < 2629743830l) {
            //this month
            when = getContext().getString(R.string.on_) + (new SimpleDateFormat("EEE dd")).format(date1);
        }
        else {
            //later on
            when = getContext().getString(R.string.on_) + (new SimpleDateFormat("yy MM dd")).format(date1);
        }

        date.setText(str + when);

        return view;
    }
}
