package com.example.christian.chatbluetooth.view.Adapters;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.christian.chatbluetooth.R;

public class MyProfileAdapter extends ArrayAdapter<String[]>{

    private int layout; //layout resource

    public MyProfileAdapter(Context context, int resource) {
        super(context, resource);

        layout = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;

        if (view == null){
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(layout, parent, false);
        }

        if (getItem(position).length < 2) {

            int gender = Integer.parseInt(getItem(position)[0]), image = (gender == 1) ? R.drawable.male_radio_unchecked : R.drawable.fem_radio_unchecked;
            view.findViewById(R.id.gender).setBackground(getContext().getDrawable(image));

            return view;
        }
        //Gender field, drawable only
        else view.findViewById(R.id.gender).setBackground(null); //set empty image

        ((TextView) view.findViewById(R.id.field)).setText(getItem(position)[0]);
        ((TextView) view.findViewById(R.id.value)).setText(getItem(position)[1]);
        //Text Fields

        if (getItem(position)[0].equals(getContext().getString(R.string.from))) {
            //Country field

            int p = Integer.parseInt(getItem(position)[2]);

            Bitmap bmp = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.flags);
            int w = bmp.getWidth() / 17, h = bmp.getHeight() / 12;
            view.findViewById(R.id.flag).setBackground(new BitmapDrawable(
                    Bitmap.createBitmap(bmp, w * (p / 12), h * (p % 12), w, h)));
            //Show flag
        }
        else view.findViewById(R.id.flag).setBackground(null); //set empty image

        return view;
    }
}
