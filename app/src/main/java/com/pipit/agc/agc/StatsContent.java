package com.pipit.agc.agc;

import android.content.Context;

import com.pipit.agc.agc.data.DayRecord;
import com.pipit.agc.agc.data.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatsContent {
    public static final List<Stat> ITEMS = new ArrayList<Stat>();
    public static List<DayRecord> _allDayRecords;

    public static final String CURRENT_STREAK = "currentstreak";
    public static final String LONGEST_STREAK = "longeststreak";

    public static final Map<String, Stat> STAT_MAP = new HashMap<String, Stat>();

    private static void addItem(Stat item) {
        ITEMS.add(item);
        STAT_MAP.put(item.id, item);
    }

    public static void updateAll(){
        updateCurrentStreak();
        updateLongestStreak();
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
}
