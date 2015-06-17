package com.example.christian.chatbluetooth.controller;

import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.OutputStream;

public class MessageThread extends Thread {

    private BluetoothSocket sckt;
    private byte[] msg;
    private OutputStream out;

    public BluetoothSocket getSckt() {
        return sckt;
    }

    public void setSckt(BluetoothSocket sckt) {
        this.sckt = sckt;
    }

    public byte[] getMsg() {
        return msg;
    }

    public void setMsg(byte[] msg) {
        this.msg = msg;
    }

    public OutputStream getOut() {
        return out;
    }

    public void setOut(OutputStream out) {
        this.out = out;
    }

    public MessageThread(BluetoothSocket sckt, byte[] msg) {
        setSckt(sckt);
        setMsg(msg);

        OutputStream out = null;
        try {

            out = this.sckt.getOutputStream();

        }
        catch (IOException ignore) {}

        setOut(out);
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
