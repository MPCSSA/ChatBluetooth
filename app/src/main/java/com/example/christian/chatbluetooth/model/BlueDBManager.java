package com.example.christian.chatbluetooth.model;

import android.bluetooth.BluetoothAdapter;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Base64;
import android.widget.Toast;

import com.example.christian.chatbluetooth.R;
import com.example.christian.chatbluetooth.controller.BlueCtrl;
import com.example.christian.chatbluetooth.view.Activities.ChatActivity;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;


public class BlueDBManager {

    private Context context;
    private SQLiteDatabase db;
    private final String[] tables = {"user", "history", "nation"};
    public final String[] userTable = {"mac","username","last_upd","isFav","profile_pic","nationality","gender","age"}; //field User Table
    public final String[] historyTable = {"msg","user","date","sent_by", "is_emo", "username"}; //field Hystory Table
    public final String[] nationTable = {"name_ita","name_eng","code", "pos"}; //field Nation Table

    public void setContext(Context context) { this.context = context; }

    public SQLiteDatabase getDb() {
        return db;
    }

    public void setDb(SQLiteDatabase db) {
        this.db = db;
    }

    public BlueDBManager(Context context, String db) {
        this(context, context.openOrCreateDatabase(db, Context.MODE_PRIVATE, null));
    }

    public BlueDBManager(Context context, SQLiteDatabase db) {
        setContext(context); //bound to activity

        //create User and History tables if this is first opening of DB
        String query = "SELECT name FROM sqlite_master WHERE type = \'table\' AND name = \'" + tables[0] + "\'";
        if (!db.rawQuery(query, null).moveToFirst()) {
            query = "CREATE TABLE " + tables[0] + "(_id integer primary key autoincrement, " +
                            userTable[0]+ " text not null, "+ userTable[1] + " text not null, " + userTable[2] +  " integer not null, " +
                            userTable[3] + " boolean not null, " + userTable[4] + " text, " + userTable[5] +
                            " integer, " + userTable[6] + " integer, " + userTable[7] + " integer, UNIQUE  (mac) ON CONFLICT REPLACE)";
            db.execSQL(query);
        }

        query = "SELECT name FROM sqlite_master WHERE type = \'table\' AND name = \'" + tables[1] + "\'";
        if (!db.rawQuery(query, null).moveToFirst()) {
            query = "CREATE TABLE " + tables[1] + "(_id integer primary key autoincrement, " +
                    historyTable[0] + " text not null, " + historyTable[1] + " integer not null, " +
                    historyTable[2] + " text not null, " + historyTable[3] + " integer not null, " +
                    historyTable[4] + " integer not null, " + historyTable[5] + " text)";
            db.execSQL(query);
        }

        query = "SELECT name FROM sqlite_master WHERE type = \'table\' AND name = \'" + tables[2] + "\'";

        boolean bool = !db.rawQuery(query, null).moveToFirst();
        if (bool) {
            query = "CREATE TABLE " + tables[2] + "(_id integer primary key autoincrement, " +
                    nationTable[0] + " text not null, " + nationTable[1] + " text not null, " +
                    nationTable[2] + " text not null, " + nationTable[3] + " integer not null)";
            db.execSQL(query);
        }

        setDb(db);

        if (bool) initializeFlags();
    }

