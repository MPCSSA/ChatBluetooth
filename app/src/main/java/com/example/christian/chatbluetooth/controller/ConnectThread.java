package com.example.christian.chatbluetooth.controller;

import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.OutputStream;

public class ConnectThread extends Thread {

    private BluetoothSocket sckt;
    private byte[] msg;
    private OutputStream out;

    public ConnectThread(BluetoothSocket sckt, byte[] msg) {
        this.sckt = sckt;
        this.msg = msg;

        OutputStream out = null;
        try {

            out = this.sckt.getOutputStream();

        }
        catch (IOException ignore) {}

        this.out = out;
    }

    public void run() {

        try {

            sckt.connect();
            out.write(msg);

        }
        catch(IOException e) {

            try {
                sckt.close();
            }
            catch(IOException ignore) {}
        }

        cancel();
    }

    public void cancel() {

        try {

            sckt.close();

        }
        catch(IOException ignore) {}
    }
}
