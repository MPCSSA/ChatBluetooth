package com.example.christian.chatbluetooth.model;


import android.bluetooth.BluetoothDevice;
import android.database.Cursor;

import com.example.christian.chatbluetooth.controller.BlueCtrl;

import java.util.Date;

public class ChatUser {

    //TODO: observer mechanism

    //identifier information
    private String strMAC; //target MAC address in String format
    private byte[] bMAC; //target MAC address in byte array format
    private Date lastUpd;

    public String getMac() { return strMAC; }
    public void setMac(String strMAC) { this.strMAC = strMAC; }

    public byte[] getMacInBytes() { return bMAC; }
    public void setMac(byte[] bMAC) { this.bMAC = bMAC; }

    public long getLastUpd() { return lastUpd.getTime(); }
    public void setLastUpd(long date) { setLastUpd(new Date(date)); }
    public void setLastUpd(Date date) { this.lastUpd = date; }


    //volatile information
    private BluetoothDevice nextNode; //next device on the route to the actual device
    private int bounces; //number of chained devices before actual user
    private int status; //user status

    public BluetoothDevice getNextNode() { return nextNode; }
    public void setNextNode(BluetoothDevice nextNode) { this.nextNode = nextNode; }

    public int getBounces(){ return bounces; }
    public void setBounces(int bounces) { this.bounces = bounces; }

    public int getStatus(){ return status; }
    public void setStatus(int status) { this.status = status; }


    //persistent information
    private String name; //user name
    private boolean favorite; //whether or not the user is into Favorites category
    private int age, gender, country;
    private String profilePic;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean isFav(){ return favorite; }
    public void setFav(boolean fav) { this.favorite = fav; }

    public int getCountry() { return country; }
    public void setCountry(int code) { this.country = code; }

    public int getGender() { return this.gender; }
    public void setGender(int gender) { this.gender = gender; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getProfilePic() { return this.profilePic; }
    public void setProfilePic(String profilePic) { this.profilePic = profilePic; }


    public ChatUser() {  }

    public ChatUser(String mac, BluetoothDevice nextNode, int bounces, int status, Cursor cursor) {

        setMac(mac);
        setMac(BlueCtrl.macToBytes(mac));
        setNextNode(nextNode);
        setBounces(bounces);
        setStatus(status);

        if (cursor != null) {
            addPersistentInfo(cursor);
        }
    }

    public void addPersistentInfo(Cursor profileInfo) {
        profileInfo.moveToFirst();
        setName(profileInfo.getString(2));
        setLastUpd(profileInfo.getLong(3));
        setFav(1 == profileInfo.getInt(4));
        setProfilePic(profileInfo.getString(5));
        setCountry(profileInfo.getInt(6));
        setGender(profileInfo.getInt(7));
        setAge(profileInfo.getInt(8));
    }

    public boolean updateUser(BluetoothDevice dvc, int bnc, int sts) {

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
