package com.example.christian.chatbluetooth.view.Adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TextView;

import com.example.christian.chatbluetooth.R;
import com.example.christian.chatbluetooth.view.Activities.SettingActivity;


public class SettingAdapter extends ArrayAdapter<String> {

    private int layout;

    public SettingAdapter(Context context, int resource) {
        super(context, resource);

        layout = resource;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view;

        if (position < 3) {

            view = inflater.inflate(layout, parent, false);
            ((TextView) view.findViewById(R.id.set_item)).setText(getItem(position));

            CheckBox cb = (CheckBox) view.findViewById(R.id.visible_cb);

            if (position == 0) {
                cb.setEnabled(false);
                cb.setVisibility(View.INVISIBLE);
            }
            else {

                cb.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        ((SettingActivity)getContext()).checked[position - 1] = ((CheckBox)view).isChecked();
                    }
                });
            }

            int p;
            if (position == 1 && (p = ((SettingActivity)getContext()).flag) != 0) {

                Bitmap bmp = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.flags);
                int w = bmp.getWidth() / 17, h = bmp.getHeight() / 12;
                view.findViewById(R.id.flag).setBackground(new BitmapDrawable(
                        Bitmap.createBitmap(bmp, w * (p / 12), h * (p % 12), w, h)));
            }



        }
        else {

            view = inflater.inflate(R.layout.item_gender, parent, false);

            RadioButton male = (RadioButton) view.findViewById(R.id.male_settings),
                    fem = (RadioButton) view.findViewById(R.id.fem_settings);

            if (((SettingActivity)getContext()).gender == 1) (male).setChecked(true);
            else if (((SettingActivity)getContext()).gender == 2) (fem).setChecked(true);

            male.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    ((SettingActivity)getContext()).gender = 1;
                }
            });

            fem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    ((SettingActivity)getContext()).gender = 2;
                }
            });

            CheckBox cb = (CheckBox) view.findViewById(R.id.visible_cb);
            cb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    ((SettingActivity)getContext()).checked[position - 1] = ((CheckBox)view).isChecked();
                }
            });
        }

        if (position > 0) ((CheckBox) view.findViewById(R.id.visible_cb)).setChecked(((SettingActivity)getContext()).checked[position - 1]);

        return view;
    }
}
