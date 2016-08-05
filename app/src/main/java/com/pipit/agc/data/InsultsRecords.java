package com.pipit.agc.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.pipit.agc.model.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Used for accessing the offline messages repo
 * Created by Eric on 2/17/2016.
 */
public class InsultsRecords {
    private static final String TAG = "InsultsRecords";

    private SQLiteOpenHelper openHelper;
    private SQLiteDatabase database;
    private static InsultsRecords instance;

    private InsultsRecords(Context context) {
        this.openHelper = new InsultsDBHelper(context);
    }

    /**
     * Return a singleton instance of DatabaseAccess.
     *
     * @param context the Context
     * @return the instance of DabaseAccess
     */
    public static InsultsRecords getInstance(Context context) {
        if (instance == null) {
            instance = new InsultsRecords(context);
        }
        return instance;
    }

    /**
     * Open the database connection.
     */
    public void open() {
        this.database = openHelper.getWritableDatabase();
    }

    /**
     * Close the database connection.
     */
    public void close() {
        if (database != null) {
            this.database.close();
        }
    }

    /**
     * Gets a random message of given type and anger
     * @param type - See MessageRepoStructure for types and their meanings
     * @param maturity - One of three maturity levels
     * @return
     */
    public Message getRandomMessageWithParams(int type, int maturity){
        String query = "SELECT * FROM " + InsultsDBHelper.TABLE_MESSAGES
                + " WHERE " + InsultsDBHelper.COLUMN_TYPE + " = " + type;

        //Add maturity column flag
        switch (maturity){
            case InsultRecordsConstants.LOW_MATURITY:
                query += ", " + InsultsDBHelper.COLUMN_LOW_MAT + " = 1";
                break;
            case InsultRecordsConstants.MED_MATURITY:
                query += ", " + InsultsDBHelper.COLUMN_LOW_MAT + " = 1";
                break;
            case InsultRecordsConstants.HIGH_MATURITY:
                query += ", " + InsultsDBHelper.COLUMN_LOW_MAT + " = 1";
                break;
            default:
                break;
        }

        Cursor cursorc = database.rawQuery(query, null);
        Log.d("Eric", query);
                /*"SELECT * FROM " + InsultsDBHelper.TABLE_MESSAGES
                + " WHERE " + //InsultsDBHelper.COLUMN_ANGER + " = " + anger + ", " +
                InsultsDBHelper.COLUMN_TYPE + " = " + type, null);*/
        Random rn = new Random();
        int rand = rn.nextInt(cursorc.getCount());
        cursorc.moveToPosition(rand);
        Message msg = cursorToMessage(cursorc);
        return msg;
    }

    public Message getMessageById(long id){
        Cursor cursorc = database.rawQuery("SELECT * FROM " + InsultsDBHelper.TABLE_MESSAGES
                + " WHERE " + InsultsDBHelper.COLUMN_ID + " = " + id, null);
        cursorc.moveToFirst();
        Message msg = cursorToMessage(cursorc);
        return msg;
    }

    private Message cursorToMessage(Cursor cursor) {
        Message message = new Message();
        message.setId(cursor.getLong(0)); //This will be overriden when it gets dropped into our own db
        message.setRepoId(cursor.getLong(0));
        message.setHeader(cursor.getString(1));
        message.setBody(cursor.getString(2));
        return message;
    }

    /**
     * Given a message type, return ids of all messages of that type
     * @param type
     * @return
     */
    public List<Long> getListOfIDsForMessageType(int type){
        Cursor cursor = null;
        List<Long> k = new ArrayList<>();
        if (type>0 && type<= InsultRecordsConstants.REMINDER_HITYESTERDAY){
                cursor = database.rawQuery("SELECT * FROM " + InsultsDBHelper.TABLE_MESSAGES
                        + " WHERE " + InsultsDBHelper.COLUMN_TYPE + " = " + type, null);
        }
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Message msg = cursorToMessage(cursor);
            k.add(msg.getId());
            cursor.moveToNext();
        }
        cursor.close();
        return k;
    }

    public boolean isOpen(){
        return database.isOpen();
    }
}