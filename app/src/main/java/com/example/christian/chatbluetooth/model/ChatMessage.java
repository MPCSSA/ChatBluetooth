package com.example.christian.chatbluetooth.model;


import java.text.DateFormat;
import java.util.Date;

public class ChatMessage {
    private String msg;
    private int sentBy;
    private Date date;

    public ChatMessage(String msg, int sentBy, long time){
        this(msg, sentBy, new Date(time));
    }

    public ChatMessage(String msg, int sentBy, Date date){
        this.msg = msg;
        this.sentBy = sentBy;
        this.date = date;
    }

    public String getMsg(){
        return this.msg;
    }

    public int getSender(){
        return this.sentBy;
    }

    public String getDate(){
        return date.toString();
    }
}
