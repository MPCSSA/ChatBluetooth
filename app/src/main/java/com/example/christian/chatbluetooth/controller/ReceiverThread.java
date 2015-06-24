package com.example.christian.chatbluetooth.controller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.database.Cursor;

import com.example.christian.chatbluetooth.model.ChatUser;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ReceiverThread extends Thread {

    private BluetoothSocket sckt; //socket for Bluetooth communication
    private InputStream in;       //InputStream object from which reading incoming messages
    private OutputStream out;     //OutputStream object from which reading ACK messages
    private BluetoothDevice rmtDvc;        //MAC address of communicating Bluetooth device

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

        rmtDvc = sckt.getRemoteDevice();
    }

    public void run() {

        try {

            int i, j;
            boolean connected = true;

            do {

                byte flag = (byte) in.read();
            /*
            incoming message flag;
            0: Greetings Message; a newly connected device is sending informations about itself.
            1: Update Message; a device is transmitting informations about remote devices that are accessible through itself.
            2: Info Request; a device has got inconsistent or no information about a target device and it requests an heavy update.
            3: Card Message; an heavy response to an Info Request; it contains persistent user information.
            4: Chat Message; if this device is the target, show message in chat; else, forward the message on the route.
            5: Drop Request; a list of devices no longer reachable due to one device disconnection.
            */
                switch (flag) {
                    case BlueCtrl.GRT_HEADER: {
                    /*
                    A Greetings message contains user status and last update field; MAC address is implicit,
                    and status and last update field have fixed length (1 byte and 8 byte long),
                    therefore no divider is needed.
                    [0][status][last update]
                       |1 byte|   8 byte  |
                    */

                        byte status = (byte) in.read();
                        byte[] bytes = new byte[8];
                        i = 0;

                        do {
                            j = in.read(bytes, i, 8 - i);
                            if (j < 0) {
                                System.out.println("Premature EOF, message misunderstanding");
                                //TODO: throw something
                            }
                            i += j;
                        } while (i < 8);

                        long lastUpd = BlueCtrl.rebuildTimestamp(bytes);

                        if (BlueCtrl.validateUser(rmtDvc.getAddress(), lastUpd)) {

                            BlueCtrl.awakeUser(BlueCtrl.macToBytes(rmtDvc.getAddress()), rmtDvc.getAddress(), status, 0);
                            //a new ChatUser object is created from pre-existing information in addition
                            //to volatile fields
                            out.write(BlueCtrl.ACK);
                        } else {
                            out.write(BlueCtrl.RQS_HEADER);
                            out.write(BlueCtrl.macToBytes(rmtDvc.getAddress()));
                        }

                        break;
                    }

                    case BlueCtrl.UPD_HEADER: {

                    /*
                    An Update message contain key and/or volatile information about a user;
                    target MAC, bounces, user status and last update fields are included. These fields
                    have fixed length, therefore no divider is needed. To be precise, MAC addresses
                    are 6 bytes long, both bounces and user status can be represented by a single byte,
                    and last update field is a long int value (8 bytes), for a total of 16 bytes per
                    user. These are all vital or non persistent information, and they are engineered to
                    take up as little space as they can.
                    [1][MAC][last update][bounces][status]
                       |            16 bytes             |
                    */

                        byte[] buffer = new byte[6], lastUpd = new byte[8];
                        byte b, status;
                        int bounces;

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
                                System.out.println("Premature EOF, message misunderstanding");
                                //TODO: throw something
                            }
                            i += j;
                        } while (i < 8);

                        String address = BlueCtrl.bytesToMAC(buffer);
                        if (BlueCtrl.validateUser(address, BlueCtrl.rebuildTimestamp(lastUpd))) {

                            BlueCtrl.awakeUser(buffer, address, status, bounces + 1);
                            /*
                            add a new ChatUser object to the adapter, building it from memorized information;
                            bounce field is increased by 1 because the remote device represents an additional
                            node to bounce on
                            */
                            out.write(BlueCtrl.ACK); //ACKed

                        }
                        else {
                            out.write(BlueCtrl.RQS_HEADER); //Info Request
                            out.write(buffer);
                        }

                        break;
                    }

                    case BlueCtrl.RQS_HEADER: {

                        System.out.println("This message should not be captured here");
                        //TODO: Misunderstanding management
                        break;
                    }

                    case BlueCtrl.CRD_HEADER: {
                    /*
                    A Card message is a heavy message containing persistent size-variable fields of a
                    ChatUser; it allows for profile customization and is only sent to create a new
                    Users table entry or update an existing one when DB contains out-of-date information.
                    Every field after header and MAC address are preceded by a length byte, indicating
                    the field length in bytes and allowing for streamer consistent reading.
                    [3][   MAC   ][last update][ age ][gender][nationality][length][  username  ][length][profile pic]
                       | 6 bytes |   8 bytes  |1 byte|1 byte |   1 byte   |1 byte |length bytes |1 byte |length bytes|
                     */
                        byte[] buffer = new byte[6],
                               lastUpd = new byte[8],
                               username, pic;
                        int length, age, gender, country;

                        i = 0;
                        do {
                            j = in.read(buffer, i, 6 - i);
                            if (j < 0) {
                                System.out.println("Premature EOF, message misunderstanding");
                                //TODO: throw something
                            }
                            i += j;
                        } while (i < 6);

                        i = 0;
                        do {
                            j = in.read(lastUpd, i, 8 - i);
                            if (j < 0) {
                                System.out.println("Premature EOF, message misunderstanding");
                                //TODO: throw something
                            }
                            i += j;
                        } while (i < 8);

                        age = in.read();
                        gender = in.read();
                        country = in.read();

                        length = in.read();
                        username = new byte[length];

                        i = 0;
                        do {
                            j = in.read(username, i, length - i);
                            if (j < 0) {
                                System.out.println("Premature EOF, message misunderstanding");
                                //TODO: throw something
                            }
                            i += j;
                        } while (i < length);

                        length = in.read();
                        pic = new byte[length];

                        i = 0;
                        do {
                            j = in.read(pic, i, length - i);
                            if (j < 0) {
                                System.out.println("Premature EOF, message misunderstanding");
                                //TODO: throw something
                            }
                            i += j;
                        } while (i < length);

                        BlueCtrl.updateUserTable(BlueCtrl.bytesToMAC(buffer), BlueCtrl.rebuildTimestamp(lastUpd),
                                                 new String(username), age, gender, country);

                        //TODO: ChatUser update

                        out.write(BlueCtrl.ACK); //ACKed

                        break;

                    }

                    case BlueCtrl.MSG_HEADER: {
                    /*
                    A Chat message is the kind of message which is ultimately shown to the user upon reception.
                    It wraps up a message from the sender in a packet consisting of a header, a target MAC address,
                    a sender MAC address and a message field. Target MAC address is the  of the actual recipient of
                    the message MAC address, whilst the sender MAC address is the original sender device MAC address.
                    Although Message field has variable length, no divider is needed because Target and Sender
                    fields have fixed length instead. A length byte precedes the Message field, indicating
                    the message length in bytes; a message cannot exceed 255 characters in length, therefore
                    1 byte is enough to represent the length field.
                    [4][Target MAC][Sender MAC][Msg length][  Message field  ]
                       | 6 bytes  |  6 bytes  |   1 byte  | Msg length bytes |
                     */
                        byte[] buffer = new byte[6], sender = new byte[6];
                        int length;
                        i = 0;

                        do {
                            j = in.read(buffer, i, 6 - i);
                            if (j < 0) {
                                System.out.println("Premature EOF, message misunderstanding");
                                //TODO: throw something
                            }
                            i += j;
                        } while (i < 6);

                        i = 0;
                        do {
                            j = in.read(sender, i, 6 - i);
                            if (j < 0) {
                                System.out.println("Premature EOF, message misunderstanding");
                                //TODO: throw something
                            }
                            i += j;
                        } while (i < 6);

                        if ((length = in.read()) < 0) {
                            System.out.println("Read Error");
                            //TODO: optional read exception
                        }

                        byte[] msgBuffer = new byte[length];
                        i = 0;
                        do {
                            j = in.read(msgBuffer, i, length - i);
                            if (j < 0) {
                                System.out.println("Premature EOF, message misunderstanding");
                                //TODO: throw something
                            }
                            i += j;
                        } while (i < length);

                        if (BlueCtrl.bytesToMAC(buffer).equals(BluetoothAdapter.getDefaultAdapter().getAddress())) {

                            BlueCtrl.buildMsg(BlueCtrl.bytesToMAC(sender), new String(msgBuffer));
                        } else {

                            BlueCtrl.sendMsg(buffer, sender, msgBuffer);
                        /*
                        if this device is not the target device, message has to be forwarded to the next node
                        on the route leading to the target; it is wrapped again in a packet and sent as a
                         */
                        }

                        out.write(BlueCtrl.ACK); //ACKed

                        break;
                    }

                    case BlueCtrl.DRP_HEADER: {
                    /*
                    A Drop Request is sent when a device closes its connection and is no longer reachable.
                    It contains a list of MACs of devices no longer reachable by this device due to
                    the fact that their routes passed through the exiting node. No divider is needed because
                    every MAC address is made up of 6 bytes.
                    [5][MAC][MAC]...
                    */
                        byte[] buffer = new byte[6];
                        boolean breakFree = true;

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

                            ChatUser user = BlueCtrl.manageDropRequest(rmtDvc.getAddress(), BlueCtrl.bytesToMAC(buffer));
                            if (user != null) {
                                out.write(BlueCtrl.buildUpdMsg(user));
                            }
                        }

                        out.write(BlueCtrl.ACK); //ACKed

                        break;
                    }
                    case -1:
                        connected = false;
                }
            } while(connected);
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
