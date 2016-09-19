package com.pipit.agc.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.pipit.agc.util.Util;
import com.pipit.agc.model.DayRecord;
import com.pipit.agc.model.Message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * This singleton holds the database and allows threadsafe access to it
 * Here are some helpful functions:
 *
 *          MsgAndDayRecords datasource = MsgAndDayRecords.getInstance()
 *          datasource.openDatabase();
 *          List<DayRecord> values = datasource.getAllDayRecords();
 *          MsgAndDayRecords.getInstance().closeDatabase();
 *
 * Created by Eric on 1/10/2016.
 */
public class MsgAndDayRecords {
    private static final String TAG = "MsgAndDayRecords";
    private static int _count;
    private SQLiteDatabase mDatabase;
    private static MsgDBHelper _databaseHelper;
    private static MsgAndDayRecords instance;
    private String[] allColumnsDayRecords = {
            MsgDBHelper.COLUMN_ID,
            MsgDBHelper.COLUMN_DAYRECORDS,
            MsgDBHelper.COLUMN_DATE,
            MsgDBHelper.COLUMN_ISGYMDAY,
            MsgDBHelper.COLUMN_BEENTOGYM,
            MsgDBHelper.COLUMN_VISITS};

    private String[] allColumnsMessages = {
            MsgDBHelper.COLUMN_ID,
            MsgDBHelper.COLUMN_MESSAGE_HEADER,
            MsgDBHelper.COLUMN_MESSAGE_BODY,
            MsgDBHelper.COLUMN_DATE,
            MsgDBHelper.COLUMN_REASON,
            MsgDBHelper.COLUMN_REPO_ID,
            MsgDBHelper.COLUMN_READ};

    public static synchronized void initializeInstance(MsgDBHelper helper) {
        if (instance == null) {
            instance = new MsgAndDayRecords();
            _databaseHelper = helper;
        }
    }

