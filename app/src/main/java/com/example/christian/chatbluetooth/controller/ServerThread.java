package com.example.christian.chatbluetooth.controller;


import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;

public class ServerThread extends Thread {

    private BluetoothServerSocket server;

    public BluetoothServerSocket getServer() {
        return server;
    }
    public void setServer(BluetoothServerSocket server) {
        this.server = server;
    }

    public ServerThread(BluetoothServerSocket server) {

        setServer(server);

    }

    public void run() {

        BluetoothSocket sckt;
        while(true) {

            try {
                sckt = server.accept();
            }
            catch (IOException e) {
                break;
            }

            if (sckt != null) {
                (new ReceiverThread(sckt)).start();
            }
        }

        cancel();
    }

    public void cancel() {
        try {
            server.close();
        }
        catch (IOException ignore) {}
    }
}
