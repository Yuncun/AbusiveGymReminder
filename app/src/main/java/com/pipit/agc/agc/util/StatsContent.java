package com.pipit.agc.agc.util;

import com.pipit.agc.agc.model.DayRecord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatsContent {
    public static  List<Stat> ITEMS = new ArrayList<Stat>();
    public static List<DayRecord> _allDayRecords;

    public static final String CURRENT_STREAK = "currentstreak";
    public static final String LONGEST_STREAK = "longeststreak";
    public static final String MISSED_GYMDAYS_WEEK = "missedgymdaysweek";
    public static final String DAYS_HIT_WEEK = "totalhitdaysweek";
    public static final String DAYS_MISSED_WEEK = "totalmisseddaysweek";
    public static final String REST_DAYS_WEEK = "totalrestdaysweek";

    public static final Map<String, Stat> STAT_MAP = new HashMap<String, Stat>();

    private static void addItem(Stat item) {
        ITEMS.add(item);
        STAT_MAP.put(item.id, item);
    }

    public static void updateAll(){
        ITEMS=new ArrayList<Stat>();
        updateCurrentStreak();
        updateLongestStreak();
        //updateLastSevenDaysStats();
    }

    private static Stat createDummyItem(int position) {
        return new Stat(String.valueOf(position), "Item " + position, makeDetails(position));
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }

    public static class Stat<T> {
        public final String id;
        public T content;
        public String details;

        public Stat(String id, T content, String details) {
            this.id = id;
            this.content = content;
            this.details = details;
        }

        public Stat(String id){
            this.id=id;
        }
        public void set(T t) { this.content = t; }
        public T get() { return content; }

        @Override
        public String toString() {
            return details;
        }
    }

    public static void updateCurrentStreak(){
        int count = 0;
        for (int i = _allDayRecords.size()-1; i>=0; i--){
            if (!_allDayRecords.get(i).beenToGym()){
                break;
            }
            count++;
        }
        Stat currentstreak = new Stat<Integer>(CURRENT_STREAK);
        currentstreak.content = new Integer(count);
        currentstreak.details = "Current Streak";
        STAT_MAP.put(currentstreak.id, currentstreak);
        ITEMS.add(currentstreak);
    }

    public static void updateLongestStreak(){
        int longest = 0;
        int curr = 0;
        for (int i = _allDayRecords.size()-1; i>=0; i--){
            if (!_allDayRecords.get(i).beenToGym()){
                curr=0;
            }else{
                curr++;
                if (curr>longest) longest=curr;
            }
        }
        Stat longeststreak = new Stat<Integer>(LONGEST_STREAK);
        longeststreak.content = new Integer(longest);
        longeststreak.details = "Longest Streak";
        STAT_MAP.put(longeststreak.id, longeststreak);
        ITEMS.add(longeststreak);
    }

    public static void updateLastSevenDaysStats(){
        int missedGymDay = 0;
        int hitrestDay = 0;
        int missTotalDays = 0;
        int hitTotalDays = 0;
        int restDaysTotal = 0;
        for (int i = 1 ; i < 8 ; i++){
            boolean wentToGym =  _allDayRecords.get(_allDayRecords.size()-i).beenToGym();
            boolean wasGymDay = _allDayRecords.get(_allDayRecords.size()-i).isGymDay();
            if (wentToGym){
                hitTotalDays++;
                if (!wasGymDay){
                    hitrestDay++;
                }
            }
            else{
                missTotalDays++;
                if (wasGymDay){
                    missedGymDay++;
                }
            }
            if (!wasGymDay) restDaysTotal++;

        }
        Stat hitdaysStat = new Stat(DAYS_HIT_WEEK, new Integer(hitTotalDays), "Times gone to the gym last week");
        STAT_MAP.put(hitdaysStat.id, hitdaysStat);
        ITEMS.add(hitdaysStat);
        Stat restdaysWeekStat = new Stat(REST_DAYS_WEEK, new Integer(restDaysTotal), "Rest Days last week");
        STAT_MAP.put(restdaysWeekStat.id, restdaysWeekStat);
        ITEMS.add(restdaysWeekStat);
    }
}
