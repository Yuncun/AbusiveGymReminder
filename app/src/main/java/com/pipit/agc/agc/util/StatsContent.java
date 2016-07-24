package com.pipit.agc.agc.util;

import com.pipit.agc.agc.data.MsgAndDayRecords;
import com.pipit.agc.agc.model.DayRecord;
import com.pipit.agc.agc.model.Message;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This singleton serves two purposes;
 *
 *  One, it keeps a local cache of dayRecords and messages, so we can limit the number of database queries for
 *  all records. Usually this is refreshed with the main activity.
 *
 *  Two, it saves a list of stats that can be accessed by ID.
 */
public class StatsContent {
    public List<Stat> ITEMS = new ArrayList<Stat>();
    public Map<String, Stat> STAT_MAP = new HashMap<String, Stat>();
    private List<DayRecord> _allDayRecords;
    private List<Message> _allMessages;

    public static final String CURRENT_STREAK = "currentstreak";
    public static final String LONGEST_STREAK = "longeststreak";
    public static final String MISSED_GYMDAYS_WEEK = "missedgymdaysweek";
    public static final String DAYS_HIT_WEEK = "totalhitdaysweek";
    public static final String DAYS_MISSED_WEEK = "totalmisseddaysweek";
    public static final String REST_DAYS_WEEK = "totalrestdaysweek";
    public static final String DAYS_PLANNED_WEEK = "totalplanneddaysweek";
    public static final String WEEK_OF_RECORD_STREAK = "weekofrecord";

    public class Stat {
        //Todo: Refactor as a generic type
        public final String id;
        public int content;
        public String details;

        public Stat(String id, int content, String details) {
            this.id = id;
            this.content = content;
            this.details = details;
        }

        public Stat(String id){
            this.id=id;
        }
        public void set(int t) { this.content = t; }
        public int get() { return content; }

        @Override
        public String toString() {
            return details;
        }
    }

    private static StatsContent singleton = new StatsContent( );

    /* A private Constructor prevents any other
     * class from instantiating.
     */
    private StatsContent(){ }

    /* Static 'instance' method */
    public static StatsContent getInstance( ) {
        return singleton;
    }

    private void addItem(Stat item) {
        ITEMS.add(item);
        STAT_MAP.put(item.id, item);
    }

    public synchronized void refreshDayRecords(){
        StatsContent stats = StatsContent.getInstance();
        MsgAndDayRecords datasource;
        datasource = MsgAndDayRecords.getInstance();
        datasource.openDatabase();
        stats._allDayRecords = datasource.getAllDayRecords();
        MsgAndDayRecords.getInstance().closeDatabase();
    }

    public synchronized void refreshMessageRecords(){
        StatsContent stats = StatsContent.getInstance();
        MsgAndDayRecords datasource;
        datasource = MsgAndDayRecords.getInstance();
        datasource.openDatabase();
        stats._allMessages = datasource.getAllMessages();
        MsgAndDayRecords.getInstance().closeDatabase();
    }

    public void calculateStats(){
        ITEMS=new ArrayList<Stat>();
        updateCurrentStreak();
        updateLongestStreak();
        updateLastSevenDaysStats();
    }

    private String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }

    public DayRecord getToday(boolean forceupdate){
        if (forceupdate){
            refreshDayRecords();
        }
        return _allDayRecords.get(_allDayRecords.size() - 1);
    }

    public DayRecord getYesterday(boolean forceupdate){
        if (forceupdate){
            refreshDayRecords();
        }
        if (_allDayRecords.size()<2){
            return null;
        }
        return _allDayRecords.get(_allDayRecords.size()-2);
    }

    public List<DayRecord> getAllDayRecords(boolean forceupdate){
        if (forceupdate){
            refreshDayRecords();
        }
        return _allDayRecords;
    }

    public List<Message> getAllMessages(boolean forceupdate){
        if (forceupdate){
            refreshMessageRecords();
        }
        return _allMessages;
    }

    public void updateCurrentStreak(){
        int count = 0;
        for (int i = _allDayRecords.size()-1; i>=0; i--){
            if (!_allDayRecords.get(i).beenToGym() && _allDayRecords.get(i).isGymDay()){
                break;
            }
            //Only hit days count towards streak, but missed rest days do not break streak.
            if (_allDayRecords.get(i).isGymDay() || _allDayRecords.get(i).beenToGym()){
                count++;
            }
        }
        Stat currentstreak = new Stat(CURRENT_STREAK);
        currentstreak.content = count;
        currentstreak.details = "Current Streak";
        STAT_MAP.put(currentstreak.id, currentstreak);
        ITEMS.add(currentstreak);
    }

    public void updateLongestStreak(){
        int longest = 0;
        int curr = 0;
        DayRecord tmpstartday = null;
        DayRecord tmpendday = null;
        DayRecord startDay = null;
        DayRecord endDay = null;

        for (int i = _allDayRecords.size()-1; i>=0; i--){
            if (!_allDayRecords.get(i).beenToGym() && _allDayRecords.get(i).isGymDay()){
                curr = 0;
            }else{
                if (_allDayRecords.get(i).isGymDay() || _allDayRecords.get(i).beenToGym()){
                    if (curr < 1){
                        tmpstartday = _allDayRecords.get(i);
                    }
                    curr++;
                    tmpendday = _allDayRecords.get(i);
                }
                if (curr>longest){
                    startDay = tmpstartday;
                    endDay = tmpendday;
                    longest=curr;
                }
            }
        }
        Stat longeststreak = new Stat(LONGEST_STREAK);
        longeststreak.content = new Integer(longest);
        longeststreak.details = "Longest Streak";
        STAT_MAP.put(longeststreak.id, longeststreak);
        ITEMS.add(longeststreak);

        if (endDay!=null && startDay!=null){
            Stat weekofstreak = new Stat(WEEK_OF_RECORD_STREAK);
            weekofstreak.details = endDay.getDateString();
            STAT_MAP.put(weekofstreak.id, weekofstreak);
            ITEMS.add(weekofstreak);
        }
    }

    /**
     * Calculates several stats from the last week of dayrecords
     */
    public void updateLastSevenDaysStats(){
        int missedGymDay = 0;
        int hitrestDay = 0;
        int missTotalDays = 0;
        int committedTotalDays = 0;
        int hitTotalDays = 0;
        int restDaysTotal = 0;
        for (int i = 1 ; i < 8 ; i++){
            if (_allDayRecords.size() < i){
                break;
            }
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
            if (wasGymDay) committedTotalDays++;

        }

        Stat hitdaysStat = new Stat(DAYS_HIT_WEEK, hitTotalDays, "Gym Visits");
        STAT_MAP.put(hitdaysStat.id, hitdaysStat);
        ITEMS.add(hitdaysStat);

        Stat restdaysWeekStat = new Stat(REST_DAYS_WEEK, new Integer(restDaysTotal), "Rest Days");
        STAT_MAP.put(restdaysWeekStat.id, restdaysWeekStat);
        ITEMS.add(restdaysWeekStat);

        Stat daysplanned = new Stat(DAYS_PLANNED_WEEK, new Integer(committedTotalDays), "Days Planned");
        STAT_MAP.put(daysplanned.id, daysplanned);
        ITEMS.add(daysplanned);

        Stat missedGymDayStat = new Stat(MISSED_GYMDAYS_WEEK, new Integer(missedGymDay), "Days Missed");
        STAT_MAP.put(missedGymDayStat.id, missedGymDayStat);
        ITEMS.add(missedGymDayStat);
    }
}
