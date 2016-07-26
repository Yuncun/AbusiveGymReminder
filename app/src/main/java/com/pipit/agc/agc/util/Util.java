package com.pipit.agc.agc.util;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * For math and stuff
 * Created by Eric on 12/14/2015.
 */
public class Util {

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
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_ONE);
        return sdf.format(date);
    }

    public static Date stringToDate(String stringdate){
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_ONE);
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

    /*Interface utility for retrieving a style*/
    public static int getStyledColor(Context context, int attrId)
    {
        TypedArray ta = getTypedArray(context, attrId);
        int color = ta.getColor(0, 0);
        ta.recycle();

        return color;
    }

    private static TypedArray getTypedArray(Context context, int attrId)
    {
        int[] attrs = new int[]{ attrId };
        return context.obtainStyledAttributes(attrs);
    }

}
