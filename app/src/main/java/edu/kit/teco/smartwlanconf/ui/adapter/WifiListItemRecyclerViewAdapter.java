package edu.kit.teco.smartwlanconf.ui.adapter;

import androidx.recyclerview.widget.RecyclerView;

import android.net.wifi.ScanResult;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import edu.kit.teco.smartwlanconf.R;
import edu.kit.teco.smartwlanconf.ui.fragments.ListOfWifisFragment.OnWifiListFragmentInteractionListener;;

import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link android.net.wifi.ScanResult} and makes a call to the
 * specified {@link OnWifiListFragmentInteractionListener}.
 *
 * See https://developer.android.com/guide/topics/ui/layout/recyclerview
 */
public class WifiListItemRecyclerViewAdapter extends RecyclerView.Adapter<WifiListItemRecyclerViewAdapter.ViewHolder> {

    private final List<ScanResult> mValues;
    private final OnWifiListFragmentInteractionListener mListener;

    public WifiListItemRecyclerViewAdapter(List<ScanResult> items, OnWifiListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mContentView.setText(mValues.get(position).SSID);

        //Every scan result gets an onClickListener
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onWifiListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mContentView;
        public ScanResult mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mContentView = (TextView) view.findViewById(R.id.content);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
