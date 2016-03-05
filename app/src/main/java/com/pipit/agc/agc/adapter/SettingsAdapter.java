package com.pipit.agc.agc.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pipit.agc.agc.R;

/**
 * Created by Eric on 2/7/2016.
 */
public class SettingsAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final String[] values;

    public SettingsAdapter(Context context, String[] values) {
        super(context, -1, values);
        this.context = context;
        this.values = values;
    }
    @Override
    public int getCount(){
        return values.length;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.settingsrow, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.header);
        textView.setText(values[position]);
        return rowView;
    }
}
