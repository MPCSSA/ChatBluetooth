package com.example.christian.chatbluetooth.controller;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MessageThread extends Thread {

    private BluetoothSocket sckt; //Socket for Bluetooth communication
    private byte type; //first byte of the message to send
    private byte[] msg; //byte array
    private BluetoothDevice rmtDvc; //Remote BluetoothDevice to communicate to
    private InputStream in; //InputStream for ACK listening
    private OutputStream out; //OutputStream for message delivery
    private Handler handler = null; //Handler for main thread communication service


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

    public void setDevice(BluetoothDevice device) { this.rmtDvc = device; }

    public InputStream getIn() {
        return in;
    }
    public void setIn(InputStream in) {
        this.in = in;
    }

    public void setOut(OutputStream out) { this.out = out; }

    public void setHandler(Handler handler) { this.handler = handler; }

    //CONSTRUCTOR
    public MessageThread(BluetoothDevice dvc, byte[] msg, Handler handler) {

        this(dvc, msg); //Set up socket communication da structures

        setHandler(handler); //Set up main thread communication service
    }

    //CONSTRUCTOR
    public MessageThread(BluetoothDevice dvc, byte[] msg) {

        BluetoothSocket sckt = null;

        try{
            //initiate communication
            sckt = dvc.createInsecureRfcommSocketToServiceRecord(java.util.UUID.fromString(BlueCtrl.UUID));
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        setSckt(sckt);
        setDevice(dvc);
        setMsg(msg);
        setType(msg[0]);
        //Set up Bluetooth communication data structures

        InputStream in = null;
        OutputStream out = null;
        try {

            in = this.sckt.getInputStream();
            out = this.sckt.getOutputStream();

        }
        catch (IOException ignore) {}

        setIn(in);
        setOut(out);
        //Set up streamers for actual communication
    }

    public void run() {

        try {

            /*if (type != BlueCtrl.ACK) */BlueCtrl.lockDiscoverySuspension();
            //ACK messages are only 1 byte long, no need to put down discovery. Right?

            sckt.connect();
            out.write(msg);

            /*
            Instant Reply
            After sending the message, this device maintains connection, listening to the remote device
            response. The remote device sends an ACK msg if no more information is needed; it can also
            forward an Info Request (RQS) msg in response to a GRT or UPD msg OR an Update msg in response
            to a Drop Request. Connection is ended (without exception occurrence) when this device receives an ACK msg.
             */
            int ack;
            int i, j;
            byte[] buffer = new byte[6];
            String address;
            //Useful objects

            while ((ack = in.read()) != BlueCtrl.ACK) {

                switch(ack) {

                    case BlueCtrl.RQS_HEADER:
                    /*
                    An Info Request is a special request sent by a node which received a reachable MAC
                    address through a Greetings or Update Msg, but no results were found in the Users table. It
                    contains MAC address of unknown device. A Card Msg follows, containing persistent user
                    information about it.
                    [2][MAC]
                     */

                        i = 0;
                        do {
                            j = in.read(buffer, i, 6 - i);
                            if (j < 0) throw new IOException(); //Misunderstanding
                            i += j;
                        } while (i < 6);
                        //MAC address of unknown user

                        address = BlueCtrl.bytesToMAC(buffer); //MAC address

                        byte[] card = BlueCtrl.buildCard(BlueCtrl.fetchPersistentInfo(address));
                        //Building response message

                        out.write(card);
                        break;

                    case BlueCtrl.GRT_HEADER:
                        /*
                        The remote device sends this NAK when this device is not on its list of known
                        reachable devices. The remote device will then skip the received message and wait for
                        a Greetings message instead. In order to restore network and communication, this
                        device needs to send a Greetings message, then its previous message will be
                        accepted.
                        This message does not contain any more bits than the header itself.
                         */

                        out.write(BlueCtrl.buildGrtMsg()); //greetings routine
                        out.write(msg);
                        break;

                    case BlueCtrl.UPD_HEADER:
                        /*
                        An Update Instant Reply is received after signaling a Drop Request; if remote user
                        knows an alternative route to the lost device, it immediately builds and sends
                        back its segment, while keeping reading the Drop message. The Update Instant
                        Reply lacks the Number field, because these are single replies and the number
                        of segments (one) is implicit.
                        [1][  MAC  ][last update ][bounces][status ]
                          |6 bytes |   8 bytes   | 1 byte | 1 byte |
                         */

                        byte[] lastUpd = new byte[8];
                        byte status;
                        int bounces;

                        i = 0;
                        do {
                            j = in.read(buffer, i, 6 - i);
                            if (j < 0) throw new IOException(); //Misunderstanding
                            i += j;
                        } while (i < 6);
                        //MAC address

                        bounces = in.read();
                        status = (byte) in.read();

                        i = 0;
                        do {
                            j = in.read(lastUpd, i, 8 - i);
                            if (j < 0) throw new IOException(); //Misunderstanding
                            i += j;
                        } while (i < 8);
                        //Last Profile Update timestamp

                        address = BlueCtrl.bytesToMAC(buffer);
                        long timestamp = BlueCtrl.rebuildTimestamp(lastUpd);

                        if (BlueCtrl.validateUser(address, timestamp)) {

                            if (BlueCtrl.awakeUser(address, rmtDvc, status, bounces + 1, timestamp)) {

                                Message mail = new Message();
                                Bundle bundle = new Bundle();
                                bundle.putString("MAC", rmtDvc.getAddress());
                                mail.setData(bundle);
                                mail.what = BlueCtrl.UPD_HEADER;
                                handler.sendMessage(mail);
                            }
                        /*
                        New route to address target
                        */
                        }
                        else throw new IOException(); //Misunderstanding
                        /*
                        This is controlled environment; if an UPD msg is received, it has to refer to
                        a ChatUser this remote device dropped, therefore no RQS if needed because this device
                        certainly has got the user information
                        */

                        break;

                    default: throw new IOException(); //Misunderstanding
                }
            }

            /*if (type != BlueCtrl.ACK) */BlueCtrl.unlockDiscoverySuspension(); //Release lock

            if (type == BlueCtrl.GRT_HEADER) {

                BlueCtrl.closeDvc.put(rmtDvc.getAddress(), rmtDvc);
                BlueCtrl.tokenMap.put(rmtDvc.getAddress(), BlueCtrl.TKN);
                //It is quite difficult to pass BluetoothDevices to ChatActivity, so they are added to data structures right here
            }

            Message mail = new Message();
            Bundle bundle = new Bundle();
            bundle.putString("MAC", rmtDvc.getAddress());
            mail.setData(bundle);
            mail.what = BlueCtrl.ACK;
            handler.sendMessage(mail);
            //send ACK

            out.close();
            in.close();
            //End Connection
        }
        catch(Exception e) {

            e.printStackTrace();

            /*if (type != BlueCtrl.ACK) */BlueCtrl.unlockDiscoverySuspension();


            Message mail = new Message();
            Bundle bundle = new Bundle();
            bundle.putString("MAC", rmtDvc.getAddress());
            mail.setData(bundle);mail.what = BlueCtrl.NAK;
            mail.getData().putByteArray("MSG", getMsg());
            mail.getData().putInt("ERRORCODE", type); //deprecated
            //Error Message

            handler.sendMessage(mail);

            try {
                sckt.close();
            }
            catch(IOException ignore) {}
        }

        cancel();
    }

    public void cancel() {
        //Close Connection

        try {

            sckt.close();
        }
        catch(IOException ignore) {}
    }
}
