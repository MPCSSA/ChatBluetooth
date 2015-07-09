package com.example.christian.chatbluetooth.controller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.util.Base64;

import com.example.christian.chatbluetooth.model.BlueDBManager;
import com.example.christian.chatbluetooth.model.ChatMessage;
import com.example.christian.chatbluetooth.model.ChatUser;
import com.example.christian.chatbluetooth.view.Adapters.EmoticonAdapter;
import com.example.christian.chatbluetooth.view.Adapters.MessageAdapter;
import com.example.christian.chatbluetooth.view.Adapters.NoMaterialRecyclerAdapter;
import com.example.christian.chatbluetooth.view.Adapters.RecycleAdapter;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class BlueCtrl {

    public static final byte GRT_HEADER = (byte) 0; //header for Greetings Message
    public static final byte UPD_HEADER = (byte) 1; //header for Update Message
    public static final byte RQS_HEADER = (byte) 2; //header for Info Request
    public static final byte CRD_HEADER = (byte) 3; //header for Card Message
    public static final byte PIC_HEADER = (byte) 4; //header for Card Message
    public static final byte MSG_HEADER = (byte) 5; //header for Chat Message
    public static final byte DRP_HEADER = (byte) 6; //header for Drop Request
    public static final byte        ACK = (byte) 7; //ACKnowledge Message for communication synchronization
    public static final byte  INVISIBLE = (byte) 8;
    public static final byte        NAK = (byte) -1;
    public static final byte        LST = (byte) -2;
    public static final int         TKN = 20;       //Tokens assigned to an alive device
    public static final String     UUID = "7235630e-9499-45b8-a8f6-d76c41d684dd"; //custom UUID, randomly generated

    public static byte STS = 1; //User status: visible 1 invisible 0
    public static byte SPY = 0; //Spy mode: enabled 1 disable 0

    private static byte[] key = {'K', 'E', 'Y','K', 'E', 'Y', 'E', 'Y', 'K', 'E', 'Y','K', 'E', 'Y', 'E', 'Y'};
    private static String keyString = "KEYKEYEYKEYKEYEY";

    /*
    DEBUG ONLY
     */
    public static boolean version;
    /*
    DEBUG ONLY
    */

    //BUFFERS
    public static ArrayList<ChatUser> userQueue = new ArrayList<>(); //ChatUser Buffer to store users to show in ListFragment
    public static ArrayList<ChatMessage> msgBuffer = new ArrayList<>(); //ChatMessage Buffer to store messages to show in ChatFragment
    public static HashMap<String, BluetoothDevice> closeDvc = new HashMap<>(); //Close Devices Buffer; it makes possible Greetings, Update and Drop mechanisms
    public static HashMap<String, Integer> tokenMap = new HashMap<>(); //Map for Token Counters

    /*
    LAST ONE OUT CLOSES THE DOOR: synchronization mechanism to restart Bluetooth Discovery when all
    sockets are done communication with the outside; discovery hinders message exchange and has to be put
    down for the sake of the communication
     */
    public static boolean DISCOVERY_SUSPENDED = false; //Discovery locks
    public static int DISCOVERY_LOCK = 0;

    /*
    DEBUG ONLY
     */
    public static NoMaterialRecyclerAdapter userNomat;
    /*
    DEBUG ONLY
     */

    /*
    STATIC ADAPTERS
    To enhance and simplify thread communication, these data structures are shared between interfaces and
    controllers; update notification is managed via Handler, because only main threads are allowed to
    manipulate views
    */
    public final static ArrayList<ChatUser> userList = new ArrayList<>();
    public final static RecycleAdapter userAdapt = new RecycleAdapter(userList);
    //ChatUser RecyclerAdapter; it is only used by ListFragent, therefore it can be initialized as final
    public static EmoticonAdapter emoticons; //ArrayAdapter used to implement the Emoticons Tab in ChatFragment
    public static MessageAdapter msgAdapt; //ArrayAdapter used to show and manage message history in ChatFragment

    //DATABASE
    private static BlueDBManager dbManager; //DB Manager
    private static final String dbname = "bluedb"; //DB name

    public static int counter = 0;


    /*
    ROUTING AND CONNECTION METHODS
     */

    public static void sendMsg(BluetoothDevice dvc, byte[] msg, Handler handler) {
        /*
        Call this method when you're ready to send a message to a device; MessageThread keeps the message
        type but is not concerned with its content. The Handler is required to ensure Thread communication
         */

        System.out.println("sending " + dvc.getAddress());
        (new MessageThread(dvc, msg, handler)).start();
        //Interface method, you are not allowed to instantiate a MessageThread object
    }

    public static void dispatchNews(byte[] msg, BluetoothDevice filter, Handler handler) {
        //Use this method to dispatch Route information to all your neighbours; it spreads UP and DRP messages

        for(BluetoothDevice dvc : BlueCtrl.closeDvc.values()) {

            if (!dvc.equals(filter)) sendMsg(dvc, msg, handler); //A filter can be specified to avoid redundancy
        }
    }





    public static void fillMsgAdapter(){

        Cursor cursor = fetchMsgHistory(msgAdapt.getAddress(), (new Date()).getTime());
        if (cursor.getCount() > 0){

            msgAdapt.add(null);

            cursor.moveToLast();
            do {
                msgAdapt.add(new ChatMessage(cursor.getString(0), cursor.getInt(2) == 1, cursor.getLong(1), cursor.getInt(3) == 1));
            } while(cursor.moveToPrevious());
        }
    }


    //BUILD MESSAGE ROUTINES

    public static byte[] buildGrtMsg() {

        byte[] grt = new byte[10], timestamp;

        Cursor cursor = dbManager.fetchTimestamp(BluetoothAdapter.getDefaultAdapter().getAddress());
        cursor.moveToFirst();
        //This device persistent information; at least Username and Last Update fields are not null
        timestamp = longToBytes(cursor.getLong(0)); //last time you updated your profile

        grt[0] = BlueCtrl.GRT_HEADER; //msg header
        grt[1] = BlueCtrl.STS; //user status

        for(int i = 2; i < 10; ++i) {
            grt[i] = timestamp[i-2];
        }
        //8-byte long timestamp

        return grt;
    }

    public static byte[] buildMsg(byte[] target, byte[] sender, byte[] msg) {
        /*
        Use this method to prepare a Text Msg packet to forward to a Bluetooth device.
        Target param is the MAC address of target device, NOT the Bluetooth device ultimately
        receiving the packet from this device. Sender param is the sender's MAC address.
        */

        int length = msg.length;
        /*
        Total length of the message; it is needed in order to ensure synchronized communication, because
        text messages have variable size. A receiving device will know exactly how many byte it has to read
        before communication is over, and it prevents disastrous blocking calls
         */

        byte[] pckt = new byte[16 + length];
        /*
        Actual number of bytes that forms a Text Msg packet:
         - 1 byte for MSG_HEADER
         - 12 bytes for sender and receiver MACs
         - 1 byte for Message type (in this case, Text Msg)
         - 1 byte for Length field
         - the rest are the message bytes
         */

        pckt[0] = MSG_HEADER; //packet header

        int i = 1;
        for(byte b : target) {
            pckt[i] = b;
            ++i;
        }
        //Target field

        for(byte b : sender) {
            pckt[i] = b;
            ++i;
        }
        //Sender field

        pckt[i] = 0; //Text Msg flag
        ++i;

        pckt[i] = BlueCtrl.SPY; //Spy Mode
        System.out.println("SPYMODE = " + BlueCtrl.SPY);
        ++i;

        pckt[i] = (byte) (length);
        ++i;
        /*
        Note that empty messages are not forwarded, so the actual message range is from 1 to 256;
        8-bit representation can only reach 255, therefore range increment is implicit
         */

        for(byte b : msg) {
            pckt[i] = b;
            ++i;
        }
        //Actual Text Msg

        return pckt;
    }

    public static byte[] buildEmoticon(byte[] target, byte[] sender, byte code) {
        /*
        Use this method to prepare an Emoticon Msg to forward to a Bluetooth device.
        All devices shares the emoticon image files, therefore this message only needs to carry
        the emoticon position into its Adapter.
         */

        byte[] pckt = new byte[15];
        /*
        Actual fixed length for an Emoticon Msg:
         - 1 byte for MSG_HEADER
         - 12 bytes for sender and receiver MACs
         - 1 byte for Message type (in this case, Emoticon Msg)
         - 1 byte for emoticon Code
         */

        pckt[0] = MSG_HEADER; //packet header

        int i = 1;
        for(byte b : target) {
            pckt[i] = b;
            ++i;
        }
        //Target field

        for(byte b : sender) {
            pckt[i] = b;
            ++i;
        }
        //Sender field

        pckt[i] = 1; //Msg type (in this case, emoticon)
        ++i;

        pckt[i] = code; //emoticon code

        return pckt;

    }

    public static byte[] buildUpdMsg(ChatUser user) {
        /*
        For DROP REQUEST Instant Reply only: it lacks Number field because these messages are sent
        one after another and are easily managed by MessageThread
         */

        byte[] upd = new byte[16], lastUpd;

        upd[0] = BlueCtrl.UPD_HEADER; //packet header

        int i = 1, j;
        for (j = 0; j < 6; ++j) {
            upd[i + j] = user.getMacInBytes()[j];
        }
        i += j;
        //Device MAC

        lastUpd = longToBytes(user.getLastUpd());
        for (j = 0; j < 8; ++j) {
            upd[i + j] = lastUpd[j];
        }
        i += j;
        //Timestamp of the last time profile was updated

        upd[i] = (byte) (user.getBounces());
        ++i;
        //number of devices on the route

        upd[i] = (byte) user.getStatus();
        //user status

        return upd;
    }

    public static byte[] buildUpdMsg(List<byte[]> updCascade) {
        /*
        Canonical Update Message; it is organized in 16-bytes segments, each one encapsulating information
        from one user. A Number field follows the UPD_HEADER, in order to ensure synchronization and
        coherent reading of the message.
         */

        byte[] upd = new byte[2 + 16 * updCascade.size()];
        /*
        Actual size of the message:
         - 1 byte for the UPD_HEADER
         - 1 byte for the Number field
         - Number time 16 bytes for all the segments;
        Size of a segment:
         - 6 bytes for device MAC address
         - 8 bytes for profile timestamp
         - 1 byte for the number of device on the route
         - 1 byte for user status
         */

        upd[0] = BlueCtrl.UPD_HEADER; //packet header

        upd[1] = (byte) updCascade.size(); //number of segments; at least 1

        for (int i = 0; i < updCascade.size(); ++i) {
            for (int j = 0; j < 16; ++j) {
                upd[2 + i * 16 + j] = updCascade.get(i)[j];
            }
        }
        //adding segments previously collected

        return upd;
    }

    public static byte[] buildCard(Cursor info) {
        /*
        Use this method to retrieve and encode persistent user information requested by a user.
        Username is the only variable size field; for performance issues, Profile Pictures are
        not dispatched, but the last field of this message contains a long value referring to the
        instant the last picture was taken; if an user requests this picture, it is sent via Picture Message
         */

        info.moveToFirst();
        //fetched info

        String mac = info.getString(0), username = info.getString(1);
        long timestamp  = info.getLong(2);
        String profile_pic = info.getString(4);
        int country = info.getInt(5), gender = info.getInt(6), age = info.getInt(7);

        byte[] address = macToBytes(mac), user = username.getBytes(), lastUpd = longToBytes(timestamp),
                card = new byte[27 + user.length];
        /*
        Actual size of a CRD header varies depending on the Username field.
         - 1 byte for the header
         - 6 bytes for MAC address
         - 8 bytes for Last Update timestamp
         - 1 byte for age, gender and country for a total of 3 bytes
         - 1 byte for the Username length field
         - length bytes for Username
         - 8 bytes for picture timestamp
         */

        int i = 0, j;
        card[i] = BlueCtrl.CRD_HEADER; //packet header
        ++i;

        for (j = 0; j < 6; ++j) {
            card[i + j] = address[j];
        }
        i += j;
        //MAC address

        for (j = 0; j < 8; ++j) {
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

        long picture = (profile_pic == null) ? 0l : Long.parseLong(profile_pic);
        //if the user did not choose a new Profile Picture, a value of 0 indicates default image
        byte[] lastPic = longToBytes(picture);

        for (j = 0; j < 8; ++j) {
            card[i + j] = lastPic[j];
        }

        return card;
    }

    public byte[] buildPicture() {

        //TODO
        return null;
    }


    //GUI COMMUNICATION METHODS

    public static boolean awakeUser(String mac, BluetoothDevice manInTheMiddle, byte status, int bounces, long timestamp) {
        /*
        This method is called when a ReceiverThread successfully receives information through GRT MSG
        and needs to forward it to the main thread; because no threads apart from the main one have
        permission to manipulate views, if needed this method will create a new ChatUser instance and add it
        to a ChatUser Buffer, where it will wait to be taken and added to the ChatUser Adapter. If creation is
        not needed, this method returns false, enabling a view update in case user information have been
        changed.
        */

        ChatUser user = scanUsers(mac); //Is the user already in the list?

        if (user == null)
            return userQueue.add(new ChatUser(mac, manInTheMiddle, bounces, status, timestamp, fetchPersistentInfo(mac)));
            //if add method failed it is not recommended to access the buffer

        else {
            user.updateUser(manInTheMiddle, bounces, (int) status);
            //force user information update; regardless of the result, a view update will be done in the main thread
            return false;
        }
    }

    public static void cardUpdate(String address) {
        /*
        This method is in charge of keeping up to date persistent user information in the Database.
        Upon CRD MSG receiving, this method is called from a parallel thread and performs Database transactions
        and ChatUser instance update.
         */

        Cursor info = fetchPersistentInfo(address);
        ChatUser user = scanUsers(address);

        if (user != null) user.addPersistentInfo(info);
    }

    public static void showReceivedMsg(String from, String msg, Date time) {
        /*
        This method is called from a ReceiverThread upon successfully receiving a Text Message from a remote
        device, but only if the receiver field matches this device MAC address. A record is created in
        the Database and a ChatMessage instance added to a buffer, so that main thread can update the Chat
        view after receiving a message via Handler.
         */

        insertMsgTable(msg, from, time, 1, 0); //DB transition routine

        if (from.equals(msgAdapt.getAddress())){
            msgBuffer.add(new ChatMessage(msg, true, time, false));
        }
    }

    public static void showReceivedEmo(String from, int code, Date time){

        /*
        This method is called from a ReceiverThread upon successfully receiving an Emoticon Message from a remote
        device, but only if the receiver field matches this device MAC address. Emoticon and Text messages
        generate different views, and they have to be managed separately. A record is created in
        the Database and a ChatMessage instance added to a buffer, so that main thread can update the Chat
        view after receiving a message via Handler.
         */

        insertMsgTable(String.valueOf(code), from, time, 1, 1); //DB transition routine

        if (from.equals(msgAdapt.getAddress())){
            msgBuffer.add(new ChatMessage(String.valueOf(code), true, time, true));
        }
    }






    public static ChatUser scanUsers(String address) {

        System.out.println("searching for " + address);
        for (ChatUser user : userList) {
            if (user.getMac().equals(address)) return user;
        }

        return null;
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


    //DEPRECATED

    /*public static boolean addCloseDvc(BluetoothDevice dvc) {

        boolean newcomer;
        int pos;
        if ((pos = closeDvc.indexOf(dvc)) != -1) {
            System.out.println(pos);
            System.out.println(counter);
            //swap
            /*if (pos != counter) {
                closeDvc.set(pos, closeDvc.get(counter));
                closeDvc.set(counter, dvc);
            }*/
            /*newcomer = false;
        }
        //insert into position
        else {
            System.out.println("adding");
            System.out.println("pippo: " + counter);
            //closeDvc.add(/*counter, *//*dvc);
            newcomer = true;
        }

        ++counter;
        System.out.println("pippo: " + counter);
        return newcomer;
    }*/

    /*if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

    String pic = info.getString(5);
    if (pic == null) {
        dbManager.updatePicture(address);
    }
    File directory = new File(BlueCtrl.appFolder, Environment.DIRECTORY_PICTURES);
    OutputStream outputStream = new FileOutputStream(directory.getAbsolutePath() + "/IMG_" + address);
    outputStream.write(image);

}*/

    public static BluetoothDevice cleanCloseDvc() {

        if (closeDvc.size() > counter) return closeDvc.remove(counter);
        return null;
    }


    //DB OPERATIONS
        //INITIALIZATION

    public static void openDatabase(Context context) {
        /*
        A Database is opened or created from scratches; it will contain an User Table for persistent
        user information and a History Table for messages storing. A reference to this instante is
        saved into the static field dbManager.
        */

        dbManager = new BlueDBManager(context, dbname);
    }

        //RECORD MANAGEMENT

    public static void insertUserTable(String mac, long timestamp, String username, int age, int gender, int country) {
        //This method performs record creation or record replacing of persistent user information

        dbManager.createRecord(0, new Object[]{mac, username, timestamp, false, null, country, gender, age});
    }

    public static void insertMsgTable(String msg, String address, Date time, int sentBy, int emoticon) {
        //This method performs record creation in the Message Table

        dbManager.createRecord(1, new Object[]{msg, address, time.getTime(), sentBy, emoticon});
    }

    public static void remove(ChatMessage message) {
        //This method removes the selected record from the Message Table

        dbManager.removeMessage(message.getId());
    }

        //FETCH INFORMATION

    public static Cursor fetchPersistentInfo(String address) {
        //Interface method for fetching persistent user information routine

        return dbManager.fetchUserInfo(address);
    }

    public static List<ChatMessage> fetchHistory(String quote, int mode) {
        /*
        Interface method for message history fetching, given a keyword and a where clause; it returns
        the cursor records unpacked into an ArrayList containing ChatMessage instances
        */

        ArrayList<ChatMessage> msgList = new ArrayList<>();
        String search;

        if (quote == null) search = null; //Abort fetch
        else {

            switch (mode) {
                case 0:
                    //Search into messages only
                    search = dbManager.historyTable[0] + " LIKE '%" + quote + "%'";
                    break;
                case 1:
                    //Search by username only
                    search = dbManager.historyTable[5] + " LIKE '%" + quote + "%'";
                    break;
                default:
                    //Both
                    search = dbManager.historyTable[0] + " LIKE '%" + quote + "%' AND " +
                            dbManager.historyTable[5] + " LIKE '%" + quote + "%'";
            }
        }

        Cursor cursor = dbManager.fetchQuotes(search); //DB transaction
        if (cursor != null && cursor.moveToFirst()) {

            do {

                msgList.add(new ChatMessage(cursor.getString(0), cursor.getString(1),
                        cursor.getInt(4), false, new Date(cursor.getLong(2)), cursor.getInt(3) == 1));
                //Records unpacking
            } while(cursor.moveToNext());
        }

        return msgList;
    }

        //UTILITY

    public static boolean validateUser(String address, long timestamp) {
        /*
        This method is called when application needs to compare user information timestamps; it returns
        true if information is up to date, false if it is out of date, the record is empty or absent
         */

        Cursor cursor = dbManager.fetchTimestamp(address);

        if (cursor == null || cursor.getCount() != 1) return false;
        return (cursor.moveToFirst() && cursor.getLong(0) == timestamp);
    }


    //UTILITY

    public static byte[] macToBytes(String address) {
        /*
        Utility method for MAC address translation in bytes; this is for performance enhancing, because
        a MAC address can have variable length spacing from 17 to 23 bytes, whilst byte array representation
        only takes 6 bytes and is easier to manage. It is only used in controlled environment, therefore
        MAC validation is not required
         */


        byte[] mac = new byte[6];
        String[] digits = address.toLowerCase().split(":"); //Isolate hexadecimal values
        byte b, counter = 0;

        for(String d : digits) {

            b = (byte) Integer.parseInt(d, 16); //parsing
            mac[counter] = b;
            ++counter;
        }

        return mac;
    }

    public static String bytesToMAC(byte[] mac) {
        /*
        Utility method for byte array translation in MAC address; this is method is needed in order to
        properly use MAC addresses Strings. It is only used in controlled environment, but some checks
        are still performed
         */

        if (mac.length == 6) {

            String address = "";
            int i;
            boolean bool = false;

            for(byte b : mac) {

                i = (b < 0) ? (b + 256) : (int) b; //value

                if (bool) address += ':'; //do not insert ':' at the beginning
                else bool = !bool; //boolean lock

                if (i < 16) address += "0"; //every field has a minimum of teo digits
                address += Integer.toHexString(i).toUpperCase(); //reconstruction
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
        System.out.println("LOCK " + DISCOVERY_LOCK);
        if (DISCOVERY_SUSPENDED) return; //the door was already opened

        DISCOVERY_SUSPENDED = true; //thread opened the door

        BluetoothAdapter.getDefaultAdapter().cancelDiscovery(); //and turned on lights
    }

    public static void unlockDiscoverySuspension() {
        if ((--DISCOVERY_LOCK) < 1) { //thread left the room

            System.out.println("UNLOCKED");

            DISCOVERY_SUSPENDED = false; //if thread was the last one in the room, turns off lights

            if (!BluetoothAdapter.getDefaultAdapter().isDiscovering())
                BluetoothAdapter.getDefaultAdapter().startDiscovery(); //and closes the door
        }
        else System.out.println("OPEN DOOR " + DISCOVERY_LOCK);
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

    public static Collection dropUsers(String address) {
        return userAdapt.dropUsers(address);
    }

    public static Cursor fetchMsgHistory(String address, long timestamp){
        return dbManager.fetchMsgHistory(address, timestamp);
    }

    public static boolean validatePicture(String address, long timestamp) {

        return (dbManager.fetchProfilePicCode(address) == timestamp);
    }

    public static BluetoothDevice scanUsersForDvc(String address) {

        ChatUser user = scanUsers(address);
        if (user != null) return user.getNextNode();
        else return null;
    }

    public static byte[] encrypt(byte encrypt[]){

        try {

            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            String encrypted = Base64.encodeToString(cipher.doFinal(encrypt), Base64.DEFAULT);
            System.out.println("Encrypted: " + encrypted);

            return encrypted.getBytes();

        }

        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static byte[] decrypt(byte[] encrypted){
        try {
            Key k = new SecretKeySpec(key, "AES");
            Cipher c = Cipher.getInstance("AES");
            c.init(Cipher.DECRYPT_MODE, k);
            byte[] ciao = Base64.decode(encrypted, Base64.DEFAULT);
            byte[] decValue = c.doFinal(ciao);
            String decryptedValue = new String(decValue);

            System.out.println("Decrypted: " + decryptedValue);

            return decValue;
        }

        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void setFavorite(String mac, int value) {

        dbManager.updateFavorites(mac, value);
    }
}
