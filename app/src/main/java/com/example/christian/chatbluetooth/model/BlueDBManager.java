package com.example.christian.chatbluetooth.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by stefano on 10/06/15.
 */
public class BlueDBManager {

    private Context context;
    private SQLiteDatabase db;
    private final String[] tables = {"user", "msg", "history"};

    public BlueDBManager(Context context, SQLiteDatabase db) {
        this.context = context;
        this.db = db;
    }

    private ContentValues createCV(String msg) {

        //TODO: build ContentValue for msg table

        ContentValues value = null;

        return value;
    }

    private ContentValues createCV(String name, String mac, char fav) {

        //TODO: build ContentValue for user table

        ContentValues value = null;

        return value;
    }

    public long createRecord(char type, Object[] attributes) {

        ContentValues value;

        switch (type) {
            case 0:
                value = createCV((String) attributes[0]);
                break;

            case 1:
                value = createCV((String) attributes[0], (String) attributes[1], (char) attributes[2]);
                break;

            default:
                value = null;
        }

        return db.insertWithOnConflict(tables[type], null, value, SQLiteDatabase.CONFLICT_IGNORE);
    }


    //TODO: fetchMethods
}
