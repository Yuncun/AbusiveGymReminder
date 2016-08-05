package com.pipit.agc.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.pipit.agc.R;
import com.pipit.agc.data.InsultRecordsConstants;
import com.pipit.agc.model.DayRecord;
import com.pipit.agc.util.Constants;
import com.pipit.agc.util.SharedPrefUtil;
import com.pipit.agc.util.StatsContent;

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
                        .inflate(R.layout.preference_maturitylevel, parent, false);
                return new MaturityViewHolder(view);
            case 1:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.preference_notificationtime, parent, false);
                return new NotificationTimeViewHolder(view);
            case 2:
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
                final MaturityViewHolder mv = (MaturityViewHolder) holder;
                mv.mPrefName.setText("Maturity Level");
                mv.subtitle.setText("Set abuse level of messages");

                //Find the current selection
                int mvindex = SharedPrefUtil.getInt(_context,  Constants.MATURITY_LEVEL, InsultRecordsConstants.MED_MATURITY);
                ((RadioButton) mv.radiohead.getChildAt(mvindex)).setChecked(true);

                int currentLevel = SharedPrefUtil.getInt(_context, Constants.MATURITY_LEVEL, InsultRecordsConstants.MED_MATURITY);
                if (currentLevel == InsultRecordsConstants.HIGH_MATURITY){
                    ((RadioButton) mv.radiohead.getChildAt(0)).setText("Bitch Mode");
                    ((RadioButton) mv.radiohead.getChildAt(1)).setText("Loser Mode");
                    ((RadioButton) mv.radiohead.getChildAt(2)).setText("Abusive (Mature)");
                    mv.contentDescription.setVisibility(View.VISIBLE);
                    mv.contentDescription.setText("Contains profanity");
                }else {
                    ((RadioButton) mv.radiohead.getChildAt(0)).setText("Passive Aggressive");
                    ((RadioButton) mv.radiohead.getChildAt(1)).setText("Abusive");
                    ((RadioButton) mv.radiohead.getChildAt(2)).setText("Abusive (Mature)");
                    mv.contentDescription.setVisibility(View.INVISIBLE);
                }

                mv.radiohead.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        View radioButton = mv.radiohead.findViewById(checkedId);
                        int index = mv.radiohead.indexOfChild(radioButton);
                        switch (index) {
                            case 0:
                                SharedPrefUtil.putInt(_context, Constants.MATURITY_LEVEL, InsultRecordsConstants.LOW_MATURITY);
                                mv.contentDescription.setVisibility(View.INVISIBLE);
                                break;
                            case 1:
                                SharedPrefUtil.putInt(_context, Constants.MATURITY_LEVEL, InsultRecordsConstants.MED_MATURITY);
                                mv.contentDescription.setVisibility(View.INVISIBLE);
                                break;
                            case 2:
                                SharedPrefUtil.putInt(_context, Constants.MATURITY_LEVEL, InsultRecordsConstants.HIGH_MATURITY);
                                mv.contentDescription.setVisibility(View.VISIBLE);
                                mv.contentDescription.setText("Contains profanity");
                                break;
                            default:
                                break;
                        }
                    }
                });

                break;
            case 1:
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
            case 2:
                final OneButtonViewHolder obv = ((OneButtonViewHolder) holder);
                obv.mPrefName.setText("Sanitize Dayrecords");
                obv.subtitle.setText("Remove unreasonably long gym visits");
                obv.button.setText("Go");
                obv.button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        List<DayRecord> days = StatsContent.getInstance().getAllDayRecords(true);
                        int count = 0;
                        for (DayRecord d : days){
                            if (d.sanitizeLastVisitTime(DayRecord.maxSaneVisitTime)) count++;
                        }

                        Toast.makeText(v.getContext(), "Removed " + count +  " visits", Toast.LENGTH_SHORT);
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
        TextView subtitle;

        public OneButtonViewHolder (View view){
            super(view);
            button = (Button) view.findViewById(R.id.prefbutton);
            subtitle = (TextView) view.findViewById(R.id.prefsubtext);
        }
    }

    public class MaturityViewHolder extends PreferencesAdapter.ViewHolder{
        RadioGroup radiohead;
        TextView subtitle;
        TextView contentDescription;

        public MaturityViewHolder(View view){
            super(view);
            radiohead = (RadioGroup) view.findViewById(R.id.radiogroup_notiftime);
            subtitle = (TextView) view.findViewById(R.id.subtitle);
            contentDescription = (TextView) view.findViewById(R.id.content_description);
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
        return 3;
    }

    @Override
    public int getItemViewType (int position) {
        return position;
    }

}
