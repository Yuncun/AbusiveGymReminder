package com.pipit.agc.agc.data;

import android.content.Context;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

/**
 * For the use of the offline message repo
 * Created by Eric on 2/17/2016.
 */
public class MessageRepoDBHelper extends SQLiteAssetHelper {
    public static final String DATABASE_NAME = "messagerepo.db";
    public static final String TABLE_MESSAGES = "Messages";
    public static final int DATABASE_VERSION = 1;

    //Table Names
    public static final String COLUMN_ANGER = "Anger";
    public static final String COLUMN_TYPE = "Type";
    public static final String COLUMN_ID = "ID";

    public MessageRepoDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
}