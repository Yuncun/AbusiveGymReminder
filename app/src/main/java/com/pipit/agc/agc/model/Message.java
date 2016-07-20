package com.pipit.agc.agc.model;

import android.util.Log;

import com.google.gson.Gson;
import com.pipit.agc.agc.util.Util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Eric on 1/23/2016.
 */
public class Message {
    private long id;
    private String body;
    private String header;
    private Date date;
    private Calendar timeCreated;
    private boolean read;
    private int reason = NO_RECORD;
    private long repoid;

    public final static int NO_RECORD = -1;
    public final static int MISSED_YESTERDAY = 0;
    public final static int HIT_YESTERDAY = 1;
    public final static int HIT_TODAY = 2;
    public final static int WELCOME = 3;
    public final static int NEW_MSG = 4;

    public Message(){
        timeCreated =  Calendar.getInstance();
        repoid=0;
        id=0;
        read=false;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getHeader(){ return header; }

    public void setHeader(String header) { this.header = header ; }

    public String getBody() {
        return body;
    }

    public void setBody(String comment) {
        this.body = comment;
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

    public void setReason(int r){
        reason=r;
    }

    public int getReason(){
        return reason;
    }

    public void setRepoId(long id){ this.repoid=id; }

    public long getRepoId() { return this.repoid; }



    // Will be used by the ArrayAdapter in the ListView
    @Override
    public String toString() {
        return body;
    }

    /**
     *
     * @return "Today", "Yesterday"
     */
    public String getIntelligentDateString(){
        if (date == null ) return "No date avaiable";
        if (isSameDay(date, new Date())){
            return "Today"; //Today
            //return getTimeFormat_HHmm();
        }
        DateFormat dateFormat = new SimpleDateFormat("M/d/yyyy");
        return dateFormat.format(date);
    }

    public String getTimeFormat_HHmm(){
        DateFormat dateFormat = new SimpleDateFormat("h:mm a");
        Date t = new Date(timeCreated.getTimeInMillis());
        return dateFormat.format(t);
    }

    public boolean isSameDay(Date date1, Date date2 ){
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    public Calendar getTimeCreated(){
        if (timeCreated==null){
            timeCreated= Calendar.getInstance();
            timeCreated.setTime(date);
        }
        return timeCreated;
    }

    synchronized public void setRead(boolean b){
        read = b;
    }

    synchronized public boolean getRead(){
        return read;
    }

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static Message fromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, Message.class);
    }
}
