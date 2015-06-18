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

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

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
        setContext(context);

        String query = "SELECT name FROM sqlite_master WHERE type = \'table\' AND name = \'" + tables[0] + "\'";
        if (!db.rawQuery(query, null).moveToFirst()) {
            query = "CREATE TABLE " + tables[0] + "(integer _id primary key autoincrement, " +
                            "text mac not null, text username not null, text profile_pic, boolean isFav not null, " +
                            "text nationality, integer gender, integer age, integer last_upd not null, " +
                            "UNIQUE  (mac) ON CONFLICT REPLACE)";
            db.execSQL(query);
        }

        query = "SELECT name FROM sqlite_master WHERE type = \'table\' AND name = \'" + tables[1] + "\'";
        if (!db.rawQuery(query, null).moveToFirst()) {
            query = "CREATE TABLE " + tables[1] + "(integer _id primary key autoincrement, " +
                    "text msg not null, integer date not null, text user not null, boolean sent_by not null)";
            db.execSQL(query);
        }

        setDb(db);
    }

    private ContentValues createCV(String address, String username, long timestamp, boolean isFav, String profile_pic, String nation, int gender, int age) {

        ContentValues values = new ContentValues();

        values.put("mac", address);
        values.put("username", username);
        values.put("last_upd", timestamp);
        values.put("isFav", isFav);
        values.put("profile_pic", profile_pic);
        values.put("nationality", nation);
        values.put("gender", gender);
        values.put("age", age);

        return values;
    }

    private ContentValues createCV(String msg, String user, long timestamp, boolean sent_by) {

        ContentValues values = new ContentValues();

        values.put("msg", msg);
        values.put("user", user);
        values.put("date", timestamp);
        values.put("sent_by", sent_by);

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
    private Cursor fetchListedUser(String address) {

        return db.query(tables[0], new String[]{"username", "profile_pic", "isFav"}, "mac = " + address, null, null, null, null, "1");

    }

    private Cursor fetchUserInfo(String address) {

        return db.query(tables[0], null, "mac = " + address, null, null, null, null, "1");

    }

    private Cursor fetchMsgHistory(String address) {

        return db.query(tables[1], new String [] {"msg", "date", "sent_by"}, "mac = " + address, null, null, null, "date", "25");

    }

    private Cursor fetchTimestamp(String address) {

        return db.query(tables[0], new String[] {"last_upd"}, "mac = " + address, null, null, null, null, "1");

    }

    //TODO: Update methods

    private long updateUserInfo(String address, String username, String profile_pic, String nation, int gender, int age, long timestamp) {

        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("profile_pic", profile_pic);
        values.put("nationality", nation);
        values.put("gender", gender);
        values.put("age", age);
        values.put("last_upd", timestamp);

        return db.update(tables[0], values, "mac = " + address, null);
    }

    private long updateFavourites(String address, boolean isFav) {

        ContentValues values = new ContentValues();

        values.put("isFav", isFav);

        return db.update(tables[0], values, "mac = " + address, null);
    }
}
