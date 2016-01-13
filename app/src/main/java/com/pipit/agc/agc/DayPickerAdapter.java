package com.pipit.agc.agc;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pipit.agc.agc.data.DayRecord;

import java.util.List;

/**
 * Created by Eric on 1/9/2016.
 */
public class DayPickerAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final String[] values;
    private final List<DayRecord> allPreviousDays;
    private final String[] DAYSOFWEEK = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday", "Monday", "Tuesday"};
    public int count;

    public DayPickerAdapter(Context context, String[] values, List<DayRecord> allPreviousDays) {
        super(context, R.layout.dayrowlayout, values);
        this.context = context;
        this.values = values;
        this.count = Math.max(values.length, 14);
        this.count+=allPreviousDays.size();
        this.allPreviousDays = allPreviousDays;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.dayrowlayout, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.day_of_week);

        if (position<allPreviousDays.size()){
            //The Past
            String primaryText = allPreviousDays.get(position).getComment();
            String idText = Long.toString(allPreviousDays.get(position).getId());
            textView.setText(idText + " " + primaryText);
        }
        else{
            //The Future
            String dayOfWeek = DAYSOFWEEK[position%7];
            if (position==allPreviousDays.size()){
                //The Present
                dayOfWeek+=" (TODAY)";
            }
            textView.setText(dayOfWeek);
        }

        return rowView;
    }

    @Override
    public int getCount() {
        return count;
    }
}