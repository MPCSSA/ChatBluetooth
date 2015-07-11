package com.example.christian.chatbluetooth.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Base64;

import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;


public class BlueDBManager {

    private Context context;
    private SQLiteDatabase db;
    private final String[] tables = {"user", "history", "Nation"};
    public final String[] userTable = {"mac","username","last_upd","isFav","profile_pic","nationality","gender","age"}; //field User Table
    public final String[] historyTable = {"msg","user","date","sent_by", "is_emo", "username"}; //field Hystory Table
    public final String[] nationTable = {"idNation","NameITA","NameEng","Code", "Pos"}; //field Nation Table

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
                    historyTable[2] + " text not null, " + historyTable[3] + " integer not null, " +
                    historyTable[4] + " integer not null, " + historyTable[5] + " text)";
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

    private ContentValues createCV(String msg, String user, long timestamp, int sent_by, int is_emoticon) {

        ContentValues values = new ContentValues();

        values.put(historyTable[0], msg);
        values.put(historyTable[1], user);
        values.put(historyTable[2], timestamp);
        values.put(historyTable[3], sent_by);
        values.put(historyTable[4], is_emoticon);
        String username;
        if (sent_by == 0) username = "You";
        else {

            Cursor c = fetchUsername(user);
            c.moveToFirst();
            username = c.getString(0);
        }
            values.put(historyTable[5], username);


        return values;
    }

    public long createRecord(int table, Object[] attrs) {

        long id;

        switch (table) {

            case 0:
                id = db.insertWithOnConflict(tables[table], null,
                        createCV((String) attrs[0], (String) attrs[1], (long) attrs[2], (boolean) attrs[3],
                                 (String) attrs[4], (int) attrs[5], (int) attrs[6], (int) attrs[7]),
                        SQLiteDatabase.CONFLICT_REPLACE);
                break;

            case 1:
                id = db.insertWithOnConflict(tables[table], null,
                        createCV((String) attrs[0], (String) attrs[1], (long) attrs[2], (int) attrs[3], (int) attrs[4]),
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

        return db.query(tables[2], new String[] {nationTable[1], nationTable[2], nationTable[4]}, null, null, null, null, null, "1");
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
}