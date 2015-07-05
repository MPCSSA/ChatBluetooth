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
    private final String[] historyTable ={"msg","user","date","sent_by"}; //field Hystory Table

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
                    historyTable[2] + " text not null, " + historyTable[3] + " integer not null)";
            db.execSQL(query);
        }

        //TODO: Nations Table

        setDb(db);
    }

    private ContentValues createCV(String address, String username, long timestamp, boolean isFav, String profile_pic, int nation, int gender, int age) {

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

    private ContentValues createCV(String msg, String user, long timestamp, int sent_by) {

        ContentValues values = new ContentValues();

        values.put(historyTable[0], msg);
        values.put(historyTable[1], user);
        values.put(historyTable[2], timestamp);
        values.put(historyTable[3], sent_by);

        return values;
    }

    public long createRecord(int table, Object[] attrs) {

        long id;

        System.out.println("creating the magic");
        switch (table) {

            case 0:
                id = db.insertWithOnConflict(tables[table], null,
                        createCV((String) attrs[0], (String) attrs[1], (long) attrs[2], (boolean) attrs[3],
                                 (String) attrs[4], (int) attrs[5], (int) attrs[6], (int) attrs[7]),
                        SQLiteDatabase.CONFLICT_REPLACE);
                break;

            case 1:
                id = db.insertWithOnConflict(tables[table], null,
                        createCV((String) attrs[0], (String) attrs[1], (long) attrs[2], (int) attrs[3]),
                        SQLiteDatabase.CONFLICT_REPLACE);
                break;

            default:
                id = -1;
        }

        System.out.println("DB error: " + id);
        return id;
    }


    //TODO: fetchMethods
    public Cursor fetchListedUser(String address) {

        return db.query(tables[0], new String[]{userTable[0], userTable[4], userTable[3]}, userTable[0] + " = \'" + address + "\'", null, null, null, null, "1");

    }

    public Cursor fetchUserInfo(String address) {

        return db.query(tables[0], new String[] {userTable[0], userTable[1], userTable[2],
                                                 userTable[5], userTable[6], userTable[7]},
                        userTable[0] + " = \'" + address + "\'", null, null, null, null, "1");

    }

    public Cursor fetchMsgHistory(String address) {

        return db.query(tables[1], new String [] {historyTable[0], historyTable[1], historyTable[3]}, historyTable[1] + " = \'" + address + "\'", null, null, null
                        , historyTable[2] + " DESC", "25");

    }

    public Cursor fetchTimestamp(String address) {

        return db.query(tables[0], new String[] {userTable[2]}, userTable[0] + " = \'" + address + "\'", null, null, null, null, "1");

    }

    //TODO: Update methods

    public long updateUserInfo(String address, String username, String profile_pic, int nation, int gender, int age, long timestamp) {

        ContentValues values = new ContentValues();
        values.put(userTable[1], username);
        values.put(userTable[2], timestamp);
        values.put(userTable[4], profile_pic);
        values.put(userTable[5], nation);
        values.put(userTable[6], gender);
        values.put(userTable[7], age);


        return db.update(tables[0], values, userTable[0] + " = \'" + address + "\'", null);
    }

    public long updateFavourites(String address, boolean isFav) {

        ContentValues values = new ContentValues();

        values.put(userTable[3], isFav);

        return db.update(tables[0], values, userTable[0] + " = \'" + address + "\'", null);
    }

    public long updatePicture(String address) {
        ContentValues values = new ContentValues();

        values.put(userTable[4], "IMG_" + address);

        return db.update(tables[0], values, userTable[0] + " = \'" + address + "\'", null);
    }
}