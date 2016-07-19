package com.pipit.agc.agc.model;

import android.location.Location;

import com.pipit.agc.agc.util.Constants;

import java.io.Serializable;

/**
 * Created by Eric on 2/20/2016.
 */
public class Gym implements Serializable{
    public Location location;
    public String address;
    public String name;
    public int proxid;
    public boolean isEmpty;

    public Gym(){
        location=new Location("");
        location.setLatitude(Constants.DEFAULT_COORDINATE);
        location.setLongitude(Constants.DEFAULT_COORDINATE);
        proxid = 0;
        name = "";
        address = "No address";
        isEmpty = false;
    }

}
