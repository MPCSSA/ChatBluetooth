package com.example.christian.chatbluetooth.model;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;


public class ChatUserAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<ChatUser> userList;
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView LargeTV;
        public TextView smallTV;
        public ViewHolder(TextView v1, TextView v2) {
            super(v1);
            LargeTV = v1;
            smallTV = v2;
        }
    }

    public ArrayList<ChatUser> getUserList() {
        return this.userList;
    }
    public void setUserList(ArrayList<ChatUser> arrayList) {
        this.userList = arrayList;
    }

    public ChatUserAdapter() {
        this(null);
    }

    public ChatUserAdapter(ArrayList<ChatUser> arrayList) {
        setUserList(arrayList);
    }

    public void addUser(ChatUser user) {

        if (getUserList() == null) {
            setUserList(new ArrayList<ChatUser>());
        }

        getUserList().add(user);

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return getUserList().size();
    }
}
