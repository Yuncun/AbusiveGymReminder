package com.pipit.agc.agc.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.pipit.agc.agc.R;
import com.pipit.agc.agc.fragment.DayOfWeekPickerFragment;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Eric on 2/6/2016.
 */
public class SharedPrefUtil {

    public static void updateMainLog(Context context, String s){
        SharedPreferences prefs = context.getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = prefs.edit();
        DateFormat dateFormat = new SimpleDateFormat("MM-dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        String mLastUpdateTime = dateFormat.format(cal.getTime());
        String logUpdate = prefs.getString("locationlist", "none") + "\n" + mLastUpdateTime + ": " + s ;
        editor.putString("locationlist", logUpdate);
        editor.commit();

    }
    public static void putString(Context context, String key, String val){
        SharedPreferences prefs = context.getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, val);
        editor.commit();
    }

    public static void putInt(Context context, String key, int val){
        SharedPreferences prefs = context.getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(key, val);
        editor.commit();
    }

    public static int getInt(Context context, String key, int defaultValue){
        SharedPreferences prefs = context.getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_MULTI_PROCESS);
        return prefs.getInt(key, defaultValue);
    }

    public static SharedPreferences.Editor putDouble(final SharedPreferences.Editor edit, final String key, final double value) {
        return edit.putLong(key, Double.doubleToRawLongBits(value));
    }

    public static double getDouble(final SharedPreferences prefs, final String key, final double defaultValue) {
        return Double.longBitsToDouble(prefs.getLong(key, Double.doubleToLongBits(defaultValue)));
    }

    public static void putLong(Context context, String key, long val){
        SharedPreferences prefs = context.getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(key, val);
        editor.commit();
    }

    public static long getLong(Context context, String key, long defaultlong){
        SharedPreferences prefs = context.getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_MULTI_PROCESS);
        return prefs.getLong(key, defaultlong);
    }

    public static void setFirstTime(Context context, boolean firsttime){
        SharedPreferences prefs = context.getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("firsttime", firsttime);
        editor.commit();
    }

    public static boolean getIsFirstTime(Context context){
        SharedPreferences prefs = context.getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_MULTI_PROCESS);
        return prefs.getBoolean("firsttime", true);
    }

    /**
     * Returns the time of last visit as a string expression.
     * @param context
     * @param sdf
     * @return
     */
    public static String getLastVisitString(Context context, SimpleDateFormat sdf){
        if (sdf==null){
            sdf = new SimpleDateFormat("MMM d h:mm a");
        }
        SharedPreferences prefs = context.getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_MULTI_PROCESS);
        Long timeinms =  prefs.getLong("last_visit_time", -1);
        if (timeinms<0){
            return null;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timeinms);
        String s = sdf.format(cal.getTime());
        return s;
    }

    public static void updateLastVisitTime(Context context, Long time){
        SharedPreferences prefs = context.getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = prefs.edit();
        editor.commit();
        editor.putLong("last_visit_time", time);
        editor.commit();
    }

    /**
     *
     * @param pos : DAY_OF_WEEK: 1 for Sunday and 7 for Saturday
     * @return True is that day is a gym day
     */
    public static boolean getGymStatusFromDayOfWeek(Context context, int pos){
        pos--; //Because the list of stored days is corresponds to 0-6 index and Calendar.DAY_OF_WEEK is 1-7
        String datestr = (Integer.toString(pos));

        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_MULTI_PROCESS);
        List<String> dates = (Util.getListFromSharedPref(prefs, Constants.SHAR_PREF_PLANNED_DAYS));
        if (dates.contains(datestr)) {
            return true;
        } else {
            return false;
        }
    }

}
