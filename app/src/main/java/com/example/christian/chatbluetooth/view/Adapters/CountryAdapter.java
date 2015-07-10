package com.example.christian.chatbluetooth.view.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.christian.chatbluetooth.R;
import com.example.christian.chatbluetooth.model.Country;

public class CountryAdapter extends ArrayAdapter<Country> {

    public CountryAdapter(Context context, int resource) {
        super(context, resource);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.item_countries, parent, false);
        }

        Country country = getItem(position);

        TextView country_name = (TextView) view.findViewById(R.id.tv_country);
        country_name.setText(country.getCountry());

        ImageView flag = (ImageView) view.findViewById(R.id.country_flag);
        Bitmap bitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.flags);
        int w = bitmap.getWidth() / 17, h = bitmap.getHeight() / 12;
        flag.setBackground(new BitmapDrawable(Bitmap.createBitmap(bitmap, w * (country.getPosition() / 17),
                                                                  h * (country.getPosition() / 12), w, h)));

        return view;
    }
}
