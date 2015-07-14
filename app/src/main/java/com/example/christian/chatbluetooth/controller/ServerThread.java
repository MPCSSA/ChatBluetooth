package com.example.christian.chatbluetooth.controller;


import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import java.io.IOException;

public class ServerThread extends Thread {

    private BluetoothServerSocket server;
    private Handler handler;

    public void setServer(BluetoothServerSocket server) {
        this.server = server;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public ServerThread(BluetoothServerSocket server, Handler handler) {

        setServer(server);
        setHandler(handler);
    }

    public void run() {

        BluetoothSocket sckt;
        while(true) {
            
            try {
                sckt = server.accept();
            }
            catch (IOException e) {
                e.printStackTrace();
                break;
            }

            if (sckt != null) {
                (new ReceiverThread(sckt, handler)).start();
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
