package com.pipit.agc.agc;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pipit.agc.agc.LocationListFragment.OnListFragmentInteractionListener;
import java.util.List;

/**
 * TODO: Replace the implementation with code for your data type.
 */
public class LocationListAdapter extends RecyclerView.Adapter<LocationListAdapter.ViewHolder> {

    private final List<Gym> mValues;
    private final OnListFragmentInteractionListener mListener;

    public LocationListAdapter(List<Gym> gyms, OnListFragmentInteractionListener listener) {
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
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mIdView.setText(Integer.toString(mValues.get(position).proxid));
        holder.mContentView.setText(mValues.get(position).address + " " + mValues.get(position).location.getLongitude()
            + " " + mValues.get(position).location.getLatitude());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    //mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return Math.min(mValues.size(), 3);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mContentView;
        public Gym mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.id);
            mContentView = (TextView) view.findViewById(R.id.content);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
