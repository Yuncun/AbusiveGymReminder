package com.pipit.agc.agc;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.pipit.agc.agc.data.Message;
import com.pipit.agc.agc.data.MessageRepoDBHelper;

/**
 * Used for accessing the offline messages repo
 * Created by Eric on 2/17/2016.
 */
public class MessageRepoAccess {
    private SQLiteOpenHelper openHelper;
    private SQLiteDatabase database;
    private static MessageRepoAccess instance;

    private MessageRepoAccess(Context context) {
        this.openHelper = new MessageRepoDBHelper(context);
    }

    /**
     * Return a singleton instance of DatabaseAccess.
     *
     * @param context the Context
     * @return the instance of DabaseAccess
     */
    public static MessageRepoAccess getInstance(Context context) {
        if (instance == null) {
            instance = new MessageRepoAccess(context);
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
     * @param anger - Anger is represented on a scale of 0-4, denoting the harshness of message
     * @return
     */
    public Message getRandomMessageWithParams(int type, int anger){
        Cursor cursorc = database.rawQuery("SELECT * FROM " + MessageRepoDBHelper.TABLE_MESSAGES
                + " WHERE " + //MessageRepoDBHelper.COLUMN_ANGER + " = " + anger + ", " +
                MessageRepoDBHelper.COLUMN_TYPE + " = " + type, null);
        cursorc.moveToFirst();
        Message msg = cursorToMessage(cursorc);
        return msg;
    }

    private Message cursorToMessage(Cursor cursor) {
        Message message = new Message();
        message.setId(cursor.getLong(0));
        message.setHeader(cursor.getString(1));
        message.setBody(cursor.getString(2));
        return message;
    }
}