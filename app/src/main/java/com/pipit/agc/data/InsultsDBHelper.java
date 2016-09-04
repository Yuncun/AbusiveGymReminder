package com.pipit.agc.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

/**
 * For the use of the offline message repo
 * Created by Eric on 2/17/2016.
 */
public class InsultsDBHelper extends SQLiteAssetHelper {
    public static final String DATABASE_NAME = "messagerepo.db";
    public static final String TABLE_MESSAGES = "Messages";
    public static final int DATABASE_VERSION = 14;

    //Column Names
    public static final String COLUMN_ANGER = "Anger";
    public static final String COLUMN_MSGBODY = "MessageBody";
    public static final String COLUMN_MSGHEADER = "MessageHeader";
    public static final String COLUMN_TYPE = "Type";
    public static final String COLUMN_ID = "ID";
    public static final String COLUMN_UNIQUE = "IsUnique";
    public static final String COLUMN_LOW_MAT = "Mat_low"; //0 or 1 to indicate if msg is appropriate for low maturity
    public static final String COLUMN_MED_MAT = "Mat_med";
    public static final String COLUMN_HI_MAT = "Mat_high";
    public static final String COLUMN_BODYTYPE = "BodyType";
    public static final String COLUMN_GENDER = "Gender";

    private String CREATE_TABLE_INSULTS = "create table "
            + TABLE_MESSAGES + "(" + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_MSGHEADER + " TEXT NOT NULL, "
            + COLUMN_MSGBODY + " TEXT NOT NULL, "
            + COLUMN_TYPE + " integer NOT NULL, "
            + COLUMN_ANGER + " integer, "
            + COLUMN_UNIQUE + " integer DEFAULT 0, "
            + COLUMN_LOW_MAT + " integer DEFAULT 0, "
            + COLUMN_MED_MAT + " integer, "
            + COLUMN_HI_MAT + " integer DEFAULT 1, "
            + COLUMN_BODYTYPE + " integer DEFAULT 0, "
            + COLUMN_GENDER + " integer DEFAULT 0 "
            + ");";

    public InsultsDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        setForcedUpgrade(DATABASE_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(InsultsDBHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        onCreate(db);
    }
}