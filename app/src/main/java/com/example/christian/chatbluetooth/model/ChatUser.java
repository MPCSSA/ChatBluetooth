package com.example.christian.chatbluetooth.model;

/**
 * Created by stefano on 10/06/15.
 */
public class ChatUser {

    //TODO: observer mechanism

    private String name; //user name
    private int status; //user status
    private String mac; //device chain next node MAC address
    private int chain; //number of chained devices before actual user
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

    public int getChain(){
        return chain;
    }
    public void setChain(int chain) {
        this.chain = chain;
    }

}
