package com.pipit.agc.agc.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Eric on 1/10/2016.
 */
public class MySQLiteHelper extends SQLiteOpenHelper {

    public static final String TABLE_DAYRECORDS = "dayrecords";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_DAYRECORDS = "dayrecord";
    public static final String COLUMN_DATE = "date";

    private static final String DATABASE_NAME = "dayrecords.db";
    private static final int DATABASE_VERSION = 2;

    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_DAYRECORDS + "(" + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_DAYRECORDS
            + " text not null, " + COLUMN_DATE
            + " text default 0" +
            ");";

    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(MySQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");

            db.execSQL("DROP TABLE IF EXISTS " + TABLE_DAYRECORDS);
            onCreate(db);

    }

}