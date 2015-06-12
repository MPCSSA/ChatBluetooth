package com.example.christian.chatbluetooth.model;


import android.bluetooth.BluetoothDevice;

public class ChatUser {

    //TODO: observer mechanism

    private String mac; //target MAC address
    private String name; //user name
    private int status; //user status
    private BluetoothDevice nextNode; //next device on the route to the actual device
    private int bounces; //number of chained devices before actual user
    //private Bitmap profilePic;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public int getStatus(){

        return status;
    }
    public void setStatus(int status) {

        this.status = status;
    }

    public String getMac() {

        return mac;
    }
    public void setMac(String mac) {

        this.mac = mac;
    }

    public BluetoothDevice getNextNode() {

        return nextNode;
    }
    public void setNextNode(BluetoothDevice nextNode) {

        this.nextNode = nextNode;
    }

    public int getBounces(){

        return bounces;
    }
    public void setChain(int bounces) {

        this.bounces = bounces;
    }

}
