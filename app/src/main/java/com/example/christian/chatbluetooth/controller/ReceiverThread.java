package com.example.christian.chatbluetooth.controller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;

public class ReceiverThread extends Thread {

    private BluetoothSocket sckt; //socket for Bluetooth communication
    private InputStream in;       //InputStream object from which reading incoming messages
    private String rmtDvc;        //MAC address of communicating Bluetooth device

    public void setSckt(BluetoothSocket sckt) {
        this.sckt = sckt;
    }

    public ReceiverThread(BluetoothSocket sckt) {
        setSckt(sckt);
        InputStream tmp = null;

        try {
            tmp = this.sckt.getInputStream();
        }
        catch (IOException ignore) {}

        in = tmp;

        rmtDvc = sckt.getRemoteDevice().getAddress();
    }

    public void run() {

        try {

            byte flag = (byte) in.read();
            /*
            incoming message flag;
            0: Greetings Message; a newly connected device is sending informations about itself.
            1: Update Message; a device is transmitting informations about remote devices that are accessible through itself.
            2: Chat Message; if this device is the target, show message in chat; else, forward the message on the route
            3: Drop Request; a list of devices no longer reachable due to one device disconnection
            */
            byte[] bytes = new byte[128]; //buffer

            int i;
            String msg = null;

            do {
                //extract message

                i = in.read(bytes);
                if (msg == null) msg = new String(bytes);
                else msg = msg.concat(new String(bytes));

            } while (i == 128);

            String[] fields = msg.split("\0");
            //split fields in the message; every kind of message has its fields, as described below

            switch (flag) {
                case BlueCtrl.GRT_HEADER: {
                    /*
                    A Greetings message contains username and user status informations separated by NUL character
                    [0][name]'\0'[status]
                    */

                    if (fields.length == 2)  {
                        BlueCtrl.addChatUser(rmtDvc, null, 0, fields[0], Integer.parseInt(fields[1]));
                        //a new ChatUser instance is created; connection with remote device is direct,
                        //so no Man in the Middle is needed for delivery and bounces are 0
                    }
                    else {
                        System.out.println("Greetings failed");
                        //TODO: optional Greetings misunderstanding recovery
                    }
                    break;
                }

                case BlueCtrl.UPD_HEADER: {

                    String[] infos;
                    /*
                    An Update message can contain information about multiple users; user informations
                    include target MAC, bounces, username and user status. User fields are
                    divided by NUL character, whilst user informations are separated by commas.
                    [1][MAC];[bounces];[name];[status]'\0'{user}'\0'{user}...
                    */

                    for (String info : fields) {
                        infos = info.split(";"); //separate informations
                        BlueCtrl.addChatUser(infos[0], rmtDvc, Integer.parseInt(infos[1]) + 1,
                                             infos[2], Integer.parseInt(infos[3]));
                        /*
                        add a new ChatUser object to the adapter; target MAC, username and status
                        are passed unadulterated to the builder, while bounce field is increased by 1
                        because the remote device represents an additional node to bounce on
                         */

                    }

                    break;
                }

                case BlueCtrl.MSG_HEADER: {
                    /*
                    A Chat message is the kind of message which is ultimately shown to the user upon reception.
                    It wraps up a message from the sender in a packet consisting of a header, a target MAC address,
                    a sender MAC address and a message field. Target MAC address is the  of the actual recipient of
                    the message MAC address, whilst the sender MAC address is the original sender device MAC address.
                    NUL character divides every field of the message.
                    [2][Target MAC]'\0'[Sender MAC]'\0'[Message field]
                     */
                    if (fields.length == 3) {

                        if (fields[0].equals(BluetoothAdapter.getDefaultAdapter().getAddress())) {
                            BlueCtrl.buildMsg(fields[1], fields[2]);
                            //show message on screen
                        } else {
                            BlueCtrl.sendMsg(fields[0], fields[1], fields[2]);
                        /*
                        if this device is not the target device, message has to be forwarded to the next node
                        on the route leading to the target; it is wrapped again in a packet and sent as a
                         */
                        }
                    }
                    else {
                        System.out.println("Message failed");
                        //TODO: optional Message misunderstanding handler
                    }
                    break;
                }

                case BlueCtrl.DRP_HEADER: {
                    /*
                    A Drop Request is sent when a device closes its connection and is no longer reachable.
                    It contains a list of MACs of devices no longer reachable by this device due to
                    the fact that their routes passed through the exiting node. NUL character divides
                    every MAC address.
                    [3][MAC]'\0'[MAC]'\0'...
                    */

                    BlueCtrl.manageDropRequest(rmtDvc, fields);
                    break;
                }
            }
        }
        catch (IOException e) {
            cancel();
        }
        catch(NullPointerException ignore) {}

        cancel();
    }

    private void cancel() {
        //close connection

        try{
            sckt.close();
        }
        catch (IOException ignore) {}
    }
}
