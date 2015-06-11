package com.example.christian.chatbluetooth.controller;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;

public class BlueCtrl {

    private static final byte GRT_HEADER = (byte) 0; //header for Greetings Message
    private static final byte UPD_HEADER = (byte) 1; //header for Update Message
    private static final byte MSG_HEADER = (byte) 2; //header for Chat Message
    private static final byte DRP_HEADER = (byte) 3; //header for Drop Request
    private static final String UUID = "BlueRoom";


    public static void sendMsg(String msg, String address, BluetoothDevice device) {

        //TODO: getting msg bytes and forwarding them to addres via insecure RFCOMM
        byte[] bTarget = address.getBytes();
        byte[] bMsg = msg.getBytes();
        byte[] pckt = new byte[1 + bTarget.length + bMsg.length];
        pckt[0] = MSG_HEADER;
        int i = 1;

        for(byte b : bTarget) {
            pckt[i] = b;
            ++i;
        }

        for(byte b : bMsg) {
            pckt[i] = b;
            ++i;
        }

        BluetoothSocket sckt;
        try{

            sckt = device.createInsecureRfcommSocketToServiceRecord(java.util.UUID.fromString(UUID));
            (new ConnectThread(sckt, pckt)).start();

        }
        catch (IOException ignore) {}

    }

    public static void getUserList() {

        //TODO: make bluetooth discovery and update model ChatUser Adapter

    }

    public static void retrieveHistory(String username) {

        //TODO: fetch msg history via DBManager

    }

}
