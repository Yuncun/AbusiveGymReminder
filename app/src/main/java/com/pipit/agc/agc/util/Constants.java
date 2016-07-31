package com.pipit.agc.agc.util;

/**
 * Created by Eric on 12/13/2015.
 */
public class Constants {
    /*Constants*/
    public static final String SHARED_PREFS = "AGR_SharedPrefs";
    public static final int REQUEST_PLACE_PICKER = 1;
    public static final String PROXIMITY_INTENT_ACTION = "com.pipit.agc.agc.action.PROXIMITY_ALERT";
    public static final String DATE_FORMAT_ONE = "yyyy-MM-dd";
    public static final String DATE_FORMAT_TWO = "EEE, MMM d, yyyy";
    public static final String MESSAGE_ID = "message_id";
    public static final double DEFAULT_COORDINATE = 0.0;

    /*Settings*/
    public static int timeBetweenLocationChecks = 1000 * 60 * 10; //Milliseconds
    public static int DAY_RESET_HOUR = 0;
    public static int DAY_RESET_MINUTE = 0;
    public static final int DEFAULT_RADIUS = 200;


    /*Shared PReference Keys*/
    public static final String SHAR_PREF_PLANNED_DAYS = "plannedDays"; //Remembers which of the seven days are gym days
    public static final String SHAR_PREF_EXCEPT_DAYS = "exceptionDays"; //Remembers all days that don't follow weekly cycle
    public static final String DEST_LAT = "lat";
    public static final String DEST_LNG = "lng";
    public static final String GEOFENCES_ADDED_KEY= "geofenceadded";
    public static final String TAKEN_MESSAGE_IDS = "taken_message_ids"; //repoids of messages we have received
    public static final String PREF_NOTIF_TIME = "prefnotificationtime";
    public static final String GYM_LIST = "gymlist";
    public static final String HIGHEST_PROXID = "highestproxid";
    public static final String MATURITY_LEVEL = "maturitylevel";
    //Todo: I kind of stopped doing this halfway through there may be others out there


    /*Fragment Names*/
    public static final String NEWSFEED_FRAG = "newsfeed_fragment";
    public static final String DAYOFWEEK_FRAG = "dayofweek_fragment";
    public static final String DAYPICKER_FRAG = "daypicker_fragment";
    public static final String LOCATION_FRAG = "location_fragment";
    public static final String LOGS_FRAG = "logs_fragment";
    public static final String PLACEPICKER_FRAG = "placepicker_fragment";
    public static final String STATS_FRAGMENT = "stats_fragment";

    /*Permissions requestcodes*/
    public static final int GRANTED_LOCATION_PERMISSIONS = 1;

    /*Some enums*/
    public static final int NOTIFTIME_MORNING = 0;
    public static final int NOTIFTIME_AFTERNOON = 1;
    public static final int NOTIFTIME_EVENING = 2;
    public static final int NOTIFTIME_YOLO = 3;
}
