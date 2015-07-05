package com.example.christian.chatbluetooth.controller;

import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;

import com.example.christian.chatbluetooth.view.Activities.ChatActivity;

import java.util.ArrayList;

public class AsyncScavenger extends AsyncTask<Void, Void, Void> {

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

        BlueCtrl.dispatchNews(drop, null);

    }

    @Override
    protected Void doInBackground(Void... params) {

        BluetoothDevice dvc;
        while ((dvc = BlueCtrl.cleanCloseDvc()) != null) {
            macs.addAll(BlueCtrl.dropUsers(dvc));
        }
        BlueCtrl.counter = 0;
        return null;
    }

}
