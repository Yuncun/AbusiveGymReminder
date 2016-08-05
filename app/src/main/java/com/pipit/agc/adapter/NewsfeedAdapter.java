package com.pipit.agc.adapter;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialcab.MaterialCab;
import com.pipit.agc.data.MsgAndDayRecords;
import com.pipit.agc.fragment.NewsfeedFragment;
import com.pipit.agc.util.Constants;
import com.pipit.agc.R;
import com.pipit.agc.util.StatsContent;
import com.pipit.agc.util.Util;
import com.pipit.agc.activity.MessageBodyActivity;
import com.pipit.agc.model.DayRecord;
import com.pipit.agc.model.Message;

import java.util.HashSet;
import java.util.List;

/**
 * Created by Eric on 1/22/2016.
 */
public class NewsfeedAdapter extends RecyclerView.Adapter<NewsfeedAdapter.CardViewHolder> {
    public static final double GYM_STATUS_HEIGHT_RATIO=.333;
    public static final int MAX_MSGS_PER_PULL = 100;

    private List<Message> _messages;
    private List<DayRecord> _days;
    private HashSet<Integer> _selectedPos;
    private NewsfeedFragment mFrag;
    private HashSet<Integer> _unreadMsgs;

    private int posOfItemThatStartedCAB;
    private boolean allSelected;
    private boolean selectionMode;

    public static class CardViewHolder extends RecyclerView.ViewHolder {
        boolean selected;
        CardView cv;
        TextView timestamp;
        TextView header;
        TextView comment;
        TextView reason;
        View icon;
        LinearLayout iconwrapper;
        RelativeLayout rlayout;

        CardViewHolder(View itemView) {
            super(itemView);
            selected=false;
            cv = (CardView)itemView.findViewById(R.id.cv);
            header = (TextView)itemView.findViewById(R.id.header);
            comment = (TextView)itemView.findViewById(R.id.comment);
            timestamp = (TextView) itemView.findViewById(R.id.date);
            reason = (TextView) itemView.findViewById(R.id.reason);
            icon = itemView.findViewById(R.id.statusicon);
            iconwrapper = (LinearLayout) itemView.findViewById(R.id.iconwrapper);
            rlayout = (RelativeLayout) itemView.findViewById(R.id.rlayout);
        }
    }

