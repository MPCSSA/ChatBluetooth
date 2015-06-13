package com.example.christian.chatbluetooth.controller;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import com.example.christian.chatbluetooth.model.BlueDBManager;
import com.example.christian.chatbluetooth.model.ChatUser;
import com.example.christian.chatbluetooth.model.ChatUserAdapter;

import java.io.IOException;

public class BlueCtrl {

    public static final byte GRT_HEADER = (byte) 0; //header for Greetings Message
    public static final byte UPD_HEADER = (byte) 1; //header for Update Message
    public static final byte MSG_HEADER = (byte) 2; //header for Chat Message
    public static final byte DRP_HEADER = (byte) 3; //header for Drop Request
    public static final String UUID = "BlueRoom";   //custom UUID

    private static ChatUserAdapter userAdapt;        //ChatUser Adapter; initialized on MainActivity creation
    private static BlueDBManager dbManager;          //User and Messages DB Manager

    public static void setUserAdapt(ChatUserAdapter userAdapt) {
        BlueCtrl.userAdapt = userAdapt;
    }

    public static void setDbManager(BlueDBManager dbManager) {
        BlueCtrl.dbManager = dbManager;
    }

    public static void sendMsg(String target, String sender, String msg) {
        //Use this method to prepare the packet to forward to a Bluetooth device.
        //Target param is the MAC address of target device, NOT the Bluetooth device receiving the packet from
        //this device. Sender param is the sender's MAC address.

        byte[] bMsg = msg.getBytes(); //Message in bytes
        byte[] bTarget = target.getBytes();
        byte[] bSender = sender.getBytes();
        byte[] pckt = new byte[3 + bTarget.length + bSender.length + bMsg.length]; //actual bytes packet that has to be sent
        pckt[0] = MSG_HEADER; //packet header
        int i = 1;

        for(byte b : bTarget) {
            pckt[i] = b; //Target field
            ++i;
        }

        bTarget[i] = (byte) 0;
        ++i;

        for(byte b : bSender) {
            pckt[i] = b; //Target field
            ++i;
        }

        bTarget[i] = (byte) 0;
        ++i;

        for(byte b : bMsg) {
            pckt[i] = b; //message field
            ++i;
        }

        BluetoothDevice node = scanUsers(target); //searches for the actual Bluetooth Device that receives the packet
        BluetoothSocket sckt;

        try{
            //initiate communication in another thread
            if (node != null) {
                sckt = node.createInsecureRfcommSocketToServiceRecord(java.util.UUID.fromString(UUID));
                (new MessageThread(sckt, pckt)).start();
            }
        }
        catch (IOException ignore) {}

    }

    private static BluetoothDevice scanUsers(String address) {

        ChatUser user;

        for (int i = 0; i < userAdapt.getCount(); ++i) {
            //get ChatUser objects sequentially and search for a match

            user = userAdapt.getItem(i);
            if (user.getMac().equals(address)) {
                //next node found and returned
                return user.getNextNode();
            }
        }

        return null;
    }

    public static void getUserList() {

        //TODO: make bluetooth discovery and update model ChatUser Adapter

    }

    public static void retrieveHistory(String username) {

        //TODO: fetch msg history via DBManager

    }

    public static void buildMsg(String from, String msg) {

        //TODO: TextView building mechanism to show message in chat

    }

    public static void manageDropRequest(String address, String[] macs) {

        //TODO: Drop Request management
    }

    public static void addChatUser(String address, String next, int bounces, String name, int status) {

        //TODO: create new ChatUser object and add it to ChatUser Adapter

    }
}
