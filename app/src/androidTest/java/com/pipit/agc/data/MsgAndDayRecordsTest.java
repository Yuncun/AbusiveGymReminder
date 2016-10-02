package com.pipit.agc.data;

import android.test.AndroidTestCase;
import android.test.RenamingDelegatingContext;
import android.util.Log;

import com.pipit.agc.activity.AllinOneActivity;
import com.pipit.agc.model.DayRecord;
import com.pipit.agc.util.StatsContent;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;

import java.util.Calendar;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * Basic tests for basic DB operations
 */
public class MsgAndDayRecordsTest extends AndroidTestCase {
    private static final String TAG = "DBTest";
    MsgAndDayRecords db;
    RenamingDelegatingContext rnContext;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        rnContext = new RenamingDelegatingContext(getContext(), "test_");
        MsgAndDayRecords.initializeInstance(new MsgDBHelper(rnContext));
        db = MsgAndDayRecords.getInstance();
    }

    @Override
    public void tearDown() throws Exception {
        db.closeDatabase();
        super.tearDown();
    }

    public void testAddDay(){
        DayRecord testDay = new DayRecord();
        testDay.setIsGymDay(true);
        testDay.setHasBeenToGym(true);
        testDay.setComment("Test");
        db.openDatabase();
        db.createDayRecord(testDay);
        db.closeDatabase();
        DayRecord retrievedDay = StatsContent.getInstance().getToday(true);
        assertTrue(retrievedDay.beenToGym());
        assertTrue(retrievedDay.isGymDay());
        assertTrue(retrievedDay.getComment().equals("Test"));

        DayRecord testDayTwo = new DayRecord();
        testDayTwo.setIsGymDay(false);
        testDayTwo.setHasBeenToGym(false);
        testDayTwo.setComment("testtwo");
        db.openDatabase();
        db.createDayRecord(testDayTwo);
        db.closeDatabase();

        //Test retrieval functions
        retrievedDay = StatsContent.getInstance().getToday(false);
        assertTrue(retrievedDay.getComment().equals("Test"));
        long testdayone_id = retrievedDay.getId();
        retrievedDay = StatsContent.getInstance().getToday(true);
        assertTrue(retrievedDay.getComment().equals("testtwo"));

        //Test delete
        db.openDatabase();
        db.deleteDayRecord(retrievedDay);
        DayRecord newlatestday = db.getLastDayRecord();
        assertTrue(newlatestday.getId() == testdayone_id); //Removed testday2, so id should be testday1
        db.deleteDayRecord(testDay);
        db.closeDatabase();
    }

    public void testUpdateDate(){
        //Set an initial test date
        DayRecord testDay = new DayRecord();
        testDay.setIsGymDay(true);
        testDay.setHasBeenToGym(true);
        testDay.setComment("UpdateTest");
        db.openDatabase();
        db.createDayRecord(testDay);
        long testDay_id = db.getLastDayRecord().getId();
        db.closeDatabase();

        //Test updateDate does nothing if last day is today
        AllinOneActivity.updateDate(rnContext);
        db.openDatabase();
        long newDay_id = db.getLastDayRecord().getId();
        assertTrue(testDay_id == newDay_id);

        //Test updateDate did something if we were one day behind
        DayRecord testDayTwo = new DayRecord();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -2);
        testDayTwo.setDate(cal.getTime());
        testDayTwo.setHasBeenToGym(false);
        testDayTwo.setIsGymDay(false);
        testDayTwo.setComment("UpdateTest");
        Log.d(TAG, "testday: " + testDayTwo.getDateString() + " today:" + db.getLastDayRecord().getDateString());
        db.createDayRecord(testDayTwo);
        Log.d(TAG, "testday: " + testDayTwo.getDateString() + " today:" + db.getLastDayRecord().getDateString());
        db.closeDatabase();
        //Do the update - We expect this to add a day behind our backs
        AllinOneActivity.updateDate(rnContext);
        db.openDatabase();
        newDay_id = db.getLastDayRecord().getId();

        assertTrue(testDay_id != newDay_id);

        db.deleteLastDayRecord();
        db.deleteLastDayRecord();
        db.deleteLastDayRecord();
        db.closeDatabase();
    }

}