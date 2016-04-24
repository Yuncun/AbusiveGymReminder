package com.pipit.agc.agc.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pipit.agc.agc.data.DBRecordsSource;
import com.pipit.agc.agc.util.Constants;
import com.pipit.agc.agc.R;
import com.pipit.agc.agc.util.Util;
import com.pipit.agc.agc.activity.MessageBodyActivity;
import com.pipit.agc.agc.model.DayRecord;
import com.pipit.agc.agc.model.Message;

import java.util.List;

/**
 * Created by Eric on 1/22/2016.
 */
public class NewsfeedAdapter extends RecyclerView.Adapter<NewsfeedAdapter.CardViewHolder> {
    public static final double GYM_STATUS_HEIGHT_RATIO=.333;
    private List<Message> _messages;
    private List<DayRecord> _days;
    private Context _context;

    public static class CardViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView timestamp;
        TextView header;
        TextView comment;
        TextView reason;
        View icon;
        LinearLayout iconwrapper;

        CardViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.cv);
            header = (TextView)itemView.findViewById(R.id.header);
            comment = (TextView)itemView.findViewById(R.id.comment);
            timestamp = (TextView) itemView.findViewById(R.id.date);
            reason = (TextView) itemView.findViewById(R.id.reason);
            icon = itemView.findViewById(R.id.statusicon);
            iconwrapper = (LinearLayout) itemView.findViewById(R.id.iconwrapper);
        }
    }

    public NewsfeedAdapter(List<Message> mMessages, List<DayRecord> mDays, Context context) {
        _messages = mMessages;
        _context = context;
        _days = mDays;
    }

    @Override
    public CardViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.newsfeed_item_card, parent, false);
        CardViewHolder vh = new CardViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(CardViewHolder holder, int position) {
        Message m = _messages.get(position);
        Resources r = _context.getResources();
        holder.header.setText(m.getHeader());
        holder.comment.setText(m.getBody());
        holder.timestamp.setText(m.getIntelligentDateString());
        holder.timestamp.setTextColor(ContextCompat.getColor(_context, R.color.black));
        if (m.getReason()== Message.HIT_YESTERDAY) {
            holder.reason.setText(r.getText(R.string.reason_hit_gym_yesterday));
            holder.icon.getBackground().setColorFilter(ContextCompat.getColor(_context, R.color.schemethree_teal), PorterDuff.Mode.SRC_ATOP);
            holder.reason.setVisibility(View.VISIBLE);
            holder.icon.setVisibility(View.VISIBLE);
        }
        if (m.getReason()== Message.MISSED_YESTERDAY) {
            holder.reason.setText(r.getText(R.string.reason_missed_gym));
            holder.icon.getBackground().setColorFilter(ContextCompat.getColor(_context, R.color.schemethree_red), PorterDuff.Mode.SRC_ATOP);
            holder.reason.setVisibility(View.VISIBLE);
            holder.icon.setVisibility(View.VISIBLE);
        }
        if (m.getReason()== Message.HIT_TODAY) {
            holder.reason.setText(r.getText(R.string.reason_hit_gym));
            //holder.reason.setTextColor(ContextCompat.getColor(_context, R.color.darkgreen));
            holder.reason.setVisibility(View.VISIBLE);
            holder.icon.setVisibility(View.VISIBLE);
            holder.icon.getBackground().setColorFilter(ContextCompat.getColor(_context, R.color.schemethree_teal), PorterDuff.Mode.SRC_ATOP);
        }
        if (m.getReason() == Message.WELCOME) {
            holder.icon.getBackground().setColorFilter(ContextCompat.getColor(_context, R.color.schemefour_yellow), PorterDuff.Mode.SRC_ATOP);
            holder.reason.setText(r.getText(R.string.welcome));
        }
        //holder.iconwrapper.setLayoutParams(new RelativeLayout.LayoutParams(holder.iconwrapper.getMeasuredHeight(), holder.iconwrapper.getMeasuredHeight()));
        //holder.reason.setTextSize(12);
        //holder.timestamp.setTextSize(12);

        if (!m.getRead()){
        //Todo: Add "read" field to databaseace(null, Typeface.BOLD);
            holder.reason.setTypeface(holder.reason.getTypeface(), Typeface.BOLD);
            //holder.header.setTypeface(holder.comment.getTypeface(), Typeface.BOLD);
            //holder.timestamp.setTypeface(holder.timestamp.getTypeface(), Typeface.BOLD);
        }
        Bitmap bMap = BitmapFactory.decodeResource(_context.getResources(), R.drawable.notification_icon);
        final int mpos = position;
        holder.cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(_context, MessageBodyActivity.class);
                intent.putExtra(Constants.MESSAGE_ID, _messages.get(mpos).getId());
                _context.startActivity(intent);
                _messages.get(mpos).setRead(true);
                DBRecordsSource datasource = DBRecordsSource.getInstance();
                datasource.openDatabase();
                datasource.markMessageRead(_messages.get(mpos).getId(), true);
                datasource.closeDatabase();
                notifyDataSetChanged();
           }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return _messages.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    private int calculateGymStatusCardHeight(){
        double screenheight = Util.getScreenHeightMinusStatusBar(_context);
        screenheight*=GYM_STATUS_HEIGHT_RATIO;
        return (int) screenheight;
    }


    public void updateDayrecords(List<DayRecord> newSet){
        _days=newSet;
    }

}