package com.pipit.agc.agc.util;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;

import com.pipit.agc.agc.R;
import com.pipit.agc.agc.activity.AllinOneActivity;

import org.json.JSONArray;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * For math and stuff
 * Created by Eric on 12/14/2015.
 */
public class Util {

    public static boolean putStringIntoListIntoSharedPrefs(final Context context, final String key, String s){
        SharedPreferences prefs = context.getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_MULTI_PROCESS);
        List<String> strs = getListFromSharedPref(prefs, key);
        strs.add(s);
        return putListToSharedPref(prefs.edit(), key, strs);
    }


    public static boolean putListToSharedPref(Context context, final String key, final List<String> list) {
        SharedPreferences prefs = context.getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_MULTI_PROCESS);
        return putListToSharedPref(prefs.edit(), key, list);
    }

    public static boolean putListToSharedPref(final SharedPreferences.Editor edit, final String key, final List<String> list){
        try{
            JSONArray mJSONArray = new JSONArray(list);
            edit.putString(key, mJSONArray.toString());
            edit.commit();
            return true;
        } catch(Exception e){
            Log.d("Util", "putListToSharedPref failed with " + e.toString());
            return false;
        }
    }

    public static ArrayList<String> getListFromSharedPref(final SharedPreferences prefs, final String key){
        ArrayList<String> list = new ArrayList<String>();
        try{
            String k = prefs.getString(key, "");
            if (k.isEmpty()){
                return list;
            }
            JSONArray jsonArray = new JSONArray(k);
            if (jsonArray != null) {
                int len = jsonArray.length();
                for (int i=0;i<len;i++){
                    list.add(jsonArray.get(i).toString());
                }
            }
            return list;
        } catch (Exception e){
            Log.d("Util", "getListFromSharedPref Failed to convert into jsonArray " + e.toString());
            return list;
        }
    }

    public static List<String> dateListToStringList(List<Date> dates){
        List<String> ret = new ArrayList<String>();
        for(Date d : dates){
            String s = dateToString(d);
            if (s!=null){
                ret.add(s);
            }
        }
        return ret;
    }

    public static List<Date> stringListToDateList(List<String> strs){
        List<Date> dates = new ArrayList<Date>();
        for (String s : strs){
            Date d = stringToDate(s);
            if (s!=null){
                dates.add(d);
            }
        }
        return dates;
    }

    public static String dateToString(Date date){
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_NOW);
        return sdf.format(date);
    }

    public static Date stringToDate(String stringdate){
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_NOW);
        Date date = new Date();
        try {
            date = sdf.parse(stringdate);
        } catch (Exception e){
            Log.d("Util", "Failed to parse " + stringdate + " into date format");
            return null;
        }
        return date;
    }

    public static float getScreenHeightMinusStatusBar(Context context){
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        float screen_h = dm.heightPixels;

        int resId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resId > 0) {
            screen_h -= context.getResources().getDimensionPixelSize(resId);
        }

        TypedValue typedValue = new TypedValue();
        if(context.getTheme().resolveAttribute(android.R.attr.actionBarSize, typedValue, true)){
            /* EXPERIMENTAL HERE */
            //screen_h -= context.getResources().getDimensionPixelSize(typedValue.resourceId);
            screen_h -= 2*context.getResources().getDimensionPixelSize(typedValue.resourceId);
        }

        return screen_h;
    }

    public static List<Integer> listOfStringsToListOfInts(List<String> plannedDOWstrs){
        List<Integer> plannedDOW = new ArrayList<Integer>();
        for(String s : plannedDOWstrs){
            try{
                Integer dow = Integer.parseInt(s);
                plannedDOW.add(dow);
            } catch (Exception e){
                //Log.e(TAG, "Unable to parse planned GYM days, failed on " + s);
                //Log.e(TAG, "Received " + plannedDOWstrs.toArray());
                break;
            }
        }
        return plannedDOW;
    }

    public static List<Long> listOfStringsToListOfLongs(List<String> plannedDOWstrs){
        List<Long> plannedDOW = new ArrayList<Long>();
        for(String s : plannedDOWstrs){
            try{
                Long dow = Long.parseLong(s);
                plannedDOW.add(dow);
            } catch (Exception e){
                break;
            }
        }
        return plannedDOW;
    }

}
