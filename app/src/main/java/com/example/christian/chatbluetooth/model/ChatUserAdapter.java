package com.example.christian.chatbluetooth.model;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;


public class ChatUserAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static RecyclerView.ViewHolder holder;
    private ArrayList<ChatUser> userList;

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
