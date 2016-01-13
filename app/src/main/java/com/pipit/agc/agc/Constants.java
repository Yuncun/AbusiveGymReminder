package com.pipit.agc.agc;

/**
 * Created by Eric on 12/13/2015.
 */
public class Constants {
    /*Constants*/
    public static final String SHARED_PREFS = "AGR_SharedPrefs";
    public static final int REQUEST_PLACE_PICKER = 1;
    public static final String PROXIMITY_INTENT_ACTION = "com.pipit.agc.agc.action.PROXIMITY_ALERT";

    /*Settings*/
    public static int timeBetweenLocationChecks = 1000 * 60 * 10; //Milliseconds
    public static int DAY_RESET_HOUR = 0;
    public static int DAY_RESET_MINUTE = 0;

}
