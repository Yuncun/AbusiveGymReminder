package com.pipit.agc.agc.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Eric on 1/10/2016.
 */
public class MySQLiteHelper extends SQLiteOpenHelper {

    /**DayRecords Table**/
    public static final String TABLE_DAYRECORDS = "dayrecords";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_DAYRECORDS = "dayrecord"; //Any string associated with DayRecord (comments, etc)
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_ISGYMDAY = "isgymday";
    public static final String COLUMN_BEENTOGYM = "beentogym";
    private static final String DATABASE_NAME = "dayrecords.db";
    private static final int DATABASE_VERSION = 13;

    private static final String CREATE_TABLE_DAYRECORDS = "create table "
            + TABLE_DAYRECORDS + "(" + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_DAYRECORDS + " text not null, "
            + COLUMN_DATE + " text default 0, "
            + COLUMN_ISGYMDAY + " integer default 0, "
            + COLUMN_BEENTOGYM + " integer default 0 "
            + ");";

    /**Message Table**/
    public static final String TABLE_MESSAGES = "messages";
    public static final String COLUMN_MESSAGE_BODY = "message";
    public static final String COLUMN_MESSAGE_HEADER = "header";
    public static final String COLUMN_REPO_ID = "repoid"; //The message repo ID of the message - can be 0 from non-repo messages
    public static final String COLUMN_REASON = "reason";
    public static final String COLUMN_READ = "read";

    private static final String CREATE_TABLE_MESSAGES = "create table "
            + TABLE_MESSAGES + "(" + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_MESSAGE_HEADER + " text not null, "
            + COLUMN_MESSAGE_BODY + " text not null, "
            + COLUMN_DATE + " text default 0, "
            + COLUMN_REASON + " integer default 0, "
            + COLUMN_REPO_ID + " integer default 0, "
            + COLUMN_READ + " integer default 0"
            + ");";

    /**SQL Helper functions**/
    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(CREATE_TABLE_DAYRECORDS);
        database.execSQL(CREATE_TABLE_MESSAGES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(MySQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");

            db.execSQL("DROP TABLE IF EXISTS " + TABLE_DAYRECORDS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
            onCreate(db);
    }

}