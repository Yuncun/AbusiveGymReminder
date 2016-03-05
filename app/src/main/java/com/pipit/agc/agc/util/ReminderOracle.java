package com.pipit.agc.agc.util;

import android.content.Context;
import android.util.Log;

import com.pipit.agc.agc.data.DBRecordsSource;
import com.pipit.agc.agc.data.MessageRepoAccess;
import com.pipit.agc.agc.data.MessageRepositoryStructure;
import com.pipit.agc.agc.model.Message;

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
        MessageRepoAccess databaseAccess = MessageRepoAccess.getInstance(context);
        databaseAccess.open();
        if (wentToGymYesterday) {
            Message msg = databaseAccess.getRandomMessageWithParams(MessageRepositoryStructure.REMINDER_HITYESTERDAY,
                    MessageRepositoryStructure.KINDA_ANNOYED);
            Log.d(TAG, "ReminderOracle leaving message " + msg + " header:" + msg.getHeader() + "body" + msg.getBody() );
            leaveMessage(msg);
        }else {
            Message msg = databaseAccess.getRandomMessageWithParams(MessageRepositoryStructure.REMINDER_MISSEDYESTERDAY,
                    MessageRepositoryStructure.KINDA_ANNOYED);
            leaveMessage(msg);
        }
        databaseAccess.close();

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
