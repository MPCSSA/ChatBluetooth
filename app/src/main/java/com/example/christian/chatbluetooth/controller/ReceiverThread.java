package com.example.christian.chatbluetooth.controller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.example.christian.chatbluetooth.model.ChatUser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;

public class ReceiverThread extends Thread {

    private BluetoothSocket sckt; //socket for Bluetooth communication
    private InputStream in;       //InputStream object from which reading incoming messages
    private OutputStream out;     //OutputStream object from which reading ACK messages
    private BluetoothDevice rmtDvc;        //MAC address of communicating Bluetooth device
    private Handler handler;

    public void setSckt(BluetoothSocket sckt) {
        this.sckt = sckt;
    }

    public void setHandler(Handler handler) { this.handler = handler; }

    public ReceiverThread(BluetoothSocket sckt, Handler handler) {
        setSckt(sckt);
        setHandler(handler);
        InputStream tmp0 = null;
        OutputStream tmp1 = null;

        try {
            tmp0 = this.sckt.getInputStream();
            tmp1 = this.sckt.getOutputStream();
        }
        catch (IOException ignore) {}

        in = tmp0;  //InputStream for message listening
        out = tmp1; //OutputStream for Instant Reply or ACK forwarding

        rmtDvc = sckt.getRemoteDevice(); //communicating device
    }

