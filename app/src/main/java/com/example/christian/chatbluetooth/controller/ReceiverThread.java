package com.example.christian.chatbluetooth.controller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

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

        try {

            int i, j; //counters
            boolean connected = true; //connection still on, i.e. rmtDvc did not shut its OutputStream yet
            ArrayList<byte[]> filteredUpdCascade = null; //ArrayList containing Update Message segments of an Update Cascade


            BlueCtrl.lockDiscoverySuspension();

            do {

                byte flag = (byte) in.read();
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

                if (flag != BlueCtrl.GRT_HEADER && BlueCtrl.tokenMap.get(rmtDvc.getAddress()) == null) {
                    /*
                    A busy device could have been deleted after a series of unanswered connections;
                    before this device can accept messages the remote device must send a Greetings
                    message and restore this device route.
                    */

                    out.write(BlueCtrl.GRT_HEADER);

                    int skip, k, l;
                    switch (flag) {
                        case BlueCtrl.UPD_HEADER:
                            skip = in.read() * 16; //UPD MSG length without header and number fields
                            break;
                        case BlueCtrl.CRD_HEADER:
                            k = 0;
                            do {
                                l = (int) in.skip(25 - k);
                                k += l;
                            } while(k < 25);
                            //skip first 25 bytes of the message; after that, skipping username requires reading length field
                            skip = in.read(); //skip username
                            break;
                        case BlueCtrl.MSG_HEADER:
                            k = 0;
                            do {
                                l = (int) in.skip(12 - k);
                                k += l;
                            } while(k < 12);
                            //skip MAC addresses
                            if (in.read() == 0) skip = in.read(); //skip a text message
                            else skip = 1; //skip an emoticon code
                            break;
                        case BlueCtrl.DRP_HEADER:
                            skip = in.read() * 6; //skip MACs
                            break;
                        default: skip = 0; //skip nothing
                    }

                    in.skip(skip);

                    /*
                    next accepted message would be a GRT one; main thread communication automatically
                    initiate a Greet Back routine
                     */

                    continue;
                }

                switch (flag) {
                    case BlueCtrl.GRT_HEADER: {
                    /*
                    A Greetings message contains user status and last update field; MAC address is implicit,
                    and status and last update field have fixed length (1 byte and 8 byte long),
                    therefore no divider is needed.
                    [0][status][last update]
                       |1 byte|   8 byte  |
                    */

                        byte status = (byte) in.read(); //read Status
                        byte[] bytes = new byte[8];
                        i = 0;

                        do {
                            j = in.read(bytes, i, 8 - i);
                            if (j < 0) throw new IOException(); //Misunderstanding
                            i += j;
                        } while (i < 8);
                        //read last profile update timestamp

                        long lastUpd = BlueCtrl.rebuildTimestamp(bytes);

                        Message mail = new Message();
                        Bundle bundle = new Bundle();
                        bundle.putString("MAC", rmtDvc.getAddress());
                        mail.setData(bundle);

                        if (status == (byte)0)
                            mail.what = BlueCtrl.INVISIBLE;
                            //Invisible user, but nonetheless a reachable user

                        else if (BlueCtrl.awakeUser(rmtDvc.getAddress(), rmtDvc, status, 0, lastUpd))
                            mail.what = BlueCtrl.GRT_HEADER;

                        else mail.what = BlueCtrl.ACK;

                        handler.sendMessage(mail);
                        /*
                        New ChatUser object is created regardless of incoherent or non-existent persistent information;
                        if needed, an update will be requested and the object will be updated
                        */

                        if (BlueCtrl.validateUser(rmtDvc.getAddress(), lastUpd)) {

                            out.write(BlueCtrl.ACK); //ACKed
                            connected = false;
                        }
                        else {
                            /*
                            User information are not up to date, an Info Request is forwarded as Instant Reply
                            */
                            out.write(BlueCtrl.RQS_HEADER);
                            out.write(BlueCtrl.macToBytes(rmtDvc.getAddress()));
                        }

                        /*
                        It there is an entry in the Tokens Map, this device already received a Greeting from the
                        remote device and it does not need to pic a new object from the buffer
                         */

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
                                if (j < 0) throw new IOException(); //Misunderstanding
                                i += j;
                            } while (i < 16);
                            //read a whole segment

                            System.arraycopy(segment, 0, buffer, 0, 6);
                            String address = BlueCtrl.bytesToMAC(buffer);

                            if (address.equals(BluetoothAdapter.getDefaultAdapter().getAddress())) continue;
                            //Information about this device could arrive

                            System.arraycopy(segment, 6, lastUpd, 0, 8);
                            long timestamp = BlueCtrl.rebuildTimestamp(lastUpd);

                            bounces = (segment[14] < 0) ? (byte) (segment[14] + 256) : segment[14];
                            status = segment[15];
                            //recreate fields information from segment

                            Message mail = new Message();
                            Bundle bundle = new Bundle();
                            bundle.putString("MAC", rmtDvc.getAddress());
                            mail.setData(bundle);

                            if (bool = BlueCtrl.awakeUser(address, rmtDvc, status, bounces + 1, timestamp)) {

                                mail.what = BlueCtrl.UPD_HEADER;
                            }
                            //show new ChatUser regardless of coherent information; that will be updated if needed
                            else {

                                mail.what = BlueCtrl.ACK;
                                //No new ChatUser to pop from the buffer, but message received nonetheless
                            }

                            handler.sendMessage(mail);

                            if (status == (byte)0) {

                                mail = new Message();
                                bundle = new Bundle();
                                bundle.putString("MAC", address);
                                mail.what = BlueCtrl.INVISIBLE;
                                mail.setData(bundle);
                                handler.sendMessage(mail);
                                //Invisible user, but nonetheless a reachable user
                            }

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

                        throw new IOException(); //Misunderstanding
                    }

                    case BlueCtrl.CRD_HEADER: {
                    /*
                    A Card message is a message containing persistent size-variable fields of a
                    ChatUser; it allows for profile customization and is only sent to create a new
                    Users table entry or update an existing one when DB contains out-of-date information.
                    Username is preceded by a length byte, indicating the field length in bytes and
                    allowing for streamer consistent reading. Last 8 bytes represents Profile Picture
                    last update: if this field has not changed, this device needs not to download a new
                    picture, therefore enhancing performance and avoiding heavy unnecessary message exchange.
                    Card Message can be up to 47 bytes long.
                    [3][   MAC   ][last update][ age ][gender][nationality][length][  username  ]
                       | 6 bytes |   8 bytes  |8 byte|1 byte |   1 byte   |1 byte |length bytes |
                     */
                        final byte[] buffer = new byte[6],
                               lastUpd = new byte[8],
                               age = new byte[8],
                               username;
                        final int gender, country;
                        int length;

                        i = 0;
                        do {
                            j = in.read(buffer, i, 6 - i);
                            if (j < 0) throw new IOException(); //Misunderstanding
                            i += j;
                        } while (i < 6);
                        //read MAC address

                        i = 0;
                        do {
                            j = in.read(lastUpd, i, 8 - i);
                            if (j < 0) throw new IOException(); //Misunderstanding
                            i += j;
                        } while (i < 8);
                        //read LastUpd field

                        i = 0;
                        do {
                            j = in.read(age, i, 8 - i);
                            if (j < 0) throw new IOException(); //Misunderstanding
                            i += j;
                        } while (i < 8);
                        //read age field

                        gender = in.read();
                        country = in.read();
                        //read optional user information; can be 0 (null)

                        length = in.read();
                        username = new byte[length];

                        i = 0;
                        do {
                            j = in.read(username, i, length - i);
                            if (j < 0) throw new IOException(); //Misunderstanding
                            i += j;
                        } while (i < length);
                        //read username

                        (new Thread(new Runnable() {
                            @Override
                            public void run() {

                                String address = BlueCtrl.bytesToMAC(buffer);
                                BlueCtrl.insertUserTable(address, BlueCtrl.rebuildTimestamp(lastUpd),
                                        new String(username), BlueCtrl.rebuildTimestamp(age), gender, country);
                                //consistent information inserted into DB

                                Message mail = new Message();
                                mail.what = BlueCtrl.CRD_HEADER;
                                Bundle bundle = new Bundle();
                                bundle.putString("MAC", rmtDvc.getAddress());
                                mail.setData(bundle);
                                handler.sendMessage(mail);
                                //update and show user information

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
                    Then, it is a text message, a Crypted field follows, indicating wheter or not the message has to be
                    decrypted with default AES key.
                    Although Message field has variable length, no divider is needed because Target and Sender
                    fields have fixed length instead. A type byte and a length byte precede the Message field, indicating
                    the type of the message (text or emoticon) and its length in bytes.
                    1 byte is enough to represent both the length field and the type field.
                    Text Message:
                    [5][Target MAC][Sender MAC][0][Crypted][Msg length][  Message field  ]
                       | 6 bytes  |  6 bytes  |  | 1 byte |  1 byte  | Msg length bytes |
                    Emoticon Message:
                    [5][Target MAC][Sender MAC][1][Emoticon]
                       | 6 bytes  |  6 bytes  |  | 1 byte |
                     */

                        byte[] buffer = new byte[6], sender = new byte[6],
                               msgBuffer;

                        i = 0;

                        do {
                            j = in.read(buffer, i, 6 - i);
                            if (j < 0) throw new IOException();
                            i += j;
                        } while (i < 6);

                        i = 0;
                        do {
                            j = in.read(sender, i, 6 - i);
                            if (j < 0) throw new IOException();
                            i += j;
                        } while (i < 6);



                        switch(in.read()) {

                            case 0:
                                //Text Message

                                int crypted, length;
                                if ((crypted = in.read()) == -1) throw new  IOException(); //is the message crypted?

                                if ((length = in.read()) <= 0) throw new IOException();
                                //you cannot send empty messages, so message length is between 1 and 255

                                byte[] tmpBuffer;
                                tmpBuffer = new byte[length];
                                i = 0;
                                do {
                                    j = in.read(tmpBuffer, i, length - i);
                                    if (j < 0) throw new IOException();
                                    i += j;
                                } while (i < length);

                                if (BlueCtrl.bytesToMAC(buffer).equals(BluetoothAdapter.getDefaultAdapter().getAddress())) {
                                    //Show if this is target

                                    if (crypted == 1) msgBuffer = BlueCtrl.decrypt(tmpBuffer); //decrypt if crypted
                                    else msgBuffer = tmpBuffer;
                                    if (msgBuffer != null) BlueCtrl.showReceivedMsg(BlueCtrl.bytesToMAC(sender), new String(msgBuffer), new Date());

                                    Message mail = new Message();
                                    mail.what = BlueCtrl.MSG_HEADER;
                                    Bundle bundle = new Bundle();
                                    bundle.putString("MAC", rmtDvc.getAddress());
                                    mail.setData(bundle);
                                    handler.sendMessage(mail);
                                }
                                else {

                                    byte[] glued = new byte[16 + length];
                                    glued[0] = BlueCtrl.MSG_HEADER;
                                    System.arraycopy(buffer, 0, glued, 1, 6);
                                    System.arraycopy(sender, 0, glued, 7, 6);
                                    glued[13] = 0;
                                    glued[14] = (byte) crypted;
                                    glued[15] = (byte) length;
                                    System.arraycopy(tmpBuffer, 0, glued, 16, length);
                                    //Glue together the message to resend it without errors

                                    BlueCtrl.sendMsg(BlueCtrl.scanUsersForDvc(BlueCtrl.bytesToMAC(buffer)),
                                                     glued, handler);
                                    /*
                                    if this device is not the target device, message has to be routed to the next node
                                    on the route leading to the target; it is wrapped again in a packet and sent as a
                                    Text Message
                                    */
                                }

                                break;

                            case 1:
                                //Emoticon Message

                                int code = in.read(); //Emoticon position

                                if (BlueCtrl.bytesToMAC(buffer).equals(BluetoothAdapter.getDefaultAdapter().getAddress())) {
                                    //Show if this is target

                                    BlueCtrl.showReceivedEmo(BlueCtrl.bytesToMAC(sender), code, new Date()); //Emoticons are never crypted

                                    Message mail = new Message();
                                    mail.what = BlueCtrl.MSG_HEADER;
                                    Bundle bundle = new Bundle();
                                    bundle.putString("MAC", rmtDvc.getAddress());
                                    mail.setData(bundle);
                                    handler.sendMessage(mail);
                                }
                                else {

                                    BlueCtrl.sendMsg(BlueCtrl.scanUsersForDvc(BlueCtrl.bytesToMAC(buffer)),
                                            BlueCtrl.buildEmoticon(buffer, sender, (byte) code), handler);
                                    //route Emoticon
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
                    every MAC address is made up of 6 bytes. A number field preceeds MACs, indicating
                    the message length.
                    [6][number][MAC][MAC]...
                    */

                        byte[] buffer = new byte[6], tmp, lst = new byte[0];
                        int number = in.read();

                        for (int _ = 0; _ < number; ++_) {

                            i = 0;
                            do {
                                j = in.read(buffer, i, 6 - i);
                                if (j < 0) throw new IOException(); //misunderstanding
                                i += j;
                            } while (i < 6);

                            byte[] instantreply = BlueCtrl.manageDropRequest(rmtDvc.getAddress(), rmtDvc);
                            //instantreply != null only if alternative route exists
                            if (instantreply != null) {

                                out.write(instantreply); //Instant Reply Update Msg
                            }
                            else {

                                Message mail = new Message();
                                mail.what = BlueCtrl.DRP_HEADER;
                                Bundle bundle = new Bundle();
                                bundle.putString("MAC", rmtDvc.getAddress());
                                bundle.putString("LST", BlueCtrl.bytesToMAC(buffer));
                                mail.setData(bundle);
                                handler.sendMessage(mail);
                                //Sending lost device MAC to ChatActivity for user list update

                                tmp = lst;
                                int index = tmp.length;
                                lst = new byte[index + 6];
                                System.arraycopy(tmp, 0, lst, 0, index);
                                System.arraycopy(buffer, 0, lst, index, 6);
                                //Keeping track of lost devices
                            }
                        }

                        Message mail = new Message();
                        mail.what = BlueCtrl.LST;
                        Bundle bundle = new Bundle();
                        bundle.putString("MAC", rmtDvc.getAddress());
                        bundle.putByteArray("LST", lst);
                        mail.setData(bundle);
                        handler.sendMessage(mail);
                        //all dropped devices

                        out.write(BlueCtrl.ACK); //ACKed

                        break;
                    }

                    case BlueCtrl.ACK:
                        /*
                        Drop-Awareness mechanism: when a device receives an ACK, it sends an ACK as well
                        to signal its presence. A Bluetooth Socket can be busy sending or receiving another
                        message; therefore, a Token Counter is assigned to every reachable close device and is
                        decremented every time a connectino fails; if instead an ACK is sent back,
                        Tocken Counter is restored to its maximum.
                         */

                        if (BlueCtrl.closeDvc.containsValue(rmtDvc)) {
                            out.write(BlueCtrl.ACK);
                            BlueCtrl.tokenMap.put(rmtDvc.getAddress(), BlueCtrl.TKN);
                        }
                        else {
                            throw new IOException(); //force this device drop
                        }
                        connected = false;

                        break;

                    default:
                        //Misunderstanding
                        connected = false;
                }
            } while(connected);

            BlueCtrl.unlockDiscoverySuspension();

            Message mail = new Message();
            mail.what = BlueCtrl.ACK;
            Bundle bundle = new Bundle();
            bundle.putString("MAC", rmtDvc.getAddress());
            mail.setData(bundle);
            handler.sendMessage(mail);

            if (filteredUpdCascade != null && filteredUpdCascade.size() > 0) {
                BlueCtrl.dispatchNews(BlueCtrl.buildUpdMsg(filteredUpdCascade), rmtDvc, handler);
            }
            //UPDATE CASCADE
            in.close();
            out.close();
        }

        catch (Exception e) {

            e.printStackTrace();
            BlueCtrl.unlockDiscoverySuspension();
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
