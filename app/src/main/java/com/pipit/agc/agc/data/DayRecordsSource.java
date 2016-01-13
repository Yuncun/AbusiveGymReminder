package com.pipit.agc.agc.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.sql.SQLException;
import java.util.ArrayList;
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
            MySQLiteHelper.COLUMN_DAYRECORDS};


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

    public DayRecord createDayRecord(String comment) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_DAYRECORDS, comment);
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

    public List<DayRecord> getAllDayRecords() {
        List<DayRecord> comments = new ArrayList<DayRecord>();

        Cursor cursor = mDatabase.query(MySQLiteHelper.TABLE_DAYRECORDS,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            DayRecord dayRecord = cursorToDayRecord(cursor);
            comments.add(dayRecord);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return comments;
    }

    private DayRecord cursorToDayRecord(Cursor cursor) {
        DayRecord dayRecord = new DayRecord();
        dayRecord.setId(cursor.getLong(0));
        dayRecord.setComment(cursor.getString(1));
        return dayRecord;
    }
}