    public void run() {

        System.out.println("sto ricevendo");
        try {

            int i, j; //counters
            boolean connected = true; //connection still on, i.e. rmtDvc did not shut its OutputStream yet
            ArrayList<byte[]> filteredUpdCascade = null; //ArrayList containing Update Message segments of an Update Cascade

            BlueCtrl.lockDiscoverySuspension();

            do {

                while (BluetoothAdapter.getDefaultAdapter().isDiscovering()) System.out.println("Discovery: " + BluetoothAdapter.getDefaultAdapter().isDiscovering());

                System.out.println("leggo");
                byte flag = (byte) in.read();
                System.out.println("ho letto " + flag);
            /*
            incoming message flag;
            0: Greetings Message; a newly connected device is sending information about itself.
            1: Update Message; a device is transmitting information about remote devices that are accessible through itself.
            2: Info Request; a device has got inconsistent or no information about a target device and it requests an heavy update.
                             It is an Instant Reply only message, and it is not supposed to be received here.
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

                        /*BlueCtrl.newcomers.put(rmtDvc.getAddress(), rmtDvc);

                        BlueCtrl.greet(rmtDvc, handler);*/

                        byte status = (byte) in.read(); //read Status
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
                        //read last profile update timestamp

                        long lastUpd = BlueCtrl.rebuildTimestamp(bytes);

                        if (BlueCtrl.awakeUser(rmtDvc.getAddress(), rmtDvc, status, 0, lastUpd)) {
                            handler.sendEmptyMessage(BlueCtrl.GRT_HEADER);
                            System.out.println(rmtDvc.getAddress() + " summoned");
                        }
                        /*
                        New ChatUser object is created regardless of incoherent or non-existent persistent information;
                        if needed, an update will be requested and the object will be updated
                        */

                        if (BlueCtrl.validateUser(rmtDvc.getAddress(), lastUpd)) {
                            out.write(BlueCtrl.ACK); //ACKed
                            System.out.println("ACKED");
                            connected = false;
                        } else {
                            /*
                            User information are not up to date, an Info Request is forwarded as Instant Reply
                            */
                            System.out.println("requesting information");
                            out.write(BlueCtrl.RQS_HEADER);
                            out.write(BlueCtrl.macToBytes(rmtDvc.getAddress()));
                        }

                        break;
                    }

                    case BlueCtrl.UPD_HEADER: {

                    /*
                    An Update message contain key and/or volatile information about users, stuffed into
                    16-bytes-long segments; target MAC, bounces, user status and last update fields are included.
                    These fields have fixed length, therefore no divider is needed. To be precise, MAC addresses
                    are 6 bytes long, both bounces and user status can be represented by a single byte,
                    and last update field is a long int value (8 bytes), for a total of 16 bytes per
                    user (a segment). A Number field is included at the beginning of the message to keep track of segments
                    number. These are all vital or non persistent information, and they are engineered to
                    take up as little space as they can.
                    [1][number][MAC][last update][bounces][status]{user}{user}...
                       |1 byte|            16 bytes              | 16  | 16  |
                    */

                        byte[] segment = new byte[16], buffer = new byte[6], lastUpd = new byte[8];
                        byte status;
                        int bounces, number = in.read();
                        boolean bool;
                        filteredUpdCascade = new ArrayList<>();

                        for (int _ = 0; _ < number; ++_) {
                            //repeat until all segments have been read

                            i = 0;
                            do {
                                j = in.read(segment, i, 16 - i);
                                if (j < 0) {
                                    System.out.println("Premature EOF, message misunderstanding");
                                    //TODO: throw something
                                }
                                i += j;
                            } while (i < 16);
                            //read a whole segment

                            i = 0;
                            while (i < 6) {
                                buffer[i] = segment[i];
                                ++i;
                            }

                            i = 0;
                            while (i < 8) {
                                lastUpd[i] = segment[i + 6];
                                ++i;
                            }
                            bounces = (segment[14] < 0) ? (byte) (segment[14] + 256) : segment[14];
                            status = segment[15];
                            //recreate fields information from segment

                            String address = BlueCtrl.bytesToMAC(buffer);
                            long timestamp = BlueCtrl.rebuildTimestamp(lastUpd);

                            if (bool = BlueCtrl.awakeUser(address, rmtDvc, status, bounces + 1, timestamp))
                                handler.sendEmptyMessage(BlueCtrl.UPD_HEADER);
                            //show new ChatUser regardless of coherent information; that will be updated if needed

                            if (BlueCtrl.validateUser(address, timestamp)) {

                                if (bool) {

                                    ++segment[14];
                                    filteredUpdCascade.add(segment);
                                }
                            /*
                            try adding or updating a ChatUser object into the adapter; if addition was
                            successful, this segment is inserted into the array of filtered update segments;
                            bounces field has to be increased by one though, because this device is one more
                            node on the route.
                            */

                            } else {

                                ++segment[14];
                                filteredUpdCascade.add(segment);
                                /*
                                up to date information is required; an Info Request will be fulfilled,
                                and this segment will be forwarded after that
                                 */

                                out.write(BlueCtrl.RQS_HEADER); //Info Request
                                out.write(buffer);
                            }
                        }

                        out.write(BlueCtrl.ACK); //all segments were received; ACKed
                        connected = false;
                        break;
                    }

                    case BlueCtrl.RQS_HEADER: {
                        //You should not be able to send these as actual messages

                        System.out.println("This message should not be captured here");
                        //TODO: Misunderstanding management
                        break;
                    }

                    case BlueCtrl.CRD_HEADER: {
                    /*
                    A Card message is a heavy message containing persistent size-variable fields of a
                    ChatUser; it allows for profile customization and is only sent to create a new
                    Users table entry or update an existing one when DB contains out-of-date information.
                    Username and Profile Pic fields are preceded by a length byte, indicating
                    the field length in bytes and allowing for streamer consistent reading.
                    [3][   MAC   ][last update][ age ][gender][nationality][length][  username  ][pic size (q + r)][profile pic]
                       | 6 bytes |   8 bytes  |1 byte|1 byte |   1 byte   |1 byte |length bytes |2 bytes + 1 byte ||size bytes |
                     */
                        final byte[] buffer = new byte[6],
                               lastUpd = new byte[8],
                               username, pic;
                        final int age, gender, country;
                        int length;

                        //BlueCtrl.lockDiscoverySuspension(); //heavy message, needs fast download
                        i = 0;
                        do {
                            j = in.read(buffer, i, 6 - i);
                            if (j < 0) {
                                System.out.println("Premature EOF, message misunderstanding");
                                //TODO: throw something
                            }
                            i += j;
                        } while (i < 6);
                        //read MAC address

                        i = 0;
                        do {
                            j = in.read(lastUpd, i, 8 - i);
                            if (j < 0) {
                                System.out.println("Premature EOF, message misunderstanding");
                                //TODO: throw something
                            }
                            i += j;
                        } while (i < 8);
                        //read LastUpd field

                        age = in.read();
                        gender = in.read();
                        country = in.read();
                        //read optional user information; can be null

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
                        //read username

                        /*i = 0;
                        length = 0;
                        do {
                            length = (length * 256) + in.read();
                            ++i;
                        } while (i < 3);


                        if (length > 0) {
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
                            //download profile image as a byte sequence
                        }*/

                        (new Thread(new Runnable() {
                            @Override
                            public void run() {
                                String address = BlueCtrl.bytesToMAC(buffer);
                                BlueCtrl.insertUserTable(address, BlueCtrl.rebuildTimestamp(lastUpd),
                                        new String(username), age, gender, country);
                                //consistent information inserted into DB

                                BlueCtrl.updateQueue.add(address); //ChatUser object updated

                                Message crd = new Message();
                                crd.what = BlueCtrl.CRD_HEADER;
                                Bundle bundle = new Bundle();
                                bundle.putString("MAC", address);
                                crd.setData(bundle);
                                handler.sendMessage(crd);
                                //update and show user information

                                //TODO: image updating

                            }
                        })).start();

                        out.write(BlueCtrl.ACK); //ACKed
                        connected = false;

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
                    [4][Target MAC][Sender MAC][Emoticon][Msg length][  Message field  ]
                       | 6 bytes  |  6 bytes  | 1 byte  |  1 byte   | Msg length bytes |
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

                        switch(in.read()) {

                            case 0:

                                if ((length = in.read()) < 0) {
                                    System.out.println("Read Error");
                                    //TODO: optional read exception
                                }

                                ++length; //you cannot send empty messages, so message length is between 1 and 256
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

                                    System.out.println("SHOWING OFF");
                                    BlueCtrl.showMsg(BlueCtrl.bytesToMAC(sender), new String(msgBuffer), new Date(), true);
                                    handler.sendEmptyMessage(BlueCtrl.MSG_HEADER);
                                }
                                else {

                                    BlueCtrl.sendMsg(BlueCtrl.scanUsers(BlueCtrl.bytesToMAC(buffer)).getNextNode(),
                                            BlueCtrl.buildMsg(buffer, sender, msgBuffer));
                                    /*
                                    if this device is not the target device, message has to be forwarded to the next node
                                    on the route leading to the target; it is wrapped again in a packet and sent as a
                                    */
                                }

                                break;

                            case 1:

                                int code = in.read();

                                if (BlueCtrl.bytesToMAC(buffer).equals(BluetoothAdapter.getDefaultAdapter().getAddress())) {

                                    System.out.println("SHOWING OFF");
                                    BlueCtrl.showEmo(BlueCtrl.bytesToMAC(sender), code, new Date(), true);
                                    handler.sendEmptyMessage(BlueCtrl.MSG_HEADER);
                                }
                                else {

                                    BlueCtrl.sendMsg(BlueCtrl.scanUsers(BlueCtrl.bytesToMAC(buffer)).getNextNode(),
                                            BlueCtrl.buildEmoticon(buffer, sender, (byte) code));
                                }
                        }


                        out.write(BlueCtrl.ACK); //ACKed
                        connected = false;

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

            System.out.println("Unlocking discovery");
            BlueCtrl.unlockDiscoverySuspension();
            handler.sendEmptyMessage(BlueCtrl.ACK);

            /*if (filteredUpdCascade != null && filteredUpdCascade.size() > 0) {
                BlueCtrl.dispatchNews(BlueCtrl.buildUpdMsg(filteredUpdCascade), rmtDvc);
            }*/
            in.close();
            out.close();
        }
        catch (IOException e) {

            e.printStackTrace();
            BlueCtrl.unlockDiscoverySuspension();
            cancel();
        }
        catch (Exception e) {

            e.printStackTrace();
            BlueCtrl.unlockDiscoverySuspension();
            cancel();
        }

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
