package com.example.christian.chatbluetooth.view.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.christian.chatbluetooth.R;
import com.example.christian.chatbluetooth.model.ChatMessage;

import java.text.SimpleDateFormat;
import java.util.Date;

public class HistoryAdapter extends ArrayAdapter<ChatMessage> {

    public HistoryAdapter(Context context, int resource) {
        super(context, resource);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ChatMessage msg = getItem(position); //ChatMessage item
        int layout = (msg.isEmo()) ? R.layout.item_history_emo : R.layout.item_history;
        //Different layout for Text and Emoticons

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(layout, parent, false);
        //inflate layout

        ((CheckBox)view.findViewById(R.id.cboxSelect)).setChecked(msg.getSender());
        //sentBy attribute has no use in this context; therefore, here it is used as a persistent way of checking CheckBoxes

        int imageResource = R.drawable.white_emoticons;

        if (msg.isEmo()) {
            //display emoticon

            ImageView emoticon = (ImageView) view.findViewById(R.id.emoticon);

            Bitmap emoBitmap = BitmapFactory.decodeResource(getContext().getResources(), imageResource);
            //white cards red emoticons
            emoBitmap.setDensity(DisplayMetrics.DENSITY_HIGH);
            int w = emoBitmap.getWidth() / 5, h = emoBitmap.getHeight() / 5, code = Integer.parseInt(msg.getMsg());

            emoticon.setBackground(new BitmapDrawable(Bitmap.createBitmap(emoBitmap, (code % 5) * w, (code / 5) * h, w, h)));
            //Crop the right emoticon
        }
        else {
            //display text message

            TextView quote = (TextView) view.findViewById(R.id.tv_quote);
            quote.setText("\"" + msg.getMsg() + "\"");
        }

        TextView from = (TextView) view.findViewById(R.id.tv_from); //

        Date date = msg.getDate();
        String when;
        long ago = date.getTime() - (new Date()).getTime();
        //timestamp gets a different format depending on how old is the message

        if (ago < 86400000) {
            //this day
            when = getContext().getString(R.string.today_at) + (new SimpleDateFormat("HH:mm")).format(date);

        } else if (ago < 172800000) {
            //yesterday
            when = getContext().getString(R.string.yesterday_at) + (new SimpleDateFormat("HH:mm")).format(date);
        } else if (ago < 604800000) {
            //this week
            when = getContext().getString(R.string.this_) + (new SimpleDateFormat("EEE")).format(date) + " " +
                    getContext().getString(R.string.at) + (new SimpleDateFormat("HH:mm")).format(date);
        } else if (ago < 2629743830l) {
            //this month
            when = getContext().getString(R.string.on_) + (new SimpleDateFormat("EEE dd")).format(date) + " " + getContext().getString(R.string.at) +
                    (new SimpleDateFormat("HH:mm")).format(date);
        } else {
            //later on
            when = getContext().getString(R.string.on_) + (new SimpleDateFormat("yy MM dd")).format(date) + " " +
                    getContext().getString(R.string.at) +  (new SimpleDateFormat("HH:mm")).format(date);
        }

        from.setText(msg.getUsername() + ", " + when); //from who and when

        return view;
    }
}
