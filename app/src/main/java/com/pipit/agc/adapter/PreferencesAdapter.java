package com.pipit.agc.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.pipit.agc.R;
import com.pipit.agc.activity.SettingsActivity;
import com.pipit.agc.data.InsultRecordsConstants;
import com.pipit.agc.data.MsgAndDayRecords;
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
            case 3:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.preference_slider, parent, false);
                return new RangePickerViewHolder(view);
            case 4:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.preference_togglebutton, parent, false);
                return new ToggleButtonViewHolder(view);
            case 5:
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
                //"Maturity level"
                final MaturityViewHolder mv = (MaturityViewHolder) holder;
                mv.mPrefName.setText(_context.getString(R.string.maturitylevel));
                mv.subtitle.setText(_context.getString(R.string.setabuselevel));

                //Find the current selection
                int mvindex = SharedPrefUtil.getInt(_context,  Constants.MATURITY_LEVEL, InsultRecordsConstants.MED_MATURITY);
                ((RadioButton) mv.radiohead.getChildAt(mvindex)).setChecked(true);

                int currentLevel = SharedPrefUtil.getInt(_context, Constants.MATURITY_LEVEL, InsultRecordsConstants.MED_MATURITY);
                if (currentLevel == InsultRecordsConstants.HIGH_MATURITY){
                    ((RadioButton) mv.radiohead.getChildAt(0)).setText(_context.getString(R.string.bitchmode));
                    ((RadioButton) mv.radiohead.getChildAt(1)).setText(_context.getString(R.string.losermode));
                    ((RadioButton) mv.radiohead.getChildAt(2)).setText(_context.getString(R.string.abusiveprofane));
                    mv.contentDescription.setVisibility(View.VISIBLE);
                    mv.contentDescription.setText(_context.getString(R.string.containsprofanity));
                }else {
                    ((RadioButton) mv.radiohead.getChildAt(0)).setText(_context.getString(R.string.passiveaggressive));
                    ((RadioButton) mv.radiohead.getChildAt(1)).setText(_context.getString(R.string.abusive));
                    ((RadioButton) mv.radiohead.getChildAt(2)).setText(_context.getString(R.string.abusiveprofane));
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
                                mv.contentDescription.setText(_context.getString(R.string.containsprofanity));
                                break;
                            default:
                                break;
                        }
                    }
                });

                break;
            case 1:
                //"Notif pref time"
                final NotificationTimeViewHolder dv = ((NotificationTimeViewHolder) holder);
                dv.mPrefName.setText(_context.getString(R.string.prefnotiftime));

                //Find the current selection
                int index = SharedPrefUtil.getInt(_context, Constants.PREF_NOTIF_TIME, Constants.NOTIFTIME_AFTERNOON);
                ((RadioButton) dv.radiohead.getChildAt(index)).setChecked(true);

                ((RadioButton) dv.radiohead.getChildAt(0)).setText(_context.getString(R.string.wakeup));
                ((RadioButton) dv.radiohead.getChildAt(1)).setText(_context.getString(R.string.morning));
                ((RadioButton) dv.radiohead.getChildAt(2)).setText(_context.getString(R.string.afternoon));
                ((RadioButton) dv.radiohead.getChildAt(3)).setText(_context.getString(R.string.evening));

                dv.radiohead.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(RadioGroup group, int checkedId) {
                        View radioButton = dv.radiohead.findViewById(checkedId);
                        int index = dv.radiohead.indexOfChild(radioButton);
                        switch (index) {
                            case 0:
                                SharedPrefUtil.putInt(_context, Constants.PREF_NOTIF_TIME, Constants.NOTIFTIME_ON_WAKEUP);
                                break;
                            case 1:
                                SharedPrefUtil.putInt(_context, Constants.PREF_NOTIF_TIME, Constants.NOTIFTIME_MORNING);
                                break;
                            case 2:
                                SharedPrefUtil.putInt(_context, Constants.PREF_NOTIF_TIME, Constants.NOTIFTIME_AFTERNOON);
                                break;
                            case 3:
                                SharedPrefUtil.putInt(_context, Constants.PREF_NOTIF_TIME, Constants.NOTIFTIME_EVENING);
                                break;
                            default:
                                break;
                        }
                    }
                });

                break;
            case 2:
                //"Sanitize days"
                final OneButtonViewHolder obv = ((OneButtonViewHolder) holder);
                obv.mPrefName.setText(_context.getString(R.string.sanitize));
                obv.subtitle.setText(_context.getString(R.string.removesallun));
                obv.button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "Clicked to sanitize dayrecords");
                        List<DayRecord> days = StatsContent.getInstance().getAllDayRecords(true);
                        int count = 0;
                        for (DayRecord d : days){
                            if (d.sanitizeLastVisitTime(DayRecord.maxSaneVisitTime)){
                                MsgAndDayRecords datasource = MsgAndDayRecords.getInstance();
                                datasource.openDatabase();
                                datasource.updateDayRecordVisitsById(d.getId(), d.getSerializedVisitsList());
                                datasource.closeDatabase();
                                count++;
                            }
                        }
                        Toast.makeText(v.getContext(), "Removed " + count +  " visits", Toast.LENGTH_SHORT).show();
                    }
                });
                break;
            case 3:
                //"Geofence Radius"
                final RangePickerViewHolder rnv = ((RangePickerViewHolder) holder);
                rnv.mPrefName.setText(_context.getString(R.string.geofenceradius));
                rnv.subtitle.setText("(Meters)");
                rnv.value.setTextColor(ContextCompat.getColor(_context, R.color.schemefour_darkerteal));
                final int currentRadius = SharedPrefUtil.getInt(_context, Constants.SHAR_PREF_GYMRADIUS, Constants.DEFAULT_RADIUS);
                        rnv.value.setText("" + currentRadius);

                rnv.clickableNumberLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        MaterialDialog dialog = new MaterialDialog.Builder(v.getContext())
                                .title(_context.getString(R.string.geofenceradius))
                                .customView(R.layout.numberpicker, true)
                                .positiveText(R.string.ok)
                                .show();

                        View dv = dialog.getCustomView();
                        NumberPicker numberPicker = (NumberPicker) dv.findViewById(R.id.numpicker);

                        //Numberpicker step size
                        final int STEPSIZE = 10;
                        NumberPicker.Formatter formatter = new NumberPicker.Formatter() {
                            @Override
                            public String format(int value) {
                                int temp = value * STEPSIZE;
                                return "" + temp;
                            }
                        };
                        numberPicker.setFormatter(formatter);

                        numberPicker.setMinValue(Constants.GEOFENCE_MIN_RADIUS / STEPSIZE);
                        numberPicker.setMaxValue(Constants.GEOFENCE_MAX_RADIUS / STEPSIZE);
                        numberPicker.setValue(currentRadius / STEPSIZE);
                        //Set a value change listener for NumberPicker
                        numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                            @Override
                            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                                newVal *= STEPSIZE;
                                //Toast.makeText(_context, "Radius changed", Toast.LENGTH_SHORT);
                                SharedPrefUtil.putInt(_context, Constants.SHAR_PREF_GYMRADIUS, newVal);
                                rnv.value.setText("" + newVal);
                            }
                        });
                    }
                });
                break;
            case 4:
                //"Toggle messages"
                final ToggleButtonViewHolder tbv = ((ToggleButtonViewHolder) holder);
                boolean onVisitMessageEnabled = SharedPrefUtil.getBoolean(_context, Constants.PREF_SHOW_NOTIF_ON_GYMHITS, true);
                tbv.mPrefName.setText(_context.getString(R.string.receivemessageonvisits));
                tbv.switchbutton.setChecked(onVisitMessageEnabled);
                tbv.switchbutton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        SharedPrefUtil.putBoolean(_context, Constants.PREF_SHOW_NOTIF_ON_GYMHITS, isChecked);
                    }
                });
                break;
            case 5:
                //"About"
                final OneButtonViewHolder abv = ((OneButtonViewHolder) holder);
                String versNumber;

                try{
                    PackageInfo pInfo = _context.getPackageManager().getPackageInfo(_context.getPackageName(), 0);
                    versNumber = pInfo.versionName;
                } catch (PackageManager.NameNotFoundException e){
                    Log.e(TAG, "Could not find package name. Vers. number will be unavailable ", e);
                    versNumber = "?";
                }

                abv.mPrefName.setText(_context.getString(R.string.version));
                abv.subtitle.setText(versNumber);
                abv.button.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                        Intent i = new Intent(v.getContext(), SettingsActivity.class);
                        v.getContext().startActivity(i);
                        return false;
                    }
                });
            default:
                break;
        }
    }

    /* Preference Row Item Models */

    /**
     * This is the radio group for Notification Time
     * WAKEUP / MORNING / AFTERNOON / EVENING
     */
    public class NotificationTimeViewHolder extends PreferencesAdapter.ViewHolder{
        RadioGroup radiohead;

        public NotificationTimeViewHolder(View view){
            super(view);
            radiohead = (RadioGroup) view.findViewById(R.id.radiogroup_notiftime);
        }
    }

    public class OneButtonViewHolder extends PreferencesAdapter.ViewHolder{
        RelativeLayout button;
        TextView subtitle;

        public OneButtonViewHolder (View view){
            super(view);
            button = (RelativeLayout) view.findViewById(R.id.prefbutton);
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

    public class RangePickerViewHolder extends PreferencesAdapter.ViewHolder{
        TextView subtitle;
        TextView value;
        LinearLayout clickableNumberLayout;

        public RangePickerViewHolder(View view){
            super(view);
            subtitle = (TextView) view.findViewById(R.id.prefsubtext);
            value = (TextView) view.findViewById(R.id.prefradiusvalue);
            clickableNumberLayout = (LinearLayout) view.findViewById(R.id.clickableprefval);
        }
    }

    public class ToggleButtonViewHolder extends PreferencesAdapter.ViewHolder{
        SwitchCompat switchbutton;
        public ToggleButtonViewHolder(View view){
            super(view);
            switchbutton = (SwitchCompat) view.findViewById(R.id.on_off_toggle);
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
        return 6;
    }

    @Override
    public int getItemViewType (int position) {
        return position;
    }

}
