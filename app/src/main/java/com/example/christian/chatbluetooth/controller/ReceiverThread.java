package com.example.christian.chatbluetooth.controller;

import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;

public class ReceiverThread extends Thread {

    private BluetoothSocket sckt;
    private InputStream in;

    public BluetoothSocket getSckt() {
        return sckt;
    }
    public void setSckt(BluetoothSocket sckt) {
        this.sckt = sckt;
    }

    public ReceiverThread(BluetoothSocket sckt) {
        setSckt(sckt);
        InputStream tmp = null;

        try {
            tmp = this.sckt.getInputStream();
        }
        catch (IOException ignore) {}

        in = tmp;
    }

    public void run() {

        try {

            byte flag = (byte) in.read();
            byte[] bytes = new byte[1024];

            switch (flag) {
                case BlueCtrl.GRT_HEADER: {
                    //TODO Manage Greetings message
                    break;
                }

                case BlueCtrl.UPD_HEADER: {
                    //TODO Manage Update message
                    break;
                }

                case BlueCtrl.MSG_HEADER: {

                    int i;
                    String msg = "";

                    do {

                        i = in.read(bytes);
                        msg.concat(new String(bytes));

                    } while (i == 1024);

                    BlueCtrl.buildMsg(msg);
                    break;
                }

                case BlueCtrl.DRP_HEADER: {
                    //TODO Manage Drop Request
                    break;
                }
            }


        }
        catch (IOException e) {
            try {
                in.close();
            }
            catch (IOException ignore) {}
        }
    }
}
