package com.pipit.agc.model;

import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.pipit.agc.util.Constants;
import com.pipit.agc.util.Converters;
import com.pipit.agc.util.SharedPrefUtil;
import com.pipit.agc.util.Util;

import org.joda.time.LocalDateTime;
import org.joda.time.Minutes;
import org.joda.time.Seconds;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * Created by Eric on 1/10/2016.
 */
public class DayRecord {
    public static String TAG = "DayRecord";
    public static final int maxSaneVisitTime = 60*8; //Minutes

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
                return Minutes.minutesBetween(in, LocalDateTime.now()).getMinutes();
            }
            return Minutes.minutesBetween(in, out).getMinutes();
        }

        public int getVisitTimeSeconds(){
            if (in == null) return 0;

            if (out == null){
                //last visit not completed
                return Seconds.secondsBetween(in, LocalDateTime.now()).getSeconds();
            }
            return Seconds.secondsBetween(in, out).getSeconds();
        }

        public String printin(){
            String ret = "";
            DateTimeFormatter dtfOut = DateTimeFormat.forPattern("h:mm a");

            if (in==null){
                return ret; //Should never happen
            }
            ret += dtfOut.print(in);
            return ret;
        }

        public String printout(){
            String ret = "";
            DateTimeFormatter dtfOut = DateTimeFormat.forPattern("h:mm a");
            if (out == null){
                ret += " ? ";
            }
            else{
                ret += dtfOut.print(out);
            }
            return ret;
        }

        public String print(){
            String ret = "";
            DateTimeFormatter dtfOut = DateTimeFormat.forPattern("h:mm a");

                if (in==null){
                    return ret; //Should never happen
                }
                ret += dtfOut.print(in);
                ret += " - ";
                if (out == null){
                    ret += " ? ";
                }
                else{
                    ret += dtfOut.print(out);
                }
            return ret;
        }

        /**
         * Returns
         * -1 if argument is less than this visit
         * 0 if argument is equal to this visit
         * 1 if argument is greater to this visit
         * @param v
         * @return
         */
        public int compareTo(Visit v){
            return in.compareTo(v.in); //Well this was easy
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

    public String getDateString(String format){
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

    // Will be used by the ArrayAdapter in the ListView
    @Override
    public String toString() {
        return comment;
    }

    //Returns true if dates are equal
    public boolean equalsDate(Date otherdate){
        //This seems like a stupid way of doing it, but it's actually an accepted method
        //TODO: Use Jodatime
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
        List<String> plannedDOWstrs = SharedPrefUtil.getListFromSharedPref(prefs, Constants.SHAR_PREF_PLANNED_DAYS);
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
        //If last visit is open
        if (visits.size()>0 && visits.get(visits.size()-1).out==null){
            //Get last visit
            Visit v = visits.get(visits.size()-1);
            if (ldt == null){
                //End it
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
     * Adds a visit, and merges overlapping subsets caused by the addition.
     *
     * The merging process is basically the overlapping subsets problem, which is a common interview
     * question.
     *
         1) Find location to insert new subset by comparing start times
            Note that the list is always in sorted order (visits naturally are added in chronological
            order anyway).

     The following steps are copied from geekstogeeks

         2) Traverse sorted intervals starting from first interval,
         do following for every interval.
         a) If current interval is not first interval and it
         overlaps with previous interval, then merge it with
         previous interval. Keep doing it while the interval
         overlaps with the previous one.
         b) Else add current interval to output list of intervals.
     * @param v
     */
    public boolean addVisitAndMergeSubsets(Visit v){
        if (v.in.compareTo(v.out)>=0){
            //"in" must be before "out"
            return false;
        }

        visits.add(v);
        Collections.sort(visits, visitComparator);

        visits = merge(visits);
        return true;
    }
    private Comparator<Visit> visitComparator = new Comparator<Visit>() {
        public int compare(Visit i1, Visit i2) {
            if (i1.in != i2.in)
                return i1.in.compareTo(i2.in);
            else
                return i1.out.compareTo(i2.out);
        }
    };

    private List<Visit> merge(List<Visit> intervals) {
        List<Visit> result = new ArrayList<>();

        if(intervals==null||intervals.size()==0)
            return result;

        Collections.sort(intervals, visitComparator);

        Visit pre = intervals.get(0);
        for(int i=0; i<intervals.size(); i++){
            Visit curr = intervals.get(i);
            if(curr.in.compareTo(pre.out)>0){
                result.add(pre);
                pre = curr;
            }else{
                Visit merged = new Visit();
                merged.in = pre.in;

                if (pre.out.compareTo(curr.out)>0){
                    merged.out = pre.out;
                }else{
                    merged.out = curr.out;
                }
                // Equivalent to : merged = new Visit(pre.in, Math.max(pre.in, curr.out));
               pre = merged;
            }
        }
        result.add(pre);
        return result;
    }

    /**
     * Remove visit by position in list
     * @param position
     */
    public void removeVisit(int position){
        visits.remove(position);
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

    public int getTotalVisitsMinutes(){
        int minutes = 0;
        for (Visit v : visits){
            minutes += v.getVisitTimeMinutes();
        }
        return minutes;
    }

    public int getTotalVisitsSeconds(){
        int secs = 0;
        for (Visit v : visits){
            secs += v.getVisitTimeSeconds();
        }
        return secs;
    }

    public int getTotalMinsLastVisit(){
        return visits.get(visits.size()-1).getVisitTimeMinutes();
    }

    /**
     * This function can be used to clean up dayrecords that never ended their last visit. This bug may have been fixed
     * but in any case this function will check the dayrecord and remove the last visit if it exceeds the max time.
     *
     * This function does NOT update the database; Updating database can be done with
     * datasource.updateLatestDayRecordVisits();

     *
     * @param maxTime
     */
    public boolean sanitizeLastVisitTime(int maxTime){
        if (visits.size()<1) return false;
        boolean f = false;
        if (visits.get(visits.size()-1).getVisitTimeMinutes() > maxTime){
            visits.remove(visits.size()-1);
            f = true;
        }
        return f;
    }

}
