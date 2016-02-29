package com.pipit.agc.agc.data;

import com.pipit.agc.agc.Util;

import java.util.Date;

/**
 * Created by Eric on 1/23/2016.
 */
public class Message {
    private long id;
    private String body;
    private String header;
    private Date date;

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
}
