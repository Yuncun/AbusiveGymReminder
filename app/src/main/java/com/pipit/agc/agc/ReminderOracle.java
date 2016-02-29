package com.pipit.agc.agc;

import android.content.Context;
import android.util.Log;

import com.pipit.agc.agc.data.DBRecordsSource;
import com.pipit.agc.agc.data.Message;

import java.util.Date;

/**
 * Suite of functions that delivers reminders based on gym behavior
 */
public class ReminderOracle {
    private static final String TAG = "ReminderOracle";

    /**
     * Creates a reminder
     */
    public static void doLeaveMessageBasedOnPerformance(boolean wentToGymYesterday, Context context){
        if (wentToGymYesterday){
            Log.d(TAG, "doLeaveMessageBasedOnPerformance - about to access MessageRepo");
            MessageRepoAccess databaseAccess = MessageRepoAccess.getInstance(context);
            databaseAccess.open();
            Message msg = databaseAccess.getRandomMessageWithParams(1, 1);
            databaseAccess.close();
            leaveMessage(msg);
        }else {
            Message m = new Message();
            m.setHeader("Placeholder");
            m.setBody("Placeholder in Oracle");
            leaveMessage(m);

        }
    }

    /**
     * Uses previous statistics to figure out what message to send to user
     */
    private static void oracle(){

    }

    private static void leaveMessage(Message msg) {
        DBRecordsSource datasource;
        //Updates Data
        datasource = DBRecordsSource.getInstance();
        datasource.openDatabase();
        datasource.createMessage(msg, new Date());
        DBRecordsSource.getInstance().closeDatabase();
    }

    private static void doStats(){
        DBRecordsSource datasource;
        //Updates Data
        datasource = DBRecordsSource.getInstance();
        datasource.openDatabase();
        StatsContent._allDayRecords = datasource.getAllDayRecords();
        DBRecordsSource.getInstance().closeDatabase();
        StatsContent.updateAll();

    }

}
