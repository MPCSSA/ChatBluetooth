package com.example.christian.chatbluetooth.model;


import android.bluetooth.BluetoothDevice;

import java.util.Date;

public class ChatUser {

    //TODO: observer mechanism

    private String strMAC; //target MAC address in String format
    private byte[] bMAC; //target MAC address in byte array format
    private String name; //user name
    private int status; //user status
    private BluetoothDevice nextNode; //next device on the route to the actual device
    private int bounces; //number of chained devices before actual user
    private Date lastUpd;
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

        return strMAC;
    }
    public void setMac(String strMAC) {

        this.strMAC = strMAC;
    }

    public byte[] getMacInBytes() {

        return bMAC;
    }
    public void setMac(byte[] bMAC) {

        this.bMAC = bMAC;
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

    public Date getLastUpd() {

        return lastUpd;
    }
    public void setLastUpd(long date) {

        setLastUpd(new Date(date));
    }
    public void setLastUpd(Date date) {

        this.lastUpd = date;
    }
}
