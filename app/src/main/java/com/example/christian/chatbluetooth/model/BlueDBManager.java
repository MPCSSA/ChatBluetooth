package com.example.christian.chatbluetooth.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;


public class BlueDBManager {

    private Context context;
    private SQLiteDatabase db;
    private final String[] tables = {"user", "history"};
    private final String[] userTable = {"mac","username","last_upd","isFav","profile_pic","nationality","gender","age"}; //field User Table
    private final String[] historyTalble ={"msg","date","user","sent_by"}; //field Hystory Table

    public Context getContext() { return context; }

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
            query = "CREATE TABLE " + tables[0] + "(integer _id primary key autoincrement, " +
                            "text " + userTable[0]+ " not null, "+ "text " + userTable[1] + " not null, " +  ", integer " + userTable[2] + " not null" +
                            ", boolean " + userTable[3] + " not null, " + "text " + userTable[4] + " not null, " + "text " + userTable[5] +
                            ", integer " + userTable[6] + ", integer " + userTable[7] + "UNIQUE  (mac) ON CONFLICT REPLACE)";
            db.execSQL(query);
        }

        query = "SELECT name FROM sqlite_master WHERE type = \'table\' AND name = \'" + tables[1] + "\'";
        if (!db.rawQuery(query, null).moveToFirst()) {
            query = "CREATE TABLE " + tables[1] + "(integer _id primary key autoincrement, " +
                    "text " + historyTalble[0] + " not null, integer " + historyTalble[1] + " not null, " +
                    "text " + historyTalble[2] + " not null, boolean " + historyTalble[3] + " not null)";
            db.execSQL(query);
        }

        setDb(db);
    }

    private ContentValues createCV(String address, String username, long timestamp, boolean isFav, String profile_pic, String nation, int gender, int age) {

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

    private ContentValues createCV(String msg, String user, long timestamp, boolean sent_by) {

        ContentValues values = new ContentValues();

        values.put(historyTalble[0], msg);
        values.put(historyTalble[1], user);
        values.put(historyTalble[2], timestamp);
        values.put(historyTalble[3], sent_by);

        return values;
    }

    public long createRecord(int table, Object[] attrs) {

        long id;
        switch (table) {

            case 0:
                id = db.insertWithOnConflict(tables[table], null,
                        createCV((String) attrs[0], (String) attrs[1], (long) attrs[2], (boolean) attrs[3],
                                 (String) attrs[4], (String) attrs[5], (int) attrs[6], (int) attrs[7]),
                        SQLiteDatabase.CONFLICT_IGNORE);
                break;

            case 1:
                id = db.insertWithOnConflict(tables[table], null,
                        createCV((String) attrs[0], (String) attrs[1], (long) attrs[2], (boolean) attrs[3]),
                        SQLiteDatabase.CONFLICT_IGNORE);
                break;

            default:
                id = -1;
        }

        return id;
    }


    //TODO: fetchMethods
    public Cursor fetchListedUser(String address) {

        return db.query(tables[0], new String[]{userTable[0], userTable[4], userTable[3]}, userTable[0] + " = " + address, null, null, null, null, "1");

    }

    public Cursor fetchUserInfo(String address) {

        return db.query(tables[0], null, userTable[0] + " = " + address, null, null, null, null, "1");

    }

    public Cursor fetchMsgHistory(String address) {

        return db.query(tables[1], new String [] {historyTalble[0], historyTalble[1], historyTalble[3]}, userTable[0] + " = " + address, null, null, null
                        , historyTalble[3], "25");

    }

    public Cursor fetchTimestamp(String address) {

        return db.query(tables[0], new String[] {userTable[2]}, userTable[0] + " = " + address, null, null, null, null, "1");

    }

    //TODO: Update methods

    public long updateUserInfo(String address, String username, String profile_pic, String nation, int gender, int age, long timestamp) {

        ContentValues values = new ContentValues();
        values.put(userTable[1], username);
        values.put(userTable[2], timestamp);
        values.put(userTable[4], profile_pic);
        values.put(userTable[5], nation);
        values.put(userTable[6], gender);
        values.put(userTable[7], age);


        return db.update(tables[0], values, userTable[0] + " = " + address, null);
    }

    public long updateFavourites(String address, boolean isFav) {

        ContentValues values = new ContentValues();

        values.put(userTable[3], isFav);

        return db.update(tables[0], values, userTable[0] + " = " + address, null);
    }
}
