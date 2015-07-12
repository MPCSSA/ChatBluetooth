package com.example.christian.chatbluetooth.view.Adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.christian.chatbluetooth.R;

public class MenuAdapter extends ArrayAdapter<String>{
    //Navigation Drawer adapter for action listing

    private int layout;
    private int[] icons = {R.mipmap.profile, R.mipmap.setting, R.mipmap.history};
    private String[] titles;

    public MenuAdapter(Context context, int resource) {
        super(context, resource);
        layout = resource;
        titles = new String[]{getContext().getString(R.string.profile), getContext().getString(R.string.settings),
                              getContext().getString(R.string.history)};
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null){
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(layout, parent, false);
        }

        ImageView imageView = (ImageView) view.findViewById(R.id.menu_item_icon);
        TextView textView = (TextView) view.findViewById(R.id.menu_item_text);

        imageView.setBackground(getContext().getDrawable(icons[position]));
        textView.setText(titles[position]);

        return view;
    }
}
