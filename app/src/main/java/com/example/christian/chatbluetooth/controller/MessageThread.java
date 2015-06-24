package com.example.christian.chatbluetooth.controller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.database.Cursor;

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
    public void setMsg(byte[] msg) {
        this.msg = msg;
    }

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

    public MessageThread(BluetoothSocket sckt, byte[] msg) {
        setSckt(sckt);
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

            sckt.connect();
            boolean waiting = false;

            switch (type) {

                case BlueCtrl.CRD_HEADER:
                    BlueCtrl.lockDiscoverySuspension();
                    out.write(msg);
                    BlueCtrl.unlockDiscoverySuspension();
                    out.close();
                    break;
                case BlueCtrl.MSG_HEADER:
                    BlueCtrl.lockDiscoverySuspension();
                    out.write(msg);
                    BlueCtrl.unlockDiscoverySuspension();
                    out.close();
                    break;
                case BlueCtrl.DRP_HEADER:
                    out.write(msg);
                    out.close();
                default:
                    out.write(msg);
                    waiting = true;

            }

            /*
            Instant Reply
            After sending the message, this device maintains connection, listening to the remote device
            response. The remote device sends an ACK msg if no more information is needed; it can also
            forward an Info Request (RQS) msg in response to a GRT or UPD msg OR an Update msg in response
            to a Drop Request. Connection is ended when this device receives an ACK msg.
             */
            int ack;
            while ((ack = in.read()) != BlueCtrl.ACK) {

                int i, j;
                byte[] buffer = new byte[6];
                boolean breakFree;
                String address;

                switch(ack) {
                    case BlueCtrl.RQS_HEADER:
                    /*
                    An Info Request is a special request sent by a node which received a reachable MAC
                    address through an Update Msg, but no results were found in the Users table. It
                    contains MAC address of unknown device. A Card Msg follows, containing persistent user
                    information about it.
                    No divider is needed, because every MAC address is made up of exactly 6 bytes.
                    [2][MAC]
                     */
                        breakFree = true;

                        while (breakFree) {
                            i = 0;

                            do {
                                j = in.read(buffer, i, 6 - i);
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
                            } while (i < 6);

                            address = BlueCtrl.bytesToMAC(buffer);

                            byte[] card = BlueCtrl.buildCard(BlueCtrl.fetchPersistentInfo(address));
                            BlueCtrl.lockDiscoverySuspension();
                            out.write(card);
                            BlueCtrl.unlockDiscoverySuspension();
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
                            if (BlueCtrl.validateUser(address, BlueCtrl.rebuildTimestamp(lastUpd))) {

                                BlueCtrl.awakeUser(buffer, address, status, bounces + 1);
                            /*
                            New route to address target
                            */
                            } else {
                                System.out.println("Misunderstanding");
                                //TODO: Misunderstanding management
                            }
                            /*
                            This is controlled environment; if an UPD msg is received, it has to refer to
                            a ChatUser this device dropped, therefore no RQS if needed beacuse this device
                            certainly has got the user information
                             */
                        }

                        break;

                    default:
                        System.out.println("Misunderstanding");
                        //TODO: Misunderstanding management
                }

            }

            if (waiting) out.close();
            in.close();

        }
        catch(IOException e) {

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
