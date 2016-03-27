package com.pipit.agc.agc.data;

/**
 * Contains structure of offline reminders repo
 * This is NOT for the database of messages on the client;
 * The offline repository contains all possible messages than
 * we can pull from.
 *
 * Created by Eric on 2/17/2016.
 */
public class MessageRepositoryStructure {
    //Message Types
    public static final int REMINDER_MISSEDYESTERDAY = 1;
    public static final int REMINDER_HITYESTERDAY = 2;
    public static final int DAYEND_MISSEDYESTERDAY = 3;
    public static final int DAYEND_HITYESTERDAY = 4;

    //Anger levels
    public static final int NOT_ANGRY = 0;
    public static final int KINDA_ANNOYED = 1;
    public static final int ANGRY = 2;
}

