package com.pipit.agc.util;

/**
 * Created by Eric on 12/13/2015.
 */
public class Constants {

    /*Constants*/
    public static final String SHARED_PREFS = "AGR_SharedPrefs";
    public static final int REQUEST_PLACE_PICKER = 1;
    public static final String PROXIMITY_INTENT_ACTION = "com.pipit.agc.action.PROXIMITY_ALERT";
    public static final String DATE_FORMAT_ONE = "yyyy-MM-dd";
    public static final String DATE_FORMAT_TWO = "EEE, MMM d, yyyy";
    public static final String MESSAGE_ID = "message_id";
    public static final double DEFAULT_COORDINATE = 0.0;
    public static final String LANG_SPANISH = "es"; //language id according to ISO 639-1 standard
    public static final String LANG_ENGLISH = "en";
    public static final String SHOW_STATS_FLAG = "showstatsflag";

    /*Settings and Heuristics*/
    public static int timeBetweenLocationChecks = 1000 * 60 * 10; //Milliseconds
    public static int DAY_RESET_HOUR = 0;
    public static int DAY_RESET_MINUTE = 0;
    public static int DAYS_TO_ADD = 1; //When setting our alarm manager, we want to set
    public static final int DEFAULT_RADIUS = 200;
    public static final int GEOFENCE_MIN_RADIUS = 50;
    public static final int GEOFENCE_MAX_RADIUS = 400;
    public static final int MIN_TIME_BETWEEN_VISITS = 60; //in minutes


    /*Shared Preference Keys*/
    public static final String SHAR_PREF_PLANNED_DAYS = "plannedDays"; //Remembers which of the seven days are gym days
    public static final String SHAR_PREF_EXCEPT_DAYS = "exceptionDays"; //Remembers all days that don't follow weekly cycle
    public static final String SHAR_PREF_GYMRADIUS = "gymradius";
    public static final String DEST_LAT = "lat";
    public static final String DEST_LNG = "lng";
    public static final String GEOFENCES_ADDED_KEY= "geofenceadded";
    public static final String TAKEN_MESSAGE_IDS = "taken_message_ids"; //repoids of messages we have received
    public static final String PREF_NOTIF_TIME = "prefnotificationtime";
    public static final String GYM_LIST = "gymlist";
    public static final String HIGHEST_PROXID = "highestproxid";
    public static final String MATURITY_LEVEL = "maturitylevel";
    public static final String FLAG_WAKEUP_SHOW_NOTIF = "shownotification"; //Checked on phone wakeup to determine if notif is shown
    public static final String CONTENT_WAKEUP_SHOW_NOTIF = "wakeupnotifcontent"; //Contains JSON of message to show on wakeup
    public static final String PREF_SHOW_NOTIF_ON_GYMHITS = "showhitgymswitch"; //Option for the user to see "gym day registered" notifications
    public static final String PREF_SHOW_CHANGEPAST_JOKE = "showstupidjoke";
    public static final String PREF_GET_LAST_ENTER_TIME = "last_visit_time";
    public static final String PREF_GET_LAST_EXIT_TIME = "last_exit_time";

    //Warning - This list of prefs may not account for all existing sharedprefs used


    /*Fragment Names*/
    public static final String NEWSFEED_FRAG = "newsfeed_fragment";
    public static final String DAYOFWEEK_FRAG = "dayofweek_fragment";
    public static final String DAYPICKER_FRAG = "daypicker_fragment";
    public static final String LOCATION_FRAG = "location_fragment";
    public static final String LOGS_FRAG = "logs_fragment";
    public static final String PLACEPICKER_FRAG = "placepicker_fragment";
    public static final String STATS_FRAGMENT = "stats_fragment";

    /*Fragment order*/
    public static final int STATS_FRAG_POS = 0;
    public static final int NEWFEED_FRAG_POS = 1;
    public static final int DAYPICKER_FRAG_POS = 2;
    public static final int LOCATION_FRAG_POS = 3;

    /*Permissions requestcodes*/
    public static final int GRANTED_LOCATION_PERMISSIONS = 1;

    /*Some enums*/
    public static final int NOTIFTIME_ON_WAKEUP = 0;
    public static final int NOTIFTIME_MORNING = 1;
    public static final int NOTIFTIME_AFTERNOON = 2;
    public static final int NOTIFTIME_EVENING = 3;

    /*Universal constants*/
    public static final int MS_IN_A_MINUTE = 60000;
}
