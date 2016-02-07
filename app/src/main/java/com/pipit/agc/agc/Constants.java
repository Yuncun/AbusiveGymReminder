package com.pipit.agc.agc;

/**
 * Created by Eric on 12/13/2015.
 */
public class Constants {
    /*Constants*/
    public static final String SHARED_PREFS = "AGR_SharedPrefs";
    public static final int REQUEST_PLACE_PICKER = 1;
    public static final String PROXIMITY_INTENT_ACTION = "com.pipit.agc.agc.action.PROXIMITY_ALERT";
    public static final String DATE_FORMAT_NOW = "yyyy-MM-dd";
    public static final String MESSAGE_ID = "message_id";
    public static final String PROX_INTENT_FILTER = "com.pipit.agc.agc.ProximityReceiver";
    public static final double DEFAULT_COORDINATE = 0.0;

    /*Settings*/
    public static int timeBetweenLocationChecks = 1000 * 60 * 10; //Milliseconds
    public static int DAY_RESET_HOUR = 0;
    public static int DAY_RESET_MINUTE = 0;

    /*Shared PReference Keys*/
    public static final String SHAR_PREF_PLANNED_DAYS = "plannedDays"; //Remembers which of the seven days are gym days
    public static final String SHAR_PREF_EXCEPT_DAYS = "exceptionDays"; //Remembers all days that don't follow weekly cycle
    public static final String DEST_LAT = "lat";
    public static final String DEST_LNG = "lng";


    /*Fragment Names*/
    public static final String NEWSFEED_FRAG = "newsfeed_fragment";
    public static final String DAYOFWEEK_FRAG = "dayofweek_fragment";
    public static final String DAYPICKER_FRAG = "daypicker_fragment";
    public static final String LOGS_FRAG = "logs_fragment";
    public static final String PLACEPICKER_FRAG = "placepicker_fragment";
}
