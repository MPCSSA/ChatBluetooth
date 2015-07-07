package com.example.christian.chatbluetooth.controller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.example.christian.chatbluetooth.view.Activities.ChatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MessageThread extends Thread {

    private BluetoothSocket sckt;
    private byte type;
    private byte[] msg;
    private BluetoothDevice rmtDvc;
    private InputStream in;
    private OutputStream out;
    private Handler handler = null;

    public BluetoothSocket getSckt() {
        return sckt;
    }
    public void setSckt(BluetoothSocket sckt) {
        this.sckt = sckt;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public byte[] getMsg() {
        return msg;
    }
    public void setMsg(byte[] msg) { this.msg = msg; }

    public BluetoothDevice getDevice() { return this.rmtDvc; }
    public void setDevice(BluetoothDevice device) { this.rmtDvc = device; }

    public InputStream getIn() {
        return in;
    }
    public void setIn(InputStream in) {
        this.in = in;
    }

    public OutputStream getOut() {
        return out;
    }
    public void setOut(OutputStream out) {
        this.out = out;
    }

    public Handler getHandler() { return this.handler; }
    public void setHandler(Handler handler) { this.handler = handler; }

    public MessageThread(BluetoothDevice dvc, byte[] msg, Handler handler) {
        this(dvc, msg);

        setHandler(handler);


    }

    public MessageThread(BluetoothDevice dvc, byte[] msg) {

        BluetoothSocket sckt = null;

        try{
            //initiate communication
            sckt = dvc.createInsecureRfcommSocketToServiceRecord(java.util.UUID.fromString(BlueCtrl.UUID));
            System.out.println("hello pippo baudo");
        }
        catch (IOException ignore) {}
        catch (NullPointerException ignore) {}

        setSckt(sckt);
        setDevice(dvc);
        setMsg(msg);
        setType(msg[0]);

        InputStream in = null;
        OutputStream out = null;
        try {

            in = this.sckt.getInputStream();
            out = this.sckt.getOutputStream();

        }
        catch (IOException ignore) {}

        setIn(in);
        setOut(out);
    }

    public void run() {

        try {

            BlueCtrl.lockDiscoverySuspension();

            System.out.println("TRYING TO CONNECT TO " + rmtDvc.getAddress());
            sckt.connect();

            System.out.println("Connected to " + rmtDvc.getAddress());
            boolean waiting = false;

            out.write(msg);

            System.out.println(type + " message sent");
            /*
            Instant Reply
            After sending the message, this device maintains connection, listening to the remote device
            response. The remote device sends an ACK msg if no more information is needed; it can also
            forward an Info Request (RQS) msg in response to a GRT or UPD msg OR an Update msg in response
            to a Drop Request. Connection is ended when this device receives an ACK msg.
             */
            int ack;

            while ((ack = in.read()) != BlueCtrl.ACK) {

                System.out.println(ack + " message read");
                int i, j;
                byte[] buffer = new byte[6];
                boolean breakFree;
                String address;

                switch(ack) {

                    case BlueCtrl.RQS_HEADER:
                    /*
                    An Info Request is a special request sent by a node which received a reachable MAC
                    address through an Update Msg, but no results were found in the Users table. It
                    contains MAC address of unknown device and a RQS identifier (0). A Card Msg follows,
                    containing persistent user information about it.
                    A second check is performed to establish if a picture update is also needed. The remote
                    device will send another request, this time with the identifier set to 1.
                    No divider is needed, because every MAC address is made up of exactly 6 bytes.
                    [2][MAC][id]
                     */

                        System.out.println("information requested");
                        i = 0;
                        do {
                            j = in.read(buffer, i, 6 - i);
                            if (j < 0) {
                                System.out.println("Premature EOF, message misunderstanding");
                                //TODO: throw something
                            }
                            i += j;
                        } while (i < 6);

                        if (in.read() == 0) {

                            address = BlueCtrl.bytesToMAC(buffer);

                            byte[] card = BlueCtrl.buildCard(BlueCtrl.fetchPersistentInfo(address));
                            out.write(card);
                            System.out.println(address + " CARD SENT TO " + rmtDvc.getAddress());
                        }
                        else {

                            //TODO: fetch profile picture
                        }

                        break;

                    case BlueCtrl.UPD_HEADER:

                        byte[] lastUpd = new byte[8];
                        byte b, status;
                        int bounces;
                        breakFree = true;

                        while (breakFree) {

                            i = 0;
                            do {
                                j = in.read(buffer, i, 6 - i);
                                if (j < 0) {
                                    System.out.println("Premature EOF, message misunderstanding");
                                    //TODO: throw something
                                }
                                i += j;
                            } while (i < 6);

                            b = (byte) in.read();
                            bounces = (b < 0) ? b + 256 : (int) b;
                            status = (byte) in.read();

                            i = 0;
                            do {
                                j = in.read(lastUpd, i, 8 - i);
                                if (j < 0) {
                                    switch (i) {
                                        case 0:
                                            breakFree = false; //EOF reached
                                            break;
                                        default:
                                            System.out.println("Premature EOF, message misunderstanding");
                                            //TODO: throw something
                                    }

                                    break;
                                }
                                i += j;
                            } while (i < 8);

                            address = BlueCtrl.bytesToMAC(buffer);
                            long timestamp = BlueCtrl.rebuildTimestamp(lastUpd);

                            if (BlueCtrl.validateUser(address, timestamp)) {

                                if (BlueCtrl.awakeUser(address, rmtDvc, status, bounces + 1, timestamp))
                                    handler.sendEmptyMessage(BlueCtrl.ACK_UPD);
                            /*
                            New route to address target
                            */
                            } else {
                                System.out.println("Misunderstanding");
                                //TODO: Misunderstanding management
                            }
                            /*
                            This is controlled environment; if an UPD msg is received, it has to refer to
                            a ChatUser this device dropped, therefore no RQS if needed because this device
                            certainly has got the user information
                             */
                        }

                        break;

                    default:
                        System.out.println("Misunderstanding");
                        //TODO: Misunderstanding management
                }

            }

            System.out.println("ACKED");

            BlueCtrl.unlockDiscoverySuspension();

            if (handler != null) {

                handler.sendEmptyMessage(type + 8); //ACK values are 8 digits shifted
            }
            if (type == BlueCtrl.GRT_HEADER) {
                BlueCtrl.closeDvc.add(rmtDvc);
            }

            if (waiting) out.close();
            in.close();


        }
        catch(Exception e) {

            e.printStackTrace();
            System.out.println("alimortaccitua " + rmtDvc.getAddress());
            BlueCtrl.unlockDiscoverySuspension();

            if (handler != null) {

                Message error = new Message();
                error.what = BlueCtrl.NAK;
                Bundle bundle = new Bundle();
                bundle.putString("dvc", rmtDvc.getAddress());
                bundle.putByteArray("msg", getMsg());
                bundle.putInt("errorcode", type - 8);
                error.setData(bundle);

                handler.sendMessage(error);
            }
            try {
                sckt.close();
            }
            catch(IOException ignore) {}
        }

        cancel();
    }

    public void cancel() {

        try {

            sckt.close();

        }
        catch(IOException ignore) {}
    }
}
