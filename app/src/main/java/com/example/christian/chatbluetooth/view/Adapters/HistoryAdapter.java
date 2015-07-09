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
import java.util.ArrayList;
import java.util.Date;

public class HistoryAdapter extends ArrayAdapter<ChatMessage> {

    public HistoryAdapter(Context context, int resource) {
        super(context, resource);
    }

    private ArrayList<ChatMessage> messages = new ArrayList<>();

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ChatMessage msg = getItem(position);
        int layout = (msg.isEmo()) ? R.layout.item_history_emo : R.layout.item_history;
        //Resources initialization

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(layout, parent, false);

        ((CheckBox)view.findViewById(R.id.cboxSelect)).setChecked(msg.getSender());
        int backgroundColor = (msg.getSender()) ? R.color.light_primary : R.color.accent,
                viewColor = (msg.getSender()) ? R.color.accent : R.color.light_primary,
                imageResource = (msg.getSender()) ? R.drawable.white_emoticons : R.drawable.red_emoticons;

        if (msg.isEmo()) {

            ImageView emoticon = (ImageView) view.findViewById(R.id.emoticon);

            Bitmap emoBitmap = BitmapFactory.decodeResource(getContext().getResources(), imageResource);
            //This device balloons are red, therefore a white emoticon is picked from the right drawable resource
            emoBitmap.setDensity(DisplayMetrics.DENSITY_HIGH);
            int w = emoBitmap.getWidth() / 5, h = emoBitmap.getHeight() / 5, code = Integer.parseInt(msg.getMsg());

            emoticon.setBackground(new BitmapDrawable(Bitmap.createBitmap(emoBitmap, (code % 5) * w, (code / 5) * h, w, h)));
            //Crop the right emoticon
        }
        else {

            TextView quote = (TextView) view.findViewById(R.id.tv_quote);
            quote.setText("\"" + msg.getMsg() + "\"");
            quote.setTextColor(getContext().getResources().getColor(viewColor));
        }

        TextView from = (TextView) view.findViewById(R.id.tv_from);

        Date date = msg.getDate();
        String when;
        long ago = date.getTime() - (new Date()).getTime();
        //timestamp gets a different format depending on how old is the message

        if (ago < 86400000) {
            //this day
            when = "Today at " + (new SimpleDateFormat("HH:mm")).format(date);

        } else if (ago < 172800000) {
            //yesterday
            when = "Yesterday at " + (new SimpleDateFormat("HH:mm")).format(date);
        } else if (ago < 604800000) {
            //this week
            when = "this " + (new SimpleDateFormat("EEE 'at' HH:mm")).format(date);
        } else if (ago < 2629743830l) {
            //this month
            when = "On " + (new SimpleDateFormat("EEE dd 'at' HH:mm")).format(date);
        } else {
            //later on
            when = "On " + (new SimpleDateFormat("yy MM dd 'at' HH:mm")).format(date);
        }

        from.setText(msg.getUsername() + ", " + when);
        from.setTextColor(getContext().getResources().getColor(viewColor));

        (view.findViewById(R.id.cboxSelect)).setBackgroundColor(viewColor);
        view.findViewById(R.id.history_layout).setBackgroundColor(getContext().getResources().getColor(backgroundColor));

        messages.add(msg);

        return view;
    }

    public ChatMessage remove(int position) {

        ChatMessage msg = messages.remove(position);
        remove(msg);
        return msg;
    }
}
