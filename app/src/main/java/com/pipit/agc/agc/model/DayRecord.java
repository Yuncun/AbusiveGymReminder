package com.pipit.agc.agc.model;

import android.content.SharedPreferences;

import com.pipit.agc.agc.util.Constants;
import com.pipit.agc.agc.util.Util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Eric on 1/10/2016.
 */
public class DayRecord {
    private long id;
    private String comment;
    private Date date;
    private boolean isGymDay;
    private boolean hasBeenToGym;

    public DayRecord(){
        //Defaults
        this.hasBeenToGym=false;
        this.date=new Date();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setDate(Date date){
        this.date=date;
    }

    public Date getDate(){
        return this.date;
    }

    public String getDateString(){
        return Util.dateToString(getDate());
    }

    // Will be used by the ArrayAdapter in the ListView
    @Override
    public String toString() {
        return comment;
    }

    public boolean compareToDate(Date otherdate){
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
        return fmt.format(this.date).equals(fmt.format(otherdate));
    }

    public boolean isGymDay() {
        return isGymDay;
    }

    public void setIsGymDay(boolean isGymDay) {
        this.isGymDay = isGymDay;
    }

    public boolean beenToGym() {
        return hasBeenToGym;
    }

    public void setHasBeenToGym(boolean hasBeenToGym) {
        this.hasBeenToGym = hasBeenToGym;
    }

    public boolean checkAndSetIfGymDay(final SharedPreferences prefs){
        List<String> plannedDOWstrs = Util.getListFromSharedPref(prefs, Constants.SHAR_PREF_PLANNED_DAYS);
        List<Integer> plannedDOW = Util.listOfStringsToListOfInts(plannedDOWstrs);
        if (this.date==null){
            return false;
        }else{
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            int dayOfWeek=cal.get(Calendar.DAY_OF_WEEK);
            if (plannedDOW.contains(dayOfWeek)){
                isGymDay=true;
                return true;
            }else{
                isGymDay=false;
                return false;
            }
        }
    }

}
