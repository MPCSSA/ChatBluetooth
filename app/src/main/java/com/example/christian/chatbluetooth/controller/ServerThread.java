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
            System.out.println("before accept");
            try {
                sckt = server.accept();
                System.out.println("accepted");
            }
            catch (IOException e) {
                e.printStackTrace();
                System.out.println("server side down");
                break;
            }

            if (sckt != null) {
                (new ReceiverThread(sckt)).start();
            }
            else System.out.println("whattafuck");
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
