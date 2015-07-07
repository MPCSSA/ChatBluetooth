package com.example.christian.chatbluetooth.model;


import java.text.DateFormat;
import java.util.Date;

public class ChatMessage {
    private String msg;
    private boolean sentBy;
    private Date date;
    private boolean isEmo;

    public ChatMessage(String msg, boolean sentBy, long time, boolean isEmo){
        this(msg, sentBy, new Date(time), isEmo);
    }

    public ChatMessage(String msg, boolean sentBy, Date date, boolean isEmo){
        this.msg = msg;
        this.sentBy = sentBy;
        this.date = date;
        this.isEmo = isEmo;
    }

    public String getMsg(){
        return this.msg;
    }

    public boolean getSender(){
        return this.sentBy;
    }

    public Date getDate(){
        return date;
    }

    public boolean isEmo() { return this.isEmo; }
}
