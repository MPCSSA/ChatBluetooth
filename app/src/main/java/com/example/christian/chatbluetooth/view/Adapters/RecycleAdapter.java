package com.example.christian.chatbluetooth.view.Adapters;

import android.bluetooth.BluetoothDevice;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.christian.chatbluetooth.R;
import com.example.christian.chatbluetooth.controller.BlueCtrl;
import com.example.christian.chatbluetooth.model.ChatUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class RecycleAdapter extends RecyclerView.Adapter<RecycleAdapter.UserViewHolder> {  // <RecycleAdapter.UserViewHolder>

    private List<ChatUser> userList;
    /*private int favCounter = 0;
    private static ChatUser favTab = new ChatUser("FavoritesTab", null, 0, 0, null);
    private static ChatUser pubTab = new ChatUser("PublicTab", null, 0, 0, null);*/


    public RecycleAdapter(List<ChatUser> userList){
        this.userList = userList;
        //add(pubTab);
    }

    @Override
    public int getItemCount(){
        return userList.size();
    }

    @Override
    public void onBindViewHolder(UserViewHolder userViewHolder, int i) {

        /*if (i == 0 && favCounter > 0) {
            userViewHolder.name.setText("Favs");
        }
        else if (i == favCounter) {
            userViewHolder.name.setText("Public");
        }
        else {*/
            ChatUser chatUser = userList.get(i);
            String username = chatUser.getName();
            if (username == null) username = "Unknown";
            userViewHolder.name.setText(username); // chatUser.getName()
        //}
    }

    @Override
    public UserViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

        View itemView;

        /*if (i == 0 || i == favCounter)
            itemView = LayoutInflater.
                    from(viewGroup.getContext()).
                    inflate(R.layout.card_tab, viewGroup, false);

        else*/
            itemView = LayoutInflater.
                    from(viewGroup.getContext()).
                    inflate(R.layout.card_layout, viewGroup, false);

        return new UserViewHolder(itemView);
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        protected TextView name;

        public UserViewHolder(View v){
            super(v);

            name = (TextView) v.findViewById(R.id.title);

        }
    }

    public boolean add(ChatUser user) {

        boolean result;
        /*if (user.isFav()) {

            if (favCounter == 0) {

                userList.add(0, favTab);

            }
        }
        else */result = userList.add(user);

        notifyDataSetChanged();
        return result;
    }

    public ChatUser getItem(int position){
        return userList.get(position);
    }

    public Collection dropUsers(BluetoothDevice lostDvc) {

        ArrayList<byte[]> lostDvcs = new ArrayList<>();

        int i = 0;
        for (ChatUser u : userList) {
            if (u.getNextNode() != null && u.getNextNode().equals(lostDvc)) {
                lostDvcs.add(u.getMacInBytes());
                userList.remove(i);
            }
            else ++i;
        }

        return lostDvcs;
    }
}