    public NewsfeedAdapter(List<Message> mMessages, List<DayRecord> mDays, NewsfeedFragment frag) {
        _messages = mMessages;
        _selectedPos = new HashSet<Integer>();
        selectionMode = false;
        allSelected = false;
        mFrag = frag;
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
    public void onBindViewHolder(final CardViewHolder holder, final int position) {
        Message m = _messages.get(position);
        Resources r = mFrag.getContext().getResources();
        holder.header.setText(m.getHeader());
        holder.comment.setText(m.getBody());
        holder.timestamp.setText(m.getIntelligentDateString());
        holder.icon.setBackground(ContextCompat.getDrawable(mFrag.getContext(), R.drawable.circle));
        //holder.rlayout.setBackgroundColor(ContextCompat.getColor(mFrag.getContext(), R.color.));
        holder.timestamp.setTextColor(ContextCompat.getColor(mFrag.getContext(), R.color.black));


        if (m.getReason()== Message.HIT_YESTERDAY) {
            holder.reason.setText(r.getText(R.string.reason_hit_gym_yesterday));
            holder.icon.getBackground().setColorFilter(ContextCompat.getColor(mFrag.getContext(), R.color.schemethree_teal), PorterDuff.Mode.SRC_ATOP);
            holder.reason.setVisibility(View.VISIBLE);
            holder.icon.setVisibility(View.VISIBLE);
        }
        if (m.getReason()== Message.MISSED_YESTERDAY) {
            holder.reason.setText(r.getText(R.string.reason_missed_gym));
            holder.icon.getBackground().setColorFilter(ContextCompat.getColor(mFrag.getContext(), R.color.schemethree_red), PorterDuff.Mode.SRC_ATOP);
            holder.reason.setVisibility(View.VISIBLE);
            holder.icon.setVisibility(View.VISIBLE);
        }
        if (m.getReason()== Message.HIT_TODAY) {
            holder.reason.setText(r.getText(R.string.reason_hit_gym));
            holder.reason.setVisibility(View.VISIBLE);
            holder.icon.setVisibility(View.VISIBLE);
            holder.icon.getBackground().setColorFilter(ContextCompat.getColor(mFrag.getContext(), R.color.schemethree_teal), PorterDuff.Mode.SRC_ATOP);
        }
        if (m.getReason() == Message.WELCOME) {
            holder.icon.getBackground().setColorFilter(ContextCompat.getColor(mFrag.getContext(), R.color.schemefour_yellow), PorterDuff.Mode.SRC_ATOP);
            holder.reason.setText(r.getText(R.string.welcome));
        }
        if (m.getReason() == Message.NEW_MSG){
            holder.icon.getBackground().setColorFilter(ContextCompat.getColor(mFrag.getContext(), R.color.schemefour_yellow), PorterDuff.Mode.SRC_ATOP);
            holder.reason.setText(r.getText(R.string.new_msg));
        }
        //holder.iconwrapper.setLayoutParams(new RelativeLayout.LayoutParams(holder.iconwrapper.getMeasuredHeight(), holder.iconwrapper.getMeasuredHeight()));
        //holder.reason.setTextSize(12);
        //holder.timestamp.setTextSize(12);

        //Todo:Reenable this when we figure out why it's randomly evaluating to true
        //if (!m.getRead()){
        if (false) {
            //Todo: Add "read" field to databaseace(null, Typeface.BOLD);
            Log.d("ERIC", "Position " + position + "Message" + m.getBody());
            holder.reason.setTextColor(ContextCompat.getColor(mFrag.getContext(), R.color.black));
            holder.reason.setTypeface(holder.reason.getTypeface(), Typeface.BOLD);
            holder.timestamp.setTextColor(ContextCompat.getColor(mFrag.getContext(), R.color.black));
            holder.timestamp.setTypeface(holder.reason.getTypeface(), Typeface.BOLD);
            //holder.header.setTypeface(holder.comment.getTypeface(), Typeface.BOLD);
            //holder.timestamp.setTypeface(holder.timestamp.getTypeface(), Typeface.BOLD);
        }

        if (selectionMode && _selectedPos.contains(position)){
            holder.icon.getBackground().setColorFilter(ContextCompat.getColor(mFrag.getContext(), R.color.basewhite), PorterDuff.Mode.SRC_ATOP);
            holder.icon.setBackground(ContextCompat.getDrawable(mFrag.getContext(), android.R.drawable.checkbox_on_background));
        }
        final int mpos = position;
        holder.cv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectionMode){
                    if (_selectedPos.contains(position)){
                        _selectedPos.remove(position);
                    }else{
                        _selectedPos.add(position);
                    }
                    notifyItemChanged(position);
                    return;
                }
                Intent intent = new Intent(mFrag.getContext(), MessageBodyActivity.class);
                intent.putExtra(Constants.MESSAGE_ID, _messages.get(mpos).getId());
                mFrag.getContext().startActivity(intent);
                //_messages.get(mpos).setRead(true);
                MsgAndDayRecords datasource = MsgAndDayRecords.getInstance();
                datasource.openDatabase();
                datasource.markMessageRead(_messages.get(mpos).getId(), true);
                //_messages=datasource.getAllMessages();
                datasource.closeDatabase();
                _messages = StatsContent.getInstance().getAllMessagesReverse(true);
                notifyDataSetChanged();
                /*
                holder.reason.setTextColor(ContextCompat.getColor(mFrag.getContext(), android.R.color.primary_text_light));
                holder.reason.setTypeface(holder.reason.getTypeface(), Typeface.NORMAL);
                holder.timestamp.setTextColor(ContextCompat.getColor(mFrag.getContext(), android.R.color.primary_text_dark));
                holder.timestamp.setTypeface(holder.reason.getTypeface(), Typeface.NORMAL);*/
           }
        });

        holder.cv.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
        public boolean onLongClick(View v){
                if (mFrag.getCab()==null) return false;
                if (selectionMode) return false;
                selectionMode = true;
                //v.setSelected(true);
                //notifyDataSetChanged();
                mFrag.getCab()
                        .setMenu(R.menu.cab_menu)
                        .setTitle("Manage Inbox")
                        .setBackgroundColor(ContextCompat.getColor(mFrag.getContext(), R.color.schemethree_darkerteal))
                        .start(mCabCallback);
                _selectedPos.add(position);
                notifyItemChanged(position);
                return true;
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
        double screenheight = Util.getScreenHeightMinusStatusBar(mFrag.getContext());
        screenheight*=GYM_STATUS_HEIGHT_RATIO;
        return (int) screenheight;
    }

    public void updateDayrecords(List<DayRecord> newSet){
        _days=newSet;
    }

    private MaterialCab.Callback mCabCallback = new MaterialCab.Callback() {
        @Override
        public boolean onCabCreated(MaterialCab cab, Menu menu) {
            return true;
        }

        @Override
        public boolean onCabItemClicked(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.select_all:
                    if (allSelected){
                        allSelected=false;
                        _selectedPos = new HashSet<>();
                    }else{
                        allSelected=true;
                        for(int i = 0 ; i < _messages.size(); i++){
                            _selectedPos.add(i);
                        }
                    }
                    notifyDataSetChanged();
                    return true;

                case R.id.delete:
                    MsgAndDayRecords datasource = MsgAndDayRecords.getInstance();
                    datasource.openDatabase();

                    for (Integer i : _selectedPos){
                        datasource.deleteMessage(_messages.get(i));
                    }
                    datasource.closeDatabase();
                    _messages = StatsContent.getInstance().getAllMessagesReverse(true);
                    _selectedPos = new HashSet<>();
                    notifyDataSetChanged();
                    return true;

                default:
                    // If we got here, the user's action was not recognized.
                    // Invoke the superclass to handle it.

            }
            return true;
        }

        @Override
        public boolean onCabFinished(MaterialCab cab) {
            selectionMode = false;
            notifyDataSetChanged();
            _selectedPos = new HashSet<>();
            return true;
        }
    };


}