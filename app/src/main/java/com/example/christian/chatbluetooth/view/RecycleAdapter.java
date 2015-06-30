package com.example.christian.chatbluetooth.view;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.christian.chatbluetooth.R;
import com.example.christian.chatbluetooth.model.ChatUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;


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

    public boolean add(ChatUser user) {

        boolean result = userList.add(user);
        notifyDataSetChanged();
        return result;
    }

    public ChatUser getItem(String address) {

        for (ChatUser user : userList) {
            if (user.getMac().equals(address)) return user;
        }

        return null;
    }

    public ChatUser getItem(int position){
        return userList.get(position);
    }

    public Collection dropUsers(BluetoothDevice lostDvc) {

        ArrayList<byte[]> lostDvcs = new ArrayList<>();

        int i = 0;
        for (ChatUser u : userList) {
            if (u.getNextNode().equals(lostDvc)) {
                lostDvcs.add(u.getMacInBytes());
                userList.remove(i);
            }
            else ++i;
        }

        return lostDvcs;
    }
}
