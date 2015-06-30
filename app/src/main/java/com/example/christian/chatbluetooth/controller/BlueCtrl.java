package com.example.christian.chatbluetooth.controller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import com.example.christian.chatbluetooth.model.BlueDBManager;
import com.example.christian.chatbluetooth.model.ChatMessage;
import com.example.christian.chatbluetooth.model.ChatUser;
import com.example.christian.chatbluetooth.view.MessageAdapter;
import com.example.christian.chatbluetooth.view.RecycleAdapter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class BlueCtrl {

    public static final byte GRT_HEADER = (byte) 0; //header for Greetings Message
    public static final byte UPD_HEADER = (byte) 1; //header for Update Message
    public static final byte RQS_HEADER = (byte) 2; //header for Info Request
    public static final byte CRD_HEADER = (byte) 3; //header for Card Message
    public static final byte MSG_HEADER = (byte) 4; //header for Chat Message
    public static final byte DRP_HEADER = (byte) 5; //header for Drop Request
    public static final byte        ACK = (byte) 6; //ACKnowledge Message for communication synchronization
    public static final String     UUID = "BlueRoom"; //custom UUID
    public static File appFolder;

    public static boolean DISCOVERY_SUSPENDED = false;
    public static int DISCOVERY_LOCK = 0; //l'ultimo che esce chiude la porta

    private static SharedPreferences currentUser;
    private static RecycleAdapter userAdapt;        //ChatUser Adapter; initialized on MainActivity creation
    private static ArrayList<BluetoothDevice> closeDvc = new ArrayList<>();
    private static int counter = 0;
    private static BlueDBManager dbManager;          //User and Messages DB Manager
    private static final String dbname = "bluedb"; //DB name
    public static MessageAdapter msgAdapt;

    public static void setUserAdapt(RecycleAdapter recycleAdapter) {
        BlueCtrl.userAdapt = recycleAdapter;
    }

    public static void setDbManager(BlueDBManager dbManager) {
        BlueCtrl.dbManager = dbManager;
    }

    public static void bindUser(SharedPreferences sh) {
        currentUser = sh;
    }

    public static void fillMsgAdapter(){

        Cursor cursor = fetchMsgHistory(msgAdapt.getAddress());
        if (cursor.getCount() > 0){
            cursor.moveToFirst();
            do{

                msgAdapt.add(new ChatMessage(cursor.getString(0), cursor.getInt(2), cursor.getLong(1)));
            } while(cursor.moveToNext());
        }
    }

    public static void sendMsg(BluetoothDevice dvc, byte[] msg) {

        (new MessageThread(dvc, msg)).start();
    }

    public static void greet(BluetoothDevice dvc) {
        byte[] grt = new byte[10], timestamp = longToBytes(currentUser.getLong("lastUpd", (new Date()).getTime()));
        grt[0] = BlueCtrl.GRT_HEADER;
        grt[1] = (byte) currentUser.getInt("status", 1); //1 = disponibile

        for(int i = 2; i < 10; ++i) {
            grt[i] = timestamp[i-2];
        }

        sendMsg(dvc, grt);
    }
    public static void dispatchNews(byte[] msg, BluetoothDevice filter) {
        //dispatch message to all close devices;
        //used for GRT, UPD, DRP messages
        for(BluetoothDevice dvc : BlueCtrl.closeDvc) {
            if (!dvc.equals(filter))
                sendMsg(dvc, msg);
        }
    }

    public static ChatUser scanUsers(String address) {

        return BlueCtrl.userAdapt.getItem(address);
    }

    public static void getUserList() {

        //TODO: make bluetooth discovery and update model ChatUser Adapter

    }

    public static void retrieveHistory(String username) {

        //TODO: fetch msg history via DBManager

    }

    public static ChatUser manageDropRequest(String address, String macs) {

        //TODO: Drop Request management
        return null;
    }

    public static void addChatUser(byte[] mac, BluetoothDevice next, int bounces, String name, byte status) {

        //TODO: create new ChatUser object and add it to ChatUser Adapter

    }

    public static boolean awakeUser(String mac, BluetoothDevice manInTheMiddle, byte status, int bounces) {

        ChatUser user = scanUsers(mac);
        if (user != null) {
            if (user.updateUser(manInTheMiddle, bounces, (int) status)) {
                userAdapt.notifyDataSetChanged();
                return true;
            }
            return false;
        }
        return BlueCtrl.userAdapt.add(new ChatUser(mac, manInTheMiddle, bounces, status, dbManager.fetchUserInfo(mac)));

    }

    public static void cardUpdate(String address, byte[] image) {
        try {

            Cursor info = fetchPersistentInfo(address);
            scanUsers(address).addPersistentInfo(info);

            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

                String pic = info.getString(5);
                if (pic == null) {
                    dbManager.updatePicture(address);
                }
                File directory = new File(BlueCtrl.appFolder, Environment.DIRECTORY_PICTURES);
                OutputStream outputStream = new FileOutputStream(directory.getAbsolutePath() + "/IMG_" + address);
                outputStream.write(image);

            }

            userAdapt.notifyDataSetChanged();
        } catch (NullPointerException ignore) {}
        catch (IOException ignore) {}
    }

    public static boolean addCloseDvc(BluetoothDevice dvc) {

        boolean newcomer;
        int pos;
        if ((pos = closeDvc.indexOf(dvc)) != -1) {
            //swap
            if (pos != counter) {
                closeDvc.set(pos, closeDvc.get(counter));
                closeDvc.set(counter, dvc);
            }
            newcomer = false;
        }
        //insert into position
        else {
            closeDvc.add(counter, dvc);
            newcomer = true;
        }

        ++counter;
        return newcomer;
    }

    public static BluetoothDevice cleanCloseDvc() {

        if (closeDvc.size() > counter) return closeDvc.remove(counter);
        return null;
    }

    public static void showMsg(String from, String msg, Date time, int sentBy){

        dbManager.createRecord(1, new Object[] {msg, from, time.getTime(), sentBy});
        if (from.equals(msgAdapt.getAddress())){
            msgAdapt.add(new ChatMessage(msg, sentBy, time));
            msgAdapt.notifyDataSetChanged();
        }
    }

    //TODO: Build Message routines

    public static byte[] buildMsg(byte[] target, byte[] sender, byte[] msg) {
        /*
        Use this method to prepare the packet to forward to a Bluetooth device.
        Target param is the MAC address of target device, NOT the Bluetooth device receiving the packet from
        this device. Sender param is the sender's MAC address.
        */

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

        return pckt;
    }

    public static byte[] buildUpdMsg(ChatUser user) {
        //For DROP REQUEST Instant Reply only

        byte[] upd = new byte[16], lastUpd;

        upd[0] = BlueCtrl.UPD_HEADER;

        int i = 1, j;
        for (j = 0; j < 6; ++j) {
            upd[i + j] = user.getMacInBytes()[j];
        }
        i += j;

        lastUpd = longToBytes(user.getLastUpd());
        for (j = 0; j < 8; ++j) {
            upd[i + j] = lastUpd[j];
        }
        i += j;

        upd[i] = (byte) (user.getBounces());
        ++i;

        upd[i] = (byte) user.getStatus();

        return upd;
    }

    public static byte[] buildUpdMsg(List<byte[]> updCascade) {
        //Multi-user Update Message

        byte[] upd = new byte[2 + 16 * updCascade.size()];

        upd[0] = BlueCtrl.UPD_HEADER;
        upd[1] = (byte) updCascade.size();

        for (int i = 0; i < updCascade.size(); ++i) {
            for (int j = 0; j < 16; ++j) {
                upd[2 + i * 16 + j] = updCascade.get(i)[j];
            }
        }

        return upd;
    }

    public static byte[] buildCard(Cursor info) {

        info.moveToFirst();
        String mac = info.getString(1), username = info.getString(2);
        long timestamp  = info.getLong(3);
        String profile_pic = info.getString(5);
        int country = info.getInt(6), gender = info.getInt(7), age = info.getInt(8);

        byte[] address = macToBytes(mac), user = username.getBytes(), lastUpd = longToBytes(timestamp),
                pic = extractImage(profile_pic), card = new byte[20 + user.length + pic.length];

        int i = 0, j;
        card[i] = BlueCtrl.CRD_HEADER;
        ++i;

        for (j = 0; i < 6; ++j) {
            card[i + j] = address[j];
        }
        i += j;

        for (j = 0; i < 8; ++j) {
            card[i + j] = lastUpd[j];
        }
        i += j;

        card[i] = (byte) age;
        ++i;
        card[i] = (byte) gender;
        ++i;
        card[i] = (byte) country;
        ++i;

        card[i] = (byte) user.length;
        ++i;
        for (j = 0; j < user.length; ++j) {
            card[i + j] = user[j];
        }
        i += j;

        card[i] = (byte) pic.length;
        ++i;
        for (j = 0; j < pic.length; ++j) {
            card[i + j] = pic[j];
        }

        return card;
    }


    //TODO: DB Operations

    public static void openDatabase(Context context) {

        dbManager = new BlueDBManager(context, dbname);

    }

    public static boolean validateUser(String address, long timestamp) {

        Cursor cursor = dbManager.fetchTimestamp(address);
        return (cursor.moveToFirst() && cursor.getLong(0) == timestamp);
    }

    public static void updateUserTable(String mac, long timestamp, String username, int age, int gender, int country){

        dbManager.updateUserInfo(mac, username, null, country, gender, age, timestamp);

    }

    public static Cursor fetchPersistentInfo(String address) {

        return dbManager.fetchUserInfo(address);

    }

    //TODO: Utils

    public static byte[] macToBytes(String address) {

        if (BluetoothAdapter.checkBluetoothAddress(address)) {

            byte[] mac = new byte[6];
            String[] digits = address.toLowerCase().split(":");
            byte b, counter = 0;

            for(String d : digits) {
                b = (byte) Integer.parseInt(d, 16);
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

    public static byte[] extractImage(String path) {

        Bitmap bmp = BitmapFactory.decodeFile(path);
        OutputStream outputStream = new ByteArrayOutputStream();

        bmp.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

        return ((ByteArrayOutputStream)outputStream).toByteArray();
    }

    public static Bitmap rebuildImage(byte[] image) {

        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }

    public static Collection dropUsers(BluetoothDevice dvc) {
        return userAdapt.dropUsers(dvc);
    }

    public static Cursor fetchMsgHistory(String address){
        return dbManager.fetchMsgHistory(address);
    }
}
