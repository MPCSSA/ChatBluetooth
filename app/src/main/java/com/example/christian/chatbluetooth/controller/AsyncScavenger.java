package com.example.christian.chatbluetooth.controller;

import android.os.AsyncTask;
import android.os.Handler;

import com.example.christian.chatbluetooth.model.ChatUser;

import java.util.ArrayList;

public class AsyncScavenger extends AsyncTask<Void, Void, Void> {

    private ArrayList<byte[]> macs= new ArrayList<>();
    private ArrayList<ChatUser> users;
    private Handler handler;

    public AsyncScavenger(Handler handler, ArrayList<ChatUser> users) {

        this.users = users;
        this.handler = handler;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        byte[] drop = new byte[2 + 6 * macs.size()];

        drop[0] = BlueCtrl.DRP_HEADER; //packet header
        drop[1] = (byte) macs.size(); //number field

        for (int _ = 0; _ < macs.size(); ++_) {

            System.arraycopy(macs.get(_), 0, drop, 2 + 6 * _, 6); //List of MACs
        }

        BlueCtrl.dispatchNews(drop, null, handler);
    }

    @Override
    protected Void doInBackground(Void... params) {

        for(ChatUser user : users) {

            macs.add(user.getMacInBytes());
            //Insert macs of lost devices in a temporary buffer
        }

        return null;
    }

}
