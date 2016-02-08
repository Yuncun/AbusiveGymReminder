package com.pipit.agc.agc;

import android.content.Context;
import android.graphics.Paint;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.pipit.agc.agc.data.DBRecordsSource;
import com.pipit.agc.agc.data.DayRecord;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

/**
 * Created by Eric on 2/3/2016.
 */
public class DayOfWeekAdapter extends ArrayAdapter<String> {
    private final Context context;
    public int count = 7;
    private static int _screenheight;
    public static String TAG = "DayPickerAdapter";
    private HashSet<Integer> weeklySchedule; //Contains 0-7 days that are gym days

    public DayOfWeekAdapter(Context context, HashSet<Integer> weeklySchedule) {
        super(context, R.layout.dayofweek_layout);
        this.context = context;
        this._screenheight=-1;
        this.weeklySchedule = weeklySchedule;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.dayofweek_layout, parent, false);
        TextView dayOfMonthTV = (TextView) rowView.findViewById(R.id.txt);
        TextView commentTV = (TextView) rowView.findViewById(R.id.comment);

        commentTV.setText(getDayOfWeekText(position + 1));
        dayOfMonthTV.setText(getDayMessage(position, rowView));

        /*Set listview height to show 7 days*/
        if (_screenheight<1){
            _screenheight = Math.round(Util.getScreenHeightMinusStatusBar(getContext()));
        }
        int txtheight = (int) (_screenheight / 7);
        commentTV.setHeight(txtheight);
        //refitText(commentTV, txtheight);

        return rowView;
    }

    @Override
    public int getCount() {
        return count;
    }

    /* Re size the font so the specified text fits in the text box
    * assuming the text box is the specified width.
    *
    *  Written by StackOverflow user speedplane
    *  MIT license
    */
    private void refitText(TextView txtv, int targetheight)
    {
        if (targetheight <= 0)
            return;
        Paint mTestPaint = new Paint();
        mTestPaint.set(txtv.getPaint());
        float hi = 100;
        float lo = 2;
        final float threshold = 0.5f; // How close we have to be

        while((hi - lo) > threshold) {
            float size = (hi+lo)/2;
            mTestPaint.setTextSize(size);
            if(mTestPaint.measureText((String) txtv.getText()) >= targetheight)
                hi = size; // too big
            else
                lo = size; // too small
        }
        // Use lo so that we undershoot rather than overshoot
        txtv.setTextSize(TypedValue.COMPLEX_UNIT_PX, lo);
    }

    private String getDayOfWeekText(int n){
        switch(n){
            case 1:
                return "Sunday";
            case 2:
                return "Monday";
            case 3:
                return "Tuesday";
            case 4:
                return "Wednesday";
            case 5:
                return "Thursday";
            case 6:
                return "Friday";
            case 7:
                return "Saturday";
        }
        return null;
    }

    /*
        Also styles the rowview
     */
    private String getDayMessage(int dayOfWeek, View rowview){
        String primaryText;
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_NOW);
        String gymDay = getContext().getResources().getString(R.string.gym_day);
        String restDay = getContext().getResources().getString(R.string.rest_day);
        if (weeklySchedule.contains(dayOfWeek)){
            primaryText = gymDay;
        } else{
            primaryText = restDay;
        }
        if (primaryText.equals(gymDay)){
            rowview.setBackgroundColor(context.getResources().getColor(R.color.lightgreen));
        }else{
            rowview.setBackgroundColor(context.getResources().getColor(R.color.basewhite));
        }
        return primaryText;
    }

    public void updateData(List<DayRecord> allPreviousDays, HashSet<String> exceptionDays,
                           HashSet<Integer> weeklySchedule){
        if (weeklySchedule!=null){
            this.weeklySchedule=weeklySchedule;
        }
        this.notifyDataSetChanged();
    }

}
