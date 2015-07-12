package com.example.christian.chatbluetooth.view.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.christian.chatbluetooth.R;
import com.example.christian.chatbluetooth.controller.BlueCtrl;
import com.example.christian.chatbluetooth.model.ChatUser;
import com.example.christian.chatbluetooth.model.Country;

import java.util.Date;
import java.util.List;


public class RecycleAdapter extends RecyclerView.Adapter<RecycleAdapter.UserViewHolder> {

    private Context context; //Activity
    private List<ChatUser> userList; //user list (either BlueCtrl.userList or BlueCtrl.favList)

    public RecycleAdapter(Context context, List<ChatUser> userList){

        this.context = context;
        this.userList = userList;
    }

    @Override
    public int getItemCount(){
        return userList.size();
    }

    @Override
    public void onBindViewHolder(UserViewHolder userViewHolder, int i) {

        ChatUser chatUser = userList.get(i);

        String username = chatUser.getName();
        if (username == null) username = context.getString(R.string.unknown); //No user information yet
        userViewHolder.name.setText(username);

        long age = chatUser.getAge();
        if (age > 0) {

            userViewHolder.age.setText(String.valueOf((new Date()).getTime() - age));
        }
        //display age if public

        int gender = chatUser.getGender();
        if (gender > 0) {

            if (gender == 1) userViewHolder.thumb.setBackground(context.getDrawable(R.drawable.favorite));
            else userViewHolder.thumb.setBackground(context.getDrawable(R.drawable.favorite));
        }
        //display gender if public

        int c = chatUser.getCountry();
        Country country =  BlueCtrl.fetchFlag(c);
        if (c > 0 && country != null) {

            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.flags);
            int w = bitmap.getWidth() / 17, h = bitmap.getHeight() / 12, pos = country.getPosition();

            userViewHolder.flag.setBackground(new BitmapDrawable(Bitmap.createBitmap(bitmap, (pos / 12) * w, (pos % 12) * h, w, h)));
        }
        //display country flag if public
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

        View itemView = LayoutInflater.
                    from(viewGroup.getContext()).
                    inflate(R.layout.card_layout, viewGroup, false);

        return new UserViewHolder(itemView);
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        protected TextView name;
        protected TextView age;
        protected ImageView thumb;
        protected ImageView flag;

        public UserViewHolder(View v){
            super(v);

            name = (TextView) v.findViewById(R.id.title);
            age = (TextView) v.findViewById(R.id.tv_age);
            thumb = (ImageView) v.findViewById(R.id.profilePict);
            flag = (ImageView) v.findViewById(R.id.user_flag);
        }
    }

    //public boolean add(ChatUser user) {

        //TODO: SHOULD BE DEPRECATED

    /*    boolean result;

        result = userList.add(user);
        notifyDataSetChanged();

        return result;
    }*/

    public ChatUser getItem(int position){
        return userList.get(position);
    }
}