    private void initializeFlags() {

        createRecord(2, new Object[] {"Albania", "Albania", 1, "ALB"});
        createRecord(2, new Object[] {"Argentina", "Argentina", 5, "ARG"});
        createRecord(2, new Object[] {"Australia", "Australia", 7, "AUS"});
        createRecord(2, new Object[] {"Austria", "Austria", 8, "AUT"});
        createRecord(2, new Object[] {"Belgium", "Belgio", 15 ,"BEL"});
        createRecord(2, new Object[] {"Belize", "Belize", 16, "BLZ"});
        createRecord(2, new Object[] {"Brazil", "Brasile", 22, "BRA"});
        createRecord(2, new Object[] {"Canada", "Canada", 29, "CAN"});
        createRecord(2, new Object[] {"Croatia", "Croazia", 41, "HRV"});
        createRecord(2, new Object[] {"Denmark", "Danimarca", 45, "DNK"});
        createRecord(2, new Object[] {"Egypt", "Egitto", 51, "EGY"});
        createRecord(2, new Object[] {"Finland", "Finlandia", 58, "FIN"});
        createRecord(2, new Object[] {"France", "Francia", 59, "FRA"});
        createRecord(2, new Object[] {"Germany", "Germania", 63, "DEU"});
        createRecord(2, new Object[] {"Greece", "Grecia", 65, "GRC"});
        createRecord(2, new Object[] {"Iceland", "Islanda", 74, "ISL"});
        createRecord(2, new Object[] {"India", "India", 75, "IND"});
        createRecord(2, new Object[] {"Ireland", "Irlanda", 79, "IRL"});
        createRecord(2, new Object[] {"Italy", "Italia", 81, "ITA"});
        createRecord(2, new Object[] {"Japan", "Giappone", 83, "JPN"});
        createRecord(2, new Object[] {"Luxembourg", "Lussemburgo", 101, "LUX"});
        createRecord(2, new Object[] {"Mexico", "Messico", 112, "MEX"});
        createRecord(2, new Object[] {"Netherland", "Paesi Bassi", 115, "NLD"});
        createRecord(2, new Object[] {"New Zealand", "Nuova Zelanda", 125, "NZL"});
        createRecord(2, new Object[] {"Norway", "Norvegia", 129, "NOR"});
        createRecord(2, new Object[] {"Russia", "Russia", 142, "RUS"});
        createRecord(2, new Object[] {"Spain", "Spagna", 162, "ESP"});
        createRecord(2, new Object[] {"Sweden", "Svezia", 167, "SWE"});
        createRecord(2, new Object[] {"Switzerland", "Svizzera", 168, "CHE"});
        createRecord(2, new Object[] {"Turkey", "Turchia", 169, "TUR"});
        createRecord(2, new Object[] {"United Kingdom", "Regno Unito", 184,"GBR"});
        createRecord(2, new Object[] {"USA","USA", 185, "USA"});
    }

    private ContentValues createCV(String address, String username, long timestamp, boolean isFav, String profile_pic, int nation, int gender, long age) {

        ContentValues values = new ContentValues();

        values.put(userTable[0], address);
        values.put(userTable[1], username);
        values.put(userTable[2], timestamp);
        values.put(userTable[3], isFav);
        values.put(userTable[4], profile_pic);
        values.put(userTable[5], nation);
        values.put(userTable[6], gender);
        values.put(userTable[7], age);

        return values;
    }

    private ContentValues createCV(String msg, String user, long timestamp, int sent_by, int is_emoticon) {

        ContentValues values = new ContentValues();

        values.put(historyTable[0], msg);
        values.put(historyTable[1], user);
        values.put(historyTable[2], timestamp);
        values.put(historyTable[3], sent_by);
        values.put(historyTable[4], is_emoticon);
        String username;
        if (sent_by == 0) username = context.getString(R.string.you);
        else {

            Cursor c = fetchUsername(user);
            c.moveToFirst();
            username = c.getString(0);
        }
            values.put(historyTable[5], username);


        return values;
    }

    private ContentValues createCV(String ita, String eng, String code, int pos) {

        ContentValues values = new ContentValues();

        values.put(nationTable[0], ita);
        values.put(nationTable[1], eng);
        values.put(nationTable[2], code);
        values.put(nationTable[3], pos);

        return values;
    }

