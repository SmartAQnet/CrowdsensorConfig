package edu.kit.teco.smartwlanconf.ui.fragments;

import android.content.Context;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.thanosfisherman.wifiutils.WifiUtils;

import edu.kit.teco.smartwlanconf.R;
import edu.kit.teco.smartwlanconf.ui.adapter.WifiListItemRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

import static androidx.constraintlayout.widget.Constraints.TAG;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnWifiListFragmentInteractionListener}
 * interface.
 */
public class ListOfWifisFragment extends Fragment{

    private static final String ARG_COLUMN_COUNT = "Verfügbare Wlan";
    private int mColumnCount = 1;
    private OnWifiListFragmentInteractionListener mListener;
    private List<ScanResult> wifiList = new ArrayList<>();
    private Fragment this_fragment;

    private WifiListItemRecyclerViewAdapter wifiAdapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ListOfWifisFragment() {

    }

    public static ListOfWifisFragment newInstance(int columnCount) {
        ListOfWifisFragment fragment = new ListOfWifisFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this_fragment = this;
        return setAdapter(inflater.inflate(R.layout.wifi_fragment_item_list, container, false));
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnWifiListFragmentInteractionListener) {
            mListener = (OnWifiListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private View setAdapter(View view){
        // Set the adapter
        if (view instanceof FrameLayout) {
            Context context = view.getContext();
            RecyclerView recyclerView = view.findViewById(R.id.list);
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            DividerItemDecoration dividerItemDecoration= new DividerItemDecoration(context,
                    LinearLayoutManager.VERTICAL);
            recyclerView.addItemDecoration(dividerItemDecoration);
            WifiUtils.withContext(getActivity().getApplicationContext()).scanWifi(this::getScanResults).start();
            wifiAdapter = new WifiListItemRecyclerViewAdapter(wifiList, mListener);
            recyclerView.setAdapter(wifiAdapter);
        }
        return view;
    }

    private void getScanResults(@NonNull final List<ScanResult> results){
        if (results.isEmpty())
        {
            final Context context = getActivity().getApplicationContext();
            Snackbar snackbar = Snackbar
                    .make(getView(), "Keine Wifi Netze gefunden", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Nochmal versuchen!", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            WifiUtils.withContext(context).scanWifi(ListOfWifisFragment.this::getScanResults).start();
                        }
                    });
            snackbar.setActionTextColor(Color.RED);
            snackbar.show();
            return;
        }
        //Scan results not empty hide splash screen
        View view = getView();
        LinearLayout splash = (LinearLayout) view.findViewById(R.id.splash);
        RecyclerView list = (RecyclerView) view.findViewById(R.id.list);
        splash.setVisibility(View.GONE);
        list.setVisibility(View.VISIBLE);
        wifiList.clear();
        int netCount = results.size() - 1;
        while (netCount >= 0){
            ScanResult result = results.get(netCount);
            if(!result.SSID.isEmpty()) {
                wifiList.add(results.get(netCount));
                wifiAdapter.notifyDataSetChanged();
            }
            --netCount;
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnWifiListFragmentInteractionListener {
        void onWifiListFragmentInteraction(ScanResult scanResult);
    }
}
