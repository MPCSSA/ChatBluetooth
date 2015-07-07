package com.example.christian.chatbluetooth.controller;

import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;

import com.example.christian.chatbluetooth.view.Activities.ChatActivity;

import java.util.ArrayList;

public class AsyncScavenger extends AsyncTask<String, Void, Void> {

    private ArrayList<byte[]> macs= new ArrayList<>();

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        byte[] drop = new byte[1 + 6 * macs.size()];
        drop[0] = BlueCtrl.DRP_HEADER;

        int i = 1;
        while (i < drop.length) {
            drop[i] = macs.get((i-1)/6)[(i-1)%6];
            ++i;
        }

        BlueCtrl.dispatchNews(drop, null, null);

    }

    @Override
    protected Void doInBackground(String... params) {

        for(String address : params) {
            macs.addAll(BlueCtrl.dropUsers(address));
        }

        return null;
    }

}
