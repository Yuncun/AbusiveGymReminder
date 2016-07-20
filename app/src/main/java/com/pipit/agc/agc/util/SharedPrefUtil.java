package com.pipit.agc.agc.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.gson.Gson;
import com.pipit.agc.agc.R;
import com.pipit.agc.agc.fragment.DayOfWeekPickerFragment;
import com.pipit.agc.agc.model.Gym;

import org.json.JSONArray;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
        List<String> dates = (getListFromSharedPref(prefs, Constants.SHAR_PREF_PLANNED_DAYS));
        if (dates.contains(datestr)) {
            return true;
        } else {
            return false;
        }
    }

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

    public static void putGeofenceList(Context context, List<Gym> gyms){
        Gson gson = new Gson();
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_MULTI_PROCESS);
        SharedPreferences.Editor editor = prefs.edit();
        List<String> jsonarr = new ArrayList<>();
        for (Gym gym : gyms){
            String gs = gson.toJson(gym);
            jsonarr.add(gs);
        }
        putListToSharedPref(editor, Constants.GYM_LIST, jsonarr);
    }

    public static List<Gym> getGeofenceList(Context context){
        List<Gym> gyms = new ArrayList<Gym>();
        Gson gson = new Gson();
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(Constants.SHARED_PREFS, Context.MODE_MULTI_PROCESS);
        List<String> gymstrings = getListFromSharedPref(prefs, Constants.GYM_LIST);
        for (String gs : gymstrings){
            Gym g = gson.fromJson(gs, Gym.class);
            gyms.add(g);
        }
        return gyms;
    }

    public static void addGeofenceToSharedPrefs(Context context, Gym gym){
        List<Gym> g = getGeofenceList(context);
        g.add(gym);
        putGeofenceList(context, g);

    }


}
