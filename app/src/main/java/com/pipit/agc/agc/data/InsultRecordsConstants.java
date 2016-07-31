package com.pipit.agc.agc.data;

/**
 * Contains structure of offline reminders repo
 * This is NOT for the database of messages on the client;
 * The offline repository contains all possible messages than
 * we can pull from.
 *
 * Created by Eric on 2/17/2016.
 */
public class InsultRecordsConstants {
    //Message Types
    public static final int REMINDER_MISSEDYESTERDAY = 1;
    public static final int REMINDER_HITYESTERDAY = 2;
    public static final int DAYEND_MISSEDYESTERDAY = 3;
    public static final int DAYEND_HITYESTERDAY = 4;

    //Anger levels
    public static final int NOT_ANGRY = 0;
    public static final int KINDA_ANNOYED = 1;
    public static final int ANGRY = 2;

    //Body Shaming Types
    public static final int NO_BODY_SHAME = 0;
    public static final int IM_TOO_FAT = 1;
    public static final int IM_TOO_SKINNY = 2;
    public static final int IM_JUST_RIGHT = 3;

    //Is targeted at a specific sex
    public static final int NOT_GENDERED = 0;
    public static final int USER_MALE = 1;
    public static final int USER_FEMALE = 2;

    //Maturity Level
    public static final int LOW_MATURITY = 0;
    public static final int MED_MATURITY = 1;
    public static final int HIGH_MATURITY = 2;

}

