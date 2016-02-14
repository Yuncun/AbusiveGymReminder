package com.pipit.agc.agc;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.pipit.agc.agc.data.DayRecord;
import com.pipit.agc.agc.data.Message;

import java.util.Date;
import java.util.List;

/**
 * Created by Eric on 1/22/2016.
 */
public class NewsfeedAdapter extends RecyclerView.Adapter<NewsfeedAdapter.CardViewHolder> {
    public static final double GYM_STATUS_HEIGHT_RATIO=.333;
    private List<Message> _messages;
    private List<DayRecord> _days;
    private Context _context;
    private int _offset = 0; //Used because the first element is not a list item

    public static class CardViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView header;
        TextView comment;
        ImageView icon;

        CardViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.cv);
            header = (TextView)itemView.findViewById(R.id.header);
            comment = (TextView)itemView.findViewById(R.id.comment);
            icon = (ImageView)itemView.findViewById(R.id.icon);
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

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(CardViewHolder holder, int position) {
        //The gym status card
        if(false){
            holder.cv.setMinimumHeight(calculateGymStatusCardHeight());
            holder.cv.setCardBackgroundColor(_context.getResources().getColor(R.color.lightgreen));
            holder.cv.setCardElevation(0);
            holder.cv.setMaxCardElevation(0);
            holder.cv.setPadding(0, 0, 20, 0);
            //holder.header.setText(getGymDay());
            //holder.comment.setText(getDayComments());
        }else {
            holder.header.setText(_messages.get(position - _offset).getComment());
            holder.comment.setText(_messages.get(position - _offset).getDateString());
            Bitmap bMap = BitmapFactory.decodeResource(_context.getResources(), R.drawable.notification_icon);
            final int mpos = position;
            holder.cv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(_context, MessageBodyActivity.class);
                    intent.putExtra(Constants.MESSAGE_ID, _messages.get(mpos - _offset).getId());
                    _context.startActivity(intent);
                    //Todo: Mark comment as "read"
                }
            });
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return _messages.size()+_offset;
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