package com.example.christian.chatbluetooth.controller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.database.Cursor;

import com.example.christian.chatbluetooth.model.BlueDBManager;
import com.example.christian.chatbluetooth.model.ChatUser;
import com.example.christian.chatbluetooth.model.ChatUserAdapter;

import java.io.IOException;

public class BlueCtrl {

    public static final byte GRT_HEADER = (byte) 0; //header for Greetings Message
    public static final byte UPD_HEADER = (byte) 1; //header for Update Message
    public static final byte RQS_HEADER = (byte) 2; //header for Info Request
    public static final byte CRD_HEADER = (byte) 3; //header for Card Message
    public static final byte MSG_HEADER = (byte) 4; //header for Chat Message
    public static final byte DRP_HEADER = (byte) 5; //header for Drop Request
    public static final byte        ACK = (byte) 6; //ACKnowledge Message for communication synchronization
    public static final String     UUID = "BlueRoom"; //custom UUID

    public static boolean DISCOVERY_SUSPENDED = false;
    public static int DISCOVERY_LOCK = 0; //l'ultimo che esce chiude la porta

    private static ChatUserAdapter userAdapt;        //ChatUser Adapter; initialized on MainActivity creation
    private static BlueDBManager dbManager;          //User and Messages DB Manager
    private static final String dbname = "bluedb"; //DB name

    public static void setUserAdapt(ChatUserAdapter userAdapt) {
        BlueCtrl.userAdapt = userAdapt;
    }

    public static void setDbManager(BlueDBManager dbManager) {
        BlueCtrl.dbManager = dbManager;
    }

    public static void sendMsg(byte[] target, byte[] sender, byte[] msg) {
        //Use this method to prepare the packet to forward to a Bluetooth device.
        //Target param is the MAC address of target device, NOT the Bluetooth device receiving the packet from
        //this device. Sender param is the sender's MAC address.

        int length = msg.length; //must prevent more than 255 characters long messages
        byte[] pckt = new byte[14 + length]; //actual bytes packet that has to be sent
        pckt[0] = MSG_HEADER; //packet header

        int i = 1;
        for(byte b : target) {
            pckt[i] = b; //Target field
            ++i;
        }

        for(byte b : sender) {
            pckt[i] = b; //Target field
            ++i;
        }

        pckt[i] = (byte) length;
        ++i;

        for(byte b : msg) {
            pckt[i] = b;
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

    public static void sendMsg(byte[] target, byte[] msg) {

    }

    private static BluetoothDevice scanUsers(byte[] address) {

        //TODO: access ChatUserAdapter and retrieve ChatUser with address == MAC

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

    public static void manageDropRequest(String address, String macs) {

        //TODO: Drop Request management
    }

    public static void addChatUser(byte[] mac, BluetoothDevice next, int bounces, String name, byte status) {

        //TODO: create new ChatUser object and add it to ChatUser Adapter

    }

    public static void awakeUser(byte[] mac, String address, byte status, int bounces) {

        //TODO: fetch user information from DB and initialize ChatUser object

    }


    //TODO: DB Operations

    public static void openDatabase(Context context) {

        dbManager = new BlueDBManager(context, dbname);

    }

    public static boolean validateUser(String address, long timestamp) {

        Cursor cursor = dbManager.fetchTimestamp(address);

        if (!cursor.moveToFirst() || cursor.getLong(0) != timestamp) {
            return false;
        }

        return true;
    }

    public static Cursor fetchPersistentInfo(String address) {

        //TODO: fetch persistent user informations

        return null;
    }

    //TODO: Utils

    public static byte[] macToBytes(String address) {

        if (BluetoothAdapter.checkBluetoothAddress(address)) {

            byte[] mac = new byte[6];
            String[] digits = address.toLowerCase().split(":");
            byte b, counter = 0;

            for(String d : digits) {
                b = (byte) Integer.parseInt(d);
                mac[counter] = b;
                ++counter;
            }

            return mac;
        }

        return null;
    }

    public static String bytesToMAC(byte[] mac) {

        if (mac.length == 6) {

            String address = "";
            int i;
            boolean bool = false;

            for(byte b : mac) {

                i = (b < 0) ? (b + 256) : (int) b;

                if (bool) address += ':';
                else bool = !bool;

                address += Integer.toHexString(i).toUpperCase();

            }

            return address;
        }

        return null;
    }

    public static byte[] longToBytes(long l) {

        byte[] bytes = new byte[8]; //Java representation of a long int is made up of 64 bits

        for(int i = 7; i >= 0; --i) {
            bytes[i] = (byte) (l % 256);
            l /= 256;
        }

        return bytes;
    }

    public static long rebuildTimestamp(byte[] bytes) {

        long l = 0;
        int i = 7;

        for (byte b : bytes) {
            l += (b < 0) ? (long) (b + 256) << 8 * i : (long) b << 8 * i;
            --i;
        }

        return l;
    }

    public static void lockDiscoverySuspension() {

        ++DISCOVERY_LOCK; //thread eneters the room
        if (DISCOVERY_SUSPENDED) return; //the door was already opened

        DISCOVERY_SUSPENDED = true; //thread opened the door

        BluetoothAdapter.getDefaultAdapter().cancelDiscovery(); //and turned on lights
    }

    public static void unlockDiscoverySuspension() {
        if ((--DISCOVERY_LOCK) < 1) { //thread left the room

            DISCOVERY_SUSPENDED = false; //if thread was the last one in the room, turns off lights

            if (!BluetoothAdapter.getDefaultAdapter().isDiscovering())
                BluetoothAdapter.getDefaultAdapter().startDiscovery(); //and closes the door
        }
    }
}