    public static synchronized MsgAndDayRecords getInstance() {
        if (instance == null) {
            throw new IllegalStateException(MsgAndDayRecords.class.getSimpleName() +
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

    public boolean isOpen(){
        return mDatabase.isOpen();
    }

    /**DAY RECORDS STUFF**/
    public DayRecord createDayRecord(DayRecord day) {
        String comment=day.getComment();
        Date date = day.getDate();
        boolean isGymDay = day.isGymDay();
        boolean beenToGym = day.beenToGym();
        String servis = day.getSerializedVisitsList();

        ContentValues values = new ContentValues();
        values.put(MsgDBHelper.COLUMN_DAYRECORDS, comment);
        values.put(MsgDBHelper.COLUMN_DATE, Util.dateToString(date));
        values.put(MsgDBHelper.COLUMN_BEENTOGYM, (beenToGym) ? 1 : 0);
        values.put(MsgDBHelper.COLUMN_ISGYMDAY, (isGymDay) ? 1 : 0);

        if (servis==null){
            values.putNull(MsgDBHelper.COLUMN_VISITS);
        }else{

            values.put(MsgDBHelper.COLUMN_VISITS, servis);
        }
        long insertId = mDatabase.insert(MsgDBHelper.TABLE_DAYRECORDS, null,
                values);
        Cursor cursor = mDatabase.query(MsgDBHelper.TABLE_DAYRECORDS,
                allColumnsDayRecords, MsgDBHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        DayRecord newComment = cursorToDayRecord(cursor);
        cursor.close();
        return newComment;
    }

    public void deleteDayRecord(DayRecord day) {
        long id = day.getId();
        System.out.println("Comment deleted with id: " + id);
        mDatabase.delete(MsgDBHelper.TABLE_DAYRECORDS, MsgDBHelper.COLUMN_ID
                + " = " + id, null);
    }

    public void updateLatestDayRecordVisits(String s){

        String query = "UPDATE " + MsgDBHelper.TABLE_DAYRECORDS + " SET " + MsgDBHelper.COLUMN_VISITS + " = "
                + "?" + " WHERE " + MsgDBHelper.COLUMN_ID + " = (SELECT MAX(_id) FROM " + MsgDBHelper.TABLE_DAYRECORDS
                + ")";

        mDatabase.execSQL(query, new String[]{s});
    }

    public void updateDayRecordGymStats(DayRecord day){
        String query = "UPDATE " + MsgDBHelper.TABLE_DAYRECORDS + " SET " +
                MsgDBHelper.COLUMN_BEENTOGYM + " = \"" + ((day.beenToGym()) ? 1 : 0) +
                "\", " + MsgDBHelper.COLUMN_ISGYMDAY + " = \"" +  ((day.isGymDay()) ? 1 : 0) +
                "\" WHERE " +  MsgDBHelper.COLUMN_ID + " = " + day.getId()  ;
        Log.d("Eric", "Executed query " + query);
        mDatabase.execSQL(query);
    }

    public void updateLatestDayRecordBeenToGym(boolean beenToGymToday){
        String query = "UPDATE " + MsgDBHelper.TABLE_DAYRECORDS + " SET " + MsgDBHelper.COLUMN_BEENTOGYM + " = \""
                + ((beenToGymToday) ? 1 : 0) + "\" WHERE " + MsgDBHelper.COLUMN_ID + " = (SELECT MAX(_id) FROM " + MsgDBHelper.TABLE_DAYRECORDS
                + ")";
        mDatabase.execSQL(query);
    }

    public void updateLatestDayRecordIsGymDay(boolean isGymDay){
        String query = "UPDATE " + MsgDBHelper.TABLE_DAYRECORDS + " SET " + MsgDBHelper.COLUMN_ISGYMDAY + " = \""
                + ((isGymDay) ? 1 : 0) + "\" WHERE " + MsgDBHelper.COLUMN_ID + " = (SELECT MAX(_id) FROM " + MsgDBHelper.TABLE_DAYRECORDS
                + ")";
        mDatabase.execSQL(query);
    }

    public List<DayRecord> getAllDayRecords() {
        List<DayRecord> dayrecords = new ArrayList<DayRecord>();

        Cursor cursor = mDatabase.query(MsgDBHelper.TABLE_DAYRECORDS,
                allColumnsDayRecords, null, null, null, null, null);

        Cursor dbCursor = mDatabase.query(MsgDBHelper.TABLE_DAYRECORDS, null, null, null, null, null, null);
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
        Cursor cursor = mDatabase.query(MsgDBHelper.TABLE_DAYRECORDS, allColumnsDayRecords, null, null, null, null,
                MsgDBHelper.COLUMN_ID + " DESC", "1");
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

    public void deleteLastDayRecord(){
        deleteDayRecord(getLastDayRecord());
    }

    private DayRecord cursorToDayRecord(Cursor cursor) {
        DayRecord dayRecord = new DayRecord();
        dayRecord.setId(cursor.getLong(0));
        dayRecord.setComment(cursor.getString(1));
        dayRecord.setDate(Util.stringToDate(cursor.getString(2)));
        dayRecord.setIsGymDay(cursor.getInt(3) > 0);
        dayRecord.setHasBeenToGym(cursor.getInt(4) > 0);
        String s = cursor.getString(5);
        if (s != null && !s.isEmpty()){
            dayRecord.setVisitsFromString(s);
        }
        return dayRecord;
    }

    /** MESSAGES STUFF **/
    public Message createMessage(Message msg, Date date) {
        ContentValues values = new ContentValues();
        values.put(MsgDBHelper.COLUMN_MESSAGE_HEADER, msg.getHeader());
        values.put(MsgDBHelper.COLUMN_MESSAGE_BODY, msg.getBody());
        values.put(MsgDBHelper.COLUMN_DATE, Util.dateToString(date));
        values.put(MsgDBHelper.COLUMN_REASON, msg.getReason());
        values.put(MsgDBHelper.COLUMN_REPO_ID, msg.getRepoId());
        values.put(MsgDBHelper.COLUMN_READ, (msg.getRead()) ? 1 : 0);
        long insertId = mDatabase.insert(MsgDBHelper.TABLE_MESSAGES, null,
                values);
        Cursor cursor = mDatabase.query(MsgDBHelper.TABLE_MESSAGES,
                allColumnsMessages, MsgDBHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        Message newComment = cursorToMessage(cursor);
        cursor.close();
        return newComment;
    }

    public void deleteMessage(Message comment) {
        long id = comment.getId();
        System.out.println("Comment deleted with id: " + id);
        mDatabase.delete(MsgDBHelper.TABLE_MESSAGES, MsgDBHelper.COLUMN_ID
                + " = " + id, null);
    }

    public void deleteAllMessages(){
        mDatabase.execSQL("delete from " + MsgDBHelper.TABLE_MESSAGES);
    }

    public void updateLatestMessage(String comment){
        String query = "UPDATE " + MsgDBHelper.TABLE_MESSAGES + " SET " + MsgDBHelper.COLUMN_MESSAGE_BODY + " = \""
                + comment + "\" WHERE " + MsgDBHelper.COLUMN_ID + " = (SELECT MAX(_id) FROM " + MsgDBHelper.TABLE_MESSAGES
                + ")";
        mDatabase.execSQL(query);
    }

    public void markMessageRead(long id, boolean read){
        int readint = (read) ? 1 : 0;
        String query = "UPDATE " + MsgDBHelper.TABLE_MESSAGES + " SET " + MsgDBHelper.COLUMN_READ + " = "
                + readint + " WHERE " + MsgDBHelper.COLUMN_ID + " = " + id;
        mDatabase.execSQL(query);
    }

    public List<Message> getAllMessages() {
        List<Message> messages = new ArrayList<Message>();

        Cursor cursor = mDatabase.query(MsgDBHelper.TABLE_MESSAGES,
                allColumnsMessages, null, null, null, null, null);

        Cursor dbCursor = mDatabase.query(MsgDBHelper.TABLE_MESSAGES, null, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Message msg = cursorToMessage(cursor);
            messages.add(msg);
            cursor.moveToNext();
        }
        cursor.close();
        return messages;
    }

    public List<Message> getMessagesByRange(int a, int b){
        List<Message> messages = new ArrayList<Message>();
        Cursor cursor = mDatabase.rawQuery("SELECT * FROM " + MsgDBHelper.TABLE_MESSAGES
                + " WHERE " + MsgDBHelper.COLUMN_ID + " LIMIT " + a + ", " + b, null);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Message msg = cursorToMessage(cursor);
            messages.add(msg);
            cursor.moveToNext();
        }
        cursor.close();
        return messages;
    }


    public Message getMessageById(String id){
        Cursor cursorc = mDatabase.rawQuery("SELECT * FROM " + MsgDBHelper.TABLE_MESSAGES
                + " WHERE " + MsgDBHelper.COLUMN_ID + " = " + id, null);
        cursorc.moveToFirst();
        Message msg = cursorToMessage(cursorc);
        return msg;
    }

    private Message cursorToMessage(Cursor cursor) {
        Message message = new Message();
        message.setId(cursor.getLong(0));
        message.setRepoId(cursor.getLong(5));
        message.setHeader(cursor.getString(1));
        message.setBody(cursor.getString(2));
        message.setDate(Util.stringToDate(cursor.getString(3)));
        message.setReason(cursor.getInt(4));
        message.setRead(cursor.getInt(6)!=0);
        //Log.d(TAG, "in cursorToMessage, id = " + message.getId() + " message = " + message.getHeader() + " body = "
        //        + message.getBody() + " date = " + message.getDateString());
        return message;
    }

    //General stuff

    //Used for debugging
    public String[] getColumnNames(String tablename){
        Cursor dbCursor = mDatabase.query(tablename, null, null, null, null, null, null);
        String[] columnNames = dbCursor.getColumnNames();
        Log.d(TAG, "Column names for table " + tablename + " " + Arrays.toString(columnNames));
        return columnNames;
    }

    public int getSizeOfMessagesTable(){
        Cursor cursorc = mDatabase.rawQuery("SELECT * FROM " + MsgDBHelper.TABLE_MESSAGES, null);
        boolean isOk = cursorc.moveToFirst();
        return cursorc.getInt(0);
    }

}


