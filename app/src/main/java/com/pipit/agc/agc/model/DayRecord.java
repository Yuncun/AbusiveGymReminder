package com.pipit.agc.agc.model;

import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.pipit.agc.agc.util.Constants;
import com.pipit.agc.agc.util.Converters;
import com.pipit.agc.agc.util.Util;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.Minutes;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Eric on 1/10/2016.
 */
public class DayRecord {
    public static String TAG = "DayRecord";

    public static class Visit implements Serializable{
        public LocalDateTime in;
        public LocalDateTime out;

        public Visit(){}

        public Visit(LocalDateTime in, LocalDateTime out){
            this.in = in;
            this.out = out;
        }

        public int getVisitTimeMinutes(){
            if (in == null) return 0;

            if (out == null){
                out = LocalDateTime.now();
            }

            return Minutes.minutesBetween(in, out).getMinutes();
        }
    }

    private long id;
    private String comment;
    private Date date;
    private boolean isGymDay;
    private boolean hasBeenToGym;
    private List<Visit> visits;

    public DayRecord(){
        //Defaults
        this.hasBeenToGym=false;
        this.date=new Date();
        this.visits = new ArrayList<Visit>();
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

    public String getSerializedVisitsList(){
        if (visits==null || visits.size() == 0){
            return null;
        }
        final Gson gson = Converters.registerLocalDateTime(new GsonBuilder()).create();
        Type listOfTestObject = new TypeToken<List<Visit>>(){}.getType();
        String s = gson.toJson(visits, listOfTestObject);
        return s;
    }

    public void setVisitsFromString(String blob){
        if (blob == null || blob.isEmpty()) return;
        final Gson gson = Converters.registerLocalDateTime(new GsonBuilder()).create();
        Type listOfTestObject = new TypeToken<List<Visit>>(){}.getType();
        try{
            visits = gson.fromJson(blob, listOfTestObject);
        }catch(Exception e){
            Log.e(TAG, "Could not deserialize visit list " + e.toString() + " \n " + blob);
        }
    }

    public List<Visit> getVisits() {
        return visits;
    }

    public void setVisits(List<Visit> visits) {
        this.visits = visits;
    }

    public void startCurrentVisit(){
        startCurrentVisit(null);
    }

    public boolean endCurrentVisit(){
        return endCurrentVisit(null);
    }

    public void startCurrentVisit(LocalDateTime ldt){
        //Check if there isn't already a visit opened
        if (visits==null){
            visits = new ArrayList<Visit>();
        }
        if (!visits.isEmpty() && visits.get(visits.size()-1).out == null){
            //If we try to start a new session while the prev hasn't finished, then something went wrong.
            //We will remove this entry.
            visits.remove(visits.size()-1);
        }

        Visit v = new Visit();
        if (ldt == null){
            v.in = new LocalDateTime(); //now
        }else{
            v.in = ldt;
        }
        v.out = null; //Haven't finished yet
        this.visits.add(v);
    }

    public boolean endCurrentVisit(LocalDateTime ldt){
        if (visits.size()>0 && visits.get(visits.size()-1).out==null){
            Visit v = visits.get(visits.size()-1);
            if (ldt == null){
                v.out = new LocalDateTime();
            }
            else{
                v.out = ldt;
            }
            return true;
        }
        //If it returns false, it means there is nothing to close; nothing is done
        return false;
    }

    public void addVisit(LocalDateTime in, LocalDateTime out){
        if (visits!=null){
            visits.add(new Visit(in, out));
        }
    }

    /**
     * We leave it to the user to make sure this DayRecord is up-to-date
     * @return
     */
    public boolean isCurrentlyVisiting(){
        if (visits.size()<1){
            return false;
        }
        if (visits.get(visits.size()-1).in != null && visits.get(visits.size()-1).out == null){
            return true;
        }
        return false;
    }

    public String printVisits(){
        if (visits == null || visits.isEmpty()){
            return "No Gym Visits";
        }
        String ret = "";
        //DateTimeFormatter dtf = DateTimeFormat.forPattern("H:mm");
        //DateTime jodatime = dtf.parseDateTime(dateTime);
        DateTimeFormatter dtfOut = DateTimeFormat.forPattern("h:mm a");

        for (Visit v : visits){
            if (v.in==null){
                continue; //Hopefully this never happens
            }
            ret += dtfOut.print(v.in);
            ret += " - ";
            if (v.out == null){
                ret += " ? ";
            }
            else{
                ret += dtfOut.print(v.out);
            }
            ret += "\n";
        }
        return ret;
    }

    public int calculateTotalVisitTime(){
        int minutes = 0;
        for (Visit v : visits){
            minutes += v.getVisitTimeMinutes();
        }
        return minutes;
    }

}
