package com.pipit.agc.agc;

import android.location.Location;

/**
 * Created by Eric on 2/20/2016.
 */
public class Gym {
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
