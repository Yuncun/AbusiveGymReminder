package com.pipit.agc.agc;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.pipit.agc.agc.LocationListFragment.OnListFragmentInteractionListener;
import java.util.List;

/**
 * TODO: Replace the implementation with code for your data type.
 */
public class LocationListAdapter extends RecyclerView.Adapter<LocationListAdapter.ViewHolder> {

    private static final String TAG = "LocationListAdapter";
    private final List<Gym> mValues;
    private final OnListFragmentInteractionListener mListener;
    private final LocationListFragment mFrag;

    public LocationListAdapter(List<Gym> gyms, OnListFragmentInteractionListener listener, LocationListFragment frag) {
        mFrag = frag;
        mValues = gyms;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.location_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.mItem = mValues.get(position);
        holder.mIdView.setText(Integer.toString(mValues.get(position).proxid));
        holder.mContentView.setText(mValues.get(position).address + " " + mValues.get(position).location.getLongitude()
            + " " + mValues.get(position).location.getLatitude());

        holder.mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "clicked addButton");
                mFrag.mFlag = position+1;
                mFrag.startPlacePicker(position+1);
            }
        });

        holder.mRemoveButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Log.d(TAG, "clicked removeButton");
                ((AllinOneActivity) mFrag.getActivity()).removeProxAlert(position + 1);
                ((AllinOneActivity) mFrag.getActivity()).removeGeofencesById(position + 1);
                mValues.set(position, new Gym());
                notifyDataSetChanged();
            }

        });

    }

    @Override
    public int getItemCount() {
        return Math.min(mValues.size(), 3);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final CardView mCv;
        public final TextView mIdView;
        public final TextView mContentView;
        public final Button mAddButton;
        public final Button mRemoveButton;

        public Gym mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mCv = (CardView) view.findViewById(R.id.location_cv);
            mIdView = (TextView) view.findViewById(R.id.id);
            mContentView = (TextView) view.findViewById(R.id.content);
            mAddButton = (Button) view.findViewById(R.id.addButton);
            mRemoveButton = (Button) view.findViewById(R.id.removeButton);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }


}
