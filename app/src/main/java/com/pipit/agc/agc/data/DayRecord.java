package com.pipit.agc.agc.data;

/**
 * Created by Eric on 1/10/2016.
 */
public class DayRecord {
    private long id;
    private String comment;


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

    // Will be used by the ArrayAdapter in the ListView
    @Override
    public String toString() {
        return comment;
    }

}
