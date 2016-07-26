package com.pipit.agc.agc.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.pipit.agc.agc.R;
import com.pipit.agc.agc.model.DayRecord;
import com.pipit.agc.agc.model.Message;
import com.pipit.agc.agc.util.Constants;
import com.pipit.agc.agc.util.SharedPrefUtil;
import com.pipit.agc.agc.util.StatsContent;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by Eric on 5/8/2016.
 */
public class PreferencesAdapter extends RecyclerView.Adapter<PreferencesAdapter.ViewHolder>{

    private static final String TAG = "PreferencesAdapter";
    private Context _context;

    public PreferencesAdapter( Context context) {
        _context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        switch (viewType){
            case 0:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.preference_notificationtime, parent, false);
                return new NotificationTimeViewHolder(view);
            case 1:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.preference_onebutton, parent, false);
                return new OneButtonViewHolder(view);
            default:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.preference_rowitem, parent, false);
        }


        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        int type = getItemViewType(position);
        switch (type) {
            case 0:
                final NotificationTimeViewHolder dv = ((NotificationTimeViewHolder) holder);
                dv.mPrefName.setText("Preferred Notification Time");

                //Find the current selection
                int index = SharedPrefUtil.getInt(_context, Constants.PREF_NOTIF_TIME, Constants.NOTIFTIME_AFTERNOON);
                ((RadioButton) dv.radiohead.getChildAt(index)).setChecked(true);

                ((RadioButton) dv.radiohead.getChildAt(0)).setText("Morning");
                ((RadioButton) dv.radiohead.getChildAt(1)).setText("Afternoon");
                ((RadioButton) dv.radiohead.getChildAt(2)).setText("Evening");
                ((RadioButton) dv.radiohead.getChildAt(3)).setText("Yolo");

                dv.radiohead.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        View radioButton = dv.radiohead.findViewById(checkedId);
                        int index = dv.radiohead.indexOfChild(radioButton);
                        switch (index) {
                            case 0:
                                SharedPrefUtil.putInt(_context, Constants.PREF_NOTIF_TIME, Constants.NOTIFTIME_MORNING);
                                break;
                            case 1:
                                SharedPrefUtil.putInt(_context, Constants.PREF_NOTIF_TIME, Constants.NOTIFTIME_AFTERNOON);
                                break;
                            case 2:
                                SharedPrefUtil.putInt(_context, Constants.PREF_NOTIF_TIME, Constants.NOTIFTIME_EVENING);
                                break;
                            case 3:
                                SharedPrefUtil.putInt(_context, Constants.PREF_NOTIF_TIME, Constants.NOTIFTIME_YOLO);
                                break;
                            default:
                                break;
                        }
                    }
                });

                break;
            case 1:
                final OneButtonViewHolder obv = ((OneButtonViewHolder) holder);
                obv.mPrefName.setText("Sanitize Dayrecords");
                obv.button.setText("Go");
                obv.button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        List<DayRecord> days = StatsContent.getInstance().getAllDayRecords(true);
                        for (DayRecord d : days){
                            d.sanitizeLastVisitTime(DayRecord.maxSaneVisitTime);
                        }

                        Toast.makeText(v.getContext(), "Removed unreasonably long gym visits", Toast.LENGTH_SHORT);
                    }
                });
                break;
            default:
                break;
        }
    }


    /* Preference Row Item Models */

    /**
     * This is the radio group for Notification Time
     * MORNING / AFTERNOON / EVENING
     */
    public class NotificationTimeViewHolder extends PreferencesAdapter.ViewHolder{
        RadioGroup radiohead;

        public NotificationTimeViewHolder(View view){
            super(view);
            radiohead = (RadioGroup) view.findViewById(R.id.radiogroup_notiftime);
        }
    }

    public class OneButtonViewHolder extends PreferencesAdapter.ViewHolder{
        Button button;

        public OneButtonViewHolder (View view){
            super(view);
            button = (Button) view.findViewById(R.id.prefbutton);
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mPrefName;
        public StatsContent.Stat mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mPrefName = (TextView) view.findViewById(R.id.prefkey);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mPrefName.getText() + "'";
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    @Override
    public int getItemViewType (int position) {
        return position;
    }

}
