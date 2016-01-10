package com.pipit.agc.agc;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Eric on 1/9/2016.
 */
public class DayPickerAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final String[] values;
    private final int daysActive;
    private final String[] DAYSOFWEEK = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday", "Monday", "Tuesday"};
    public int count;

    public DayPickerAdapter(Context context, String[] values, int daysActive) {
        super(context, R.layout.dayrowlayout, values);
        this.context = context;
        this.values = values;
        this.daysActive = daysActive;
        this.count = Math.max(values.length, 14);
        this.count+=daysActive;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.dayrowlayout, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.day_of_week);

        String dayOfWeek = DAYSOFWEEK[position%7];
        if (position==daysActive){
            dayOfWeek+=" (TODAY)";
        }
        textView.setText(dayOfWeek);

        return rowView;
    }

    @Override
    public int getCount() {
        return count;
    }
}