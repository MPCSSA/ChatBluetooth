package com.example.christian.chatbluetooth.model;


import android.bluetooth.BluetoothDevice;
import android.database.Cursor;

import com.example.christian.chatbluetooth.controller.BlueCtrl;

import java.util.Date;

public class ChatUser {

    //identifier information
    private String strMAC; //target MAC address in String format
    private byte[] bMAC; //target MAC address in byte array format
    private Date lastUpd;
    private byte[] updSegment;

    public String getMac() { return strMAC; }
    public void setMac(String strMAC) { this.strMAC = strMAC; }

    public byte[] getMacInBytes() { return bMAC; }
    public void setMac(byte[] bMAC) { this.bMAC = bMAC; }

    public long getLastUpd() { return lastUpd.getTime(); }
    public void setLastUpd(long date) { setLastUpd(new Date(date)); }
    public void setLastUpd(Date date) { this.lastUpd = date; }

    public byte[] getSegment() { return this.updSegment; }
    public void setSegment(byte[] updSegment) { this.updSegment = updSegment; }
    private void setSegment() {
        //Create a segment on instantiation to get it faster when needed

        byte[] segment = new byte[16];

        System.arraycopy(bMAC, 0, segment, 0, 6);

        byte[] timestamp = BlueCtrl.longToBytes(lastUpd.getTime());
        System.arraycopy(timestamp, 0, segment, 6, 8);

        segment[14] = (byte) bounces;
        segment[15] = (byte) status;

        setSegment(segment);
    }

    //volatile information
    private BluetoothDevice nextNode; //next device on the route to the actual device
    private int bounces; //number of chained devices before actual user
    private int status; //user status
    private boolean notification; //for RecyclerAdapter usage

    public BluetoothDevice getNextNode() { return nextNode; }
    public void setNextNode(BluetoothDevice nextNode) { this.nextNode = nextNode; }

    public int getBounces(){ return bounces; }
    public void setBounces(int bounces) { this.bounces = bounces; }

    public int getStatus(){ return status; }
    public void setStatus(int status) { this.status = status; }

    public void notifyMessage() { this.notification = true; }
    public void notificationRead() { this.notification = false; }
    public boolean getNotification() { return this.notification; }


    //persistent information
    private String name; //user name
    private boolean favorite; //whether or not the user is into Favorites category
    private long age;
    private int gender, country;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean isFav(){ return favorite; }
    public void setFav(boolean fav) { this.favorite = fav; }

    public int getCountry() { return country; }
    public void setCountry(int code) { this.country = code; }

    public int getGender() { return this.gender; }
    public void setGender(int gender) { this.gender = gender; }

    public long getAge() { return age; }
    public void setAge(long age) { this.age = age; }

    public ChatUser(String mac, BluetoothDevice nextNode, int bounces, int status, long timestamp, Cursor cursor) {

        setMac(mac);
        setMac(BlueCtrl.macToBytes(mac));
        setNextNode(nextNode);
        setLastUpd(timestamp);
        setBounces(bounces);
        setStatus(status);
        //initialize fundamental user information

        notificationRead();
        //no new notification

        setSegment();
        //build segment for update requests

        if (cursor != null && cursor.getCount() > 0) {
            addPersistentInfo(cursor);
        }
    }

    public void addPersistentInfo(Cursor profileInfo) {
        //add persistent profile information

        profileInfo.moveToFirst();
        setName(profileInfo.getString(1));
        setCountry(profileInfo.getInt(5));
        setGender(profileInfo.getInt(6));
        setAge(profileInfo.getLong(7));
    }

    public boolean updateUser(BluetoothDevice dvc, int bnc, int sts) {
        //This method checks for any changes in ChatUser fields and updates the instance

        boolean bool0, bool1;
        bool0 = bool1 = false;

        if (bnc < bounces) {
            this.setNextNode(dvc);
            this.setBounces(bnc);
            bool0 = true;
        }

        if (sts != this.getStatus()) {
            setStatus(sts);
            bool1 = true;
        }

        return (bool0 || bool1);
    }
}
