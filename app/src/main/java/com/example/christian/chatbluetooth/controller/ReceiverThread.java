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
                    //TODO Manage incoming Chat Message
                    int i, j;
                    char c = 0;
                    StringBuilder strBld = new StringBuilder();

                    do {

                        i = in.read(bytes);
                        j = 0;

                        for (byte b : bytes) {
                            if (j%2 == 0) c = (char) ((char) b << 8);
                            else {
                                c += (char) b;
                                strBld.append(c);
                            }
                            ++j;
                        }

                    } while (i == 1024);

                    String msg = strBld.toString();
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
