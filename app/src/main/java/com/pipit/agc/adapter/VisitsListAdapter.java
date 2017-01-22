package com.pipit.agc.adapter;

import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.pipit.agc.R;
import com.pipit.agc.data.MsgAndDayRecords;
import com.pipit.agc.model.DayRecord;

import java.util.List;
import java.util.Stack;

/**
 * This adapter is responsible for the list of visits in DayRecord dialog.
 *
 * Created by Eric on 1/14/2017.
 */
public class VisitsListAdapter extends RecyclerView.Adapter<VisitsListAdapter.ViewHolder> {
    private DayRecord _day;
    private Stack<View> _selectedViews; //A list of rows that are currently selected
    private TimePickerCallback tfunc;
    private DayDialogCallback callback;

    public interface DayDialogCallback{
        void redrawTimeSpent();
        void setGymVisited(boolean tf);
    }

    public interface TimePickerCallback {
        void showTimePickers(DayRecord.Visit v, int position, boolean isNewRecord);
        void showDefault();
    }

    public VisitsListAdapter(DayRecord day, TimePickerCallback tpc){
        _selectedViews = new Stack<>();
        _day = day;
        tfunc = tpc;
    }

    public void setDayDialogCallBack(DayDialogCallback cb){
        callback = cb;
    }
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.visit_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final DayRecord.Visit visit = _day.getVisits().get(position);
        holder.mTextView.setText(visit.print());
        holder.mDeleteButton.setVisibility(View.GONE);
        holder.mEditButton.setVisibility(View.GONE);
        View.OnClickListener editbuttonlistener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tfunc.showTimePickers(visit, position, false);
            }
        };
        holder.mEditButton.setOnClickListener(editbuttonlistener);
        View.OnClickListener mRowClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toggle to or from edit-mode. Since we can't store a flag in the view, use the
                //visibility of the remove button to determine if curent mode is editable or not.
                if (v.findViewById(R.id.removevisitbutton).getVisibility() == View.GONE
                        || !_selectedViews.contains(v)){
                    //Deselect all other rows
                    while(!_selectedViews.isEmpty()){
                        View vs = _selectedViews.pop();
                        setRowToEditable(false, vs);
                    }
                    setRowToEditable(true, v);
                    _selectedViews.add(v);
                }else{
                    setRowToEditable(false, v);
                    _selectedViews.remove(v);
                }
            }
        };

        holder.mView.setOnClickListener(mRowClickListener);
        View.OnClickListener mDeleteButtonListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Delete, but also save item in variable in case we undo
                final DayRecord.Visit justInCaseOfDelete = _day.getVisits().get(position);
                _day.removeVisit(position);
                if (callback!=null){
                    callback.redrawTimeSpent();
                    if (_day.getVisits().size()==0){
                        callback.setGymVisited(false);
                    }
                }
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, _day.getVisits().size());
                Snackbar.make(v,"Deleted Saved Selection.", Snackbar.LENGTH_LONG).
                        setAction("Undo", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                _day.addVisitAndMergeSubsets(justInCaseOfDelete);
                                notifyDataSetChanged();
                            }
                        }).show();

                //Update database
                MsgAndDayRecords datasource;
                datasource = MsgAndDayRecords.getInstance();
                datasource.openDatabase();
                datasource.updateDayRecordVisitsById(_day.getId(), _day.getSerializedVisitsList());
                datasource.closeDatabase();
            }
        };
        holder.mDeleteButton.setOnClickListener(mDeleteButtonListener);
    }

    @Override
    public int getItemCount(){
        return _day.getVisits().size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mTextView;
        public final ImageView mEditButton;
        public final ImageView mDeleteButton;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mEditButton = (ImageView) view.findViewById(R.id.editvisitbutton);
            mDeleteButton = (ImageView) view.findViewById(R.id.removevisitbutton);
            mTextView = (TextView) view.findViewById(R.id.visittext);
        }
    }

    private void setRowToEditable(boolean isEditable, View v){
        if (isEditable){
            v.setBackgroundColor(ContextCompat.getColor(v.getContext(), R.color.grey_200));
            v.findViewById(R.id.removevisitbutton).setVisibility(View.VISIBLE);
            v.findViewById(R.id.editvisitbutton).setVisibility(View.VISIBLE);
        }else{
            v.setBackgroundColor(ContextCompat.getColor(v.getContext(), R.color.transparent));
            v.findViewById(R.id.removevisitbutton).setVisibility(View.GONE);
            v.findViewById(R.id.editvisitbutton).setVisibility(View.GONE);
        }
    }


}
