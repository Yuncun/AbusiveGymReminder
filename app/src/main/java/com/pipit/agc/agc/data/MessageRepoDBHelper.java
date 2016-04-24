package com.pipit.agc.agc.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

/**
 * For the use of the offline message repo
 * Created by Eric on 2/17/2016.
 */
public class MessageRepoDBHelper extends SQLiteAssetHelper {
    public static final String DATABASE_NAME = "messagerepo.db";
    public static final String TABLE_MESSAGES = "Messages";
    public static final int DATABASE_VERSION = 8;

    //Table Names
    public static final String COLUMN_ANGER = "Anger";
    public static final String COLUMN_TYPE = "Type";
    public static final String COLUMN_ID = "ID";
    public static final String COLUMN_UNIQUE = "Unique";

    public MessageRepoDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(MessageRepoDBHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        onCreate(db);
    }
}