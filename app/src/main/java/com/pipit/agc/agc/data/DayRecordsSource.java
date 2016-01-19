package com.pipit.agc.agc.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.pipit.agc.agc.Util;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * This singleton holds the database and allows threadsafe access to it
 * Here are some helpful functions:
 *
 *          DayRecordsSource datasource = DayRecordsSource.getInstance()
 *          datasource.openDatabase();
 *          List<DayRecord> values = datasource.getAllDayRecords();
 *          DayRecordsSource.getInstance().closeDatabase();
 *
 * Created by Eric on 1/10/2016.
 */
public class DayRecordsSource {
    private static int _count;
    private SQLiteDatabase mDatabase;
    private static MySQLiteHelper _databaseHelper;
    private static DayRecordsSource instance;
    private String[] allColumns = { MySQLiteHelper.COLUMN_ID,
            MySQLiteHelper.COLUMN_DAYRECORDS, MySQLiteHelper.COLUMN_DATE};


    public static synchronized void initializeInstance(MySQLiteHelper helper) {
        if (instance == null) {
            instance = new DayRecordsSource();
            _databaseHelper = helper;
        }
    }

    public static synchronized DayRecordsSource getInstance() {
        if (instance == null) {
            throw new IllegalStateException(DayRecordsSource.class.getSimpleName() +
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

    public DayRecord createDayRecord(String comment, Date date) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_DAYRECORDS, comment);
        values.put(MySQLiteHelper.COLUMN_DATE, Util.dateToString(date));
        long insertId = mDatabase.insert(MySQLiteHelper.TABLE_DAYRECORDS, null,
                values);
        Cursor cursor = mDatabase.query(MySQLiteHelper.TABLE_DAYRECORDS,
                allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        DayRecord newComment = cursorToDayRecord(cursor);
        cursor.close();
        return newComment;
    }

    public void deleteDayRecord(DayRecord comment) {
        long id = comment.getId();
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
                allColumns, null, null, null, null, null);

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

    private DayRecord cursorToDayRecord(Cursor cursor) {
        DayRecord dayRecord = new DayRecord();
        dayRecord.setId(cursor.getLong(0));
        dayRecord.setComment(cursor.getString(1));
        dayRecord.setDate(Util.stringToDate(cursor.getString(2)));
        return dayRecord;
    }
}


