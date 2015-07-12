package com.example.christian.chatbluetooth.model;

import java.util.Date;

public class ChatMessage {
    //This class encapsulates all information required to properly manage the GUI and the DB

    private Integer id;
    /*
    id of the record in the database; required to properly delete the message; can be null, because
    it is not needed most of the time
    */
    private String msg; //Actual Text Message or Emoticon code
    private String username; //Username of remote sender/receiver
    private boolean sentBy; //sent by remote user?
    private Date date; //When the message has been received/sent
    private boolean isEmo; //is it an Emoticon?

    public ChatMessage(String msg, boolean sentBy, long time, boolean isEmo){
        this(msg, sentBy, new Date(time), isEmo);
    }

    public ChatMessage(String msg, boolean sentBy, Date date, boolean isEmo){
        this(msg, null, sentBy, date, isEmo);
    }

    public ChatMessage(String msg, String username, boolean sentBy, Date date, boolean isEmo) {
        this(msg, username, null, sentBy, date, isEmo);
    }

    public ChatMessage(String msg, String username, Integer id, boolean sentBy, Date date, boolean isEmo) {
        //Base constructor; never called directly, because some fields can be null in certain contexts

        this.msg = msg;
        this.username = username;
        this.id = id;
        this.sentBy = sentBy;
        this.date = date;
        this.isEmo = isEmo;
    }

    public String getMsg(){ return this.msg; }

    public String getUsername() { return this.username; }

    public Integer getId() { return this.id; }
    public void setId(Integer id) { this.id = id; }

    public boolean getSender(){ return this.sentBy; }

    public Date getDate(){ return date; }

    public boolean isEmo() { return this.isEmo; }

    public void setSentBy(boolean sentBy) { this.sentBy = sentBy; }
}