    public long createRecord(int table, Object[] attrs) {

        long id;

        switch (table) {

            case 0:
                id = db.insertWithOnConflict(tables[table], null,
                        createCV((String) attrs[0], (String) attrs[1], (long) attrs[2], (boolean) attrs[3],
                                 (String) attrs[4], (int) attrs[5], (int) attrs[6], (long) attrs[7]),
                        SQLiteDatabase.CONFLICT_REPLACE);
                break;

            case 1:
                id = db.insertWithOnConflict(tables[table], null,
                        createCV((String) attrs[0], (String) attrs[1], (long) attrs[2], (int) attrs[3], (int) attrs[4]),
                        SQLiteDatabase.CONFLICT_REPLACE);
                break;

            case 2:
                id = db.insertWithOnConflict(tables[table], null,
                        createCV((String) attrs[1], (String) attrs[0], (String) attrs[3], (int) attrs[2]),
                        SQLiteDatabase.CONFLICT_REPLACE);
                break;

            default:
                id = -1;
        }

        System.out.println("DB error: " + id);
        return id;
    }


    //FETCH METHODS

    public Cursor fetchUsername(String address) {

        return db.query(tables[0], new String[] {userTable[1]}, userTable[0] + " = \'" + address + "\'", null, null, null, null, null);
    }

    public Cursor fetchUserInfo(String address) {

        return db.query(tables[0], userTable, userTable[0] + " = \'" + address + "\'", null, null, null, null, "1");

    }

    public Cursor fetchMsgHistory(String address, long timestamp) {

        return db.query(tables[1], new String [] {historyTable[0], historyTable[2], historyTable[3], historyTable[4]},
                historyTable[1] + " = \'" + address + "\' AND " + historyTable[2] + " < " + timestamp,
                null, null, null, historyTable[2] + " DESC", "25");

    }

    public Cursor fetchQuotes(String searchBy) {

        return db.query(tables[1], new String[] {historyTable[0], historyTable[5], historyTable[2], historyTable[4], "_id"},
                searchBy, null, null, null, historyTable[2], null);
    }

    public Cursor fetchTimestamp(String address) {

        return db.query(tables[0], new String[]{userTable[2]}, userTable[0] + " = \'" + address + "\'", null, null, null, null, "1");

    }

    public long fetchProfilePicCode(String address) {

        Cursor cursor = db.query(tables[0], new String[] {userTable[4]}, userTable[0] + " = \'" + address, null, null, null, null, "1");
        cursor.moveToFirst();

        String str = cursor.getString(0);
        if (str == null) {
            return 0;
        }
        else return Long.parseLong(cursor.getString(0));
    }

    public Cursor fetchCountry(int id) {

        return db.query(tables[2], new String[] {nationTable[0], nationTable[1], nationTable[3]}, "_id = " + id, null, null, null, null, "1");
    }

    public Cursor fetchCountries() {

        System.out.println("fetching");

        Cursor c = db.query(tables[2], new String[] {nationTable[0], nationTable[1], nationTable[3]}, null, null, null, null, null, null);
        System.out.println("HOW MANY? " + c.getCount());
        return db.query(tables[2], new String[]{nationTable[0], nationTable[1], nationTable[3]}, null, null, null, null, null, null);
    }

    //UPDATE METHODS

    public long updateFavorites(String address, int isFav) {

        ContentValues values = new ContentValues();

        values.put(userTable[3], isFav);

        return db.update(tables[0], values, userTable[0] + " = \'" + address + "\'", null);
    }

    //REMOVE METHODS

    public void removeMessage(Integer id) {

        db.delete(tables[1], "_id = " + id, null);
    }

    public void updateProfile(long timestamp, String usr, int country, int gender, long age) {

        ContentValues values = new ContentValues();

        values.put(userTable[1], usr);
        values.put(userTable[2], timestamp);
        values.put(userTable[5], country);
        values.put(userTable[6], gender);
        values.put(userTable[7], age);

        db.update(tables[0], values, userTable[0] + " = \'" + BluetoothAdapter.getDefaultAdapter().getAddress() + "\'", null);

        BlueCtrl.dispatchNews(BlueCtrl.buildGrtMsg(), null, ((ChatActivity)context).getHandler());
    }
}