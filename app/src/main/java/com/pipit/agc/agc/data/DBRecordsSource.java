package com.pipit.agc.agc.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.pipit.agc.agc.Util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This singleton holds the database and allows threadsafe access to it
 * Here are some helpful functions:
 *
 *          DBRecordsSource datasource = DBRecordsSource.getInstance()
 *          datasource.openDatabase();
 *          List<DayRecord> values = datasource.getAllDayRecords();
 *          DBRecordsSource.getInstance().closeDatabase();
 *
 * Created by Eric on 1/10/2016.
 */
public class DBRecordsSource {
    private static final String TAG = "DBRecordsSource";
    private static int _count;
    private SQLiteDatabase mDatabase;
    private static MySQLiteHelper _databaseHelper;
    private static DBRecordsSource instance;
    private String[] allColumnsDayRecords = { MySQLiteHelper.COLUMN_ID,
            MySQLiteHelper.COLUMN_DAYRECORDS, MySQLiteHelper.COLUMN_DATE};
    private String[] allColumnsMessages = { MySQLiteHelper.COLUMN_ID,
            MySQLiteHelper.COLUMN_MESSAGES, MySQLiteHelper.COLUMN_DATE};

    public static synchronized void initializeInstance(MySQLiteHelper helper) {
        if (instance == null) {
            instance = new DBRecordsSource();
            _databaseHelper = helper;
        }
    }

    public static synchronized DBRecordsSource getInstance() {
        if (instance == null) {
            throw new IllegalStateException(DBRecordsSource.class.getSimpleName() +
                    " is not initialized, cannot get instance.");
        }

        return instance;
    }

    public synchronized SQLiteDatabase openDatabase() {
        _count++;
        if(_count == 1) {
            mDatabase = _databaseHelper.getWritableDatabase();
        }
        return mDatabase;
    }

    public synchronized void closeDatabase() {
        _count--;
        if(_count == 0) {
            mDatabase.close();

        }
    }

    /**DAY RECORDS STUFF**/
    public DayRecord createDayRecord(String comment, Date date) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_DAYRECORDS, comment);
        values.put(MySQLiteHelper.COLUMN_DATE, Util.dateToString(date));
        long insertId = mDatabase.insert(MySQLiteHelper.TABLE_DAYRECORDS, null,
                values);
        Cursor cursor = mDatabase.query(MySQLiteHelper.TABLE_DAYRECORDS,
                allColumnsDayRecords, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        DayRecord newComment = cursorToDayRecord(cursor);
        cursor.close();
        return newComment;
    }

    public void deleteDayRecord(DayRecord day) {
        long id = day.getId();
        System.out.println("Comment deleted with id: " + id);
        mDatabase.delete(MySQLiteHelper.TABLE_DAYRECORDS, MySQLiteHelper.COLUMN_ID
                + " = " + id, null);
    }

    public void updateLatestDayRecord(String comment){
        String query = "UPDATE " + MySQLiteHelper.TABLE_DAYRECORDS + " SET " + MySQLiteHelper.COLUMN_DAYRECORDS + " = \""
                + comment + "\" WHERE " + MySQLiteHelper.COLUMN_ID + " = (SELECT MAX(_id) FROM " + MySQLiteHelper.TABLE_DAYRECORDS
                + ")";
        mDatabase.execSQL(query);
    }

    public List<DayRecord> getAllDayRecords() {
        List<DayRecord> dayrecords = new ArrayList<DayRecord>();

        Cursor cursor = mDatabase.query(MySQLiteHelper.TABLE_DAYRECORDS,
                allColumnsDayRecords, null, null, null, null, null);

        Cursor dbCursor = mDatabase.query(MySQLiteHelper.TABLE_DAYRECORDS, null, null, null, null, null, null);
        String[] columnNames = dbCursor.getColumnNames();

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            DayRecord dayRecord = cursorToDayRecord(cursor);
            dayrecords.add(dayRecord);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return dayrecords;
    }

    public DayRecord getLastDayRecord(){
        DayRecord lastDayRecord = new DayRecord();
        /*String command = "SELECT * FROM " + MySQLiteHelper.TABLE_DAYRECORDS + ""
                + "WHERE " +  MySQLiteHelper.COLUMN_ID + " = (SELECT MAX(" + MySQLiteHelper.COLUMN_ID
                + ") FROM " +  MySQLiteHelper.TABLE_DAYRECORDS + ");"; */
        Cursor cursor = mDatabase.query(MySQLiteHelper.TABLE_DAYRECORDS, allColumnsDayRecords, null, null, null, null,
                MySQLiteHelper.COLUMN_ID +" DESC", "1");
        if(cursor!=null && cursor.getCount()>0) {
            cursor.moveToFirst();
            lastDayRecord = cursorToDayRecord(cursor);
            //Log.d(TAG, "LastDayRecord " + lastDayRecord + " and cursor had " + cursor.getCount() + " elements");
        }else{
            Log.d(TAG, "LastDayRecord query found nothing, returning null");
            return null;
        }
        return lastDayRecord;

    }

    private DayRecord cursorToDayRecord(Cursor cursor) {
        DayRecord dayRecord = new DayRecord();
        dayRecord.setId(cursor.getLong(0));
        dayRecord.setComment(cursor.getString(1));
        dayRecord.setDate(Util.stringToDate(cursor.getString(2)));
        return dayRecord;
    }

    /** MESSAGES STUFF **/
    public Message createMessage(String comment, Date date) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_MESSAGES, comment);
        values.put(MySQLiteHelper.COLUMN_DATE, Util.dateToString(date));
        long insertId = mDatabase.insert(MySQLiteHelper.TABLE_MESSAGES, null,
                values);
        Cursor cursor = mDatabase.query(MySQLiteHelper.TABLE_MESSAGES,
                allColumnsMessages, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        Message newComment = cursorToMessage(cursor);
        cursor.close();
        return newComment;
    }

    public void deleteMessage(Message comment) {
        long id = comment.getId();
        System.out.println("Comment deleted with id: " + id);
        mDatabase.delete(MySQLiteHelper.TABLE_MESSAGES, MySQLiteHelper.COLUMN_ID
                + " = " + id, null);
    }

    public void updateLatestMessage(String comment){
        String query = "UPDATE " + MySQLiteHelper.TABLE_MESSAGES + " SET " + MySQLiteHelper.COLUMN_MESSAGES + " = \""
                + comment + "\" WHERE " + MySQLiteHelper.COLUMN_ID + " = (SELECT MAX(_id) FROM " + MySQLiteHelper.TABLE_MESSAGES
                + ")";
        mDatabase.execSQL(query);
    }

    public List<Message> getAllMessages() {
        List<Message> messages = new ArrayList<Message>();

        Cursor cursor = mDatabase.query(MySQLiteHelper.TABLE_MESSAGES,
                allColumnsMessages, null, null, null, null, null);

        Cursor dbCursor = mDatabase.query(MySQLiteHelper.TABLE_MESSAGES, null, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Message dayRecord = cursorToMessage(cursor);
            messages.add(dayRecord);
            cursor.moveToNext();
        }
        cursor.close();
        return messages;
    }

    private Message cursorToMessage(Cursor cursor) {
        Message message = new Message();
        message.setId(cursor.getLong(0));
        message.setComment(cursor.getString(1));
        message.setDate(Util.stringToDate(cursor.getString(2)));
        return message;
    }

}


