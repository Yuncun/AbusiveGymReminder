package com.pipit.agc.agc.model;

import android.util.Log;

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

    public Message(){
        timeCreated =  Calendar.getInstance();
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
            return getTimeFormat_HHmm();
        }
        DateFormat dateFormat = new SimpleDateFormat("M/dd");
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

    public void setRead(boolean b){
        read = b;
    }

    public boolean getRead(){
        return read;
    }
}
