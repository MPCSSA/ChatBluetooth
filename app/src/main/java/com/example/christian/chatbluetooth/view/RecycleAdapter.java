package com.example.christian.chatbluetooth.view;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.christian.chatbluetooth.R;
import com.example.christian.chatbluetooth.model.ChatUser;

import java.util.List;


public class RecycleAdapter extends RecyclerView.Adapter<RecycleAdapter.UserViewHolder> {  // <RecycleAdapter.UserViewHolder>

    private List<ChatUser> userList;

    public RecycleAdapter(List<ChatUser> userList){
        this.userList = userList;
    }

    @Override
    public int getItemCount(){
        return userList.size();
    }

    @Override
    public void onBindViewHolder(UserViewHolder userViewHolder, int i){
        ChatUser chatUser = userList.get(i);
        userViewHolder.name.setText("Ciao"); // chatUser.getName()
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup viewGroup, int i){
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.card_layout, viewGroup, false);

        return new UserViewHolder(itemView);
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder{
        protected TextView name;

        public UserViewHolder(View v){
            super(v);

            name = (TextView) v.findViewById(R.id.title);
        }
    }
}
