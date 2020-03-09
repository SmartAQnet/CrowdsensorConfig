package edu.kit.teco.smartwlanconf.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.android.material.snackbar.Snackbar;

import edu.kit.teco.smartwlanconf.R;
import edu.kit.teco.smartwlanconf.SmartWlanConfApplication;
import edu.kit.teco.smartwlanconf.ui.Config;
import edu.kit.teco.smartwlanconf.ui.SmartWlanConfActivity;
import edu.kit.teco.smartwlanconf.ui.adapter.WifiListItemRecyclerViewAdapter;
import edu.kit.teco.smartwlanconf.ui.utils.WifiConnectionUtils;
import edu.kit.teco.smartwlanconf.ui.utils.WifiScanRunnable;

import java.util.ArrayList;
import java.util.List;


/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnWifiListFragmentInteractionListener}
 * interface.
 *
 * This fragment shows the results of a scan for available wifis
 */
public class ListOfWifisFragment extends WifiFragment {

    private static final String ARG_COLUMN_COUNT = "Verfügbare Wlan";
    private int mColumnCount = 1;
    private OnWifiListFragmentInteractionListener mListener;
    private List<ScanResult> wifiList = new ArrayList<>();

    private WifiListItemRecyclerViewAdapter wifiAdapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ListOfWifisFragment() {

    }

    //columncount sets the number of columns used to display wifis
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
        this.setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.wifi_fragment_item_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setAdapter(view);
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


    //This methods starts scanning and
    //sets the WifiListItemRecyclerAdapter that is used for showing scan results
    private void setAdapter(View view){
        Context context = getActivity();
        RecyclerView recyclerView = view.findViewById(R.id.list);
        if (mColumnCount <= 1) {
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }
        DividerItemDecoration dividerItemDecoration= new DividerItemDecoration(context,
                LinearLayoutManager.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);
        WifiConnectionUtils wifi = SmartWlanConfApplication.getWifi(getContext());
        wifiAdapter = new WifiListItemRecyclerViewAdapter(wifiList, mListener);
        recyclerView.setAdapter(wifiAdapter);
        final WifiFragment wifiFragment = this;
        //Start scanning for wifis in async task
        WifiScanRunnable wifiScan = new WifiScanRunnable(wifiFragment, wifi);
        //Save wifiscan to stop runnable after scanning wifis
        SmartWlanConfApplication.setWifiScan(context, wifiScan);
        Thread t = new Thread(wifiScan);
        t.start();
    }

    //Callback method, when wifi scan returns it's results
    @Override
    public void onWaitForWifiScan(List<ScanResult> results){
        View view = getView();
        if(view == null){
            Log.d(ListOfWifisFragment.class.toString(), "view is null in onWaitForWifiScan()");
            return;
        }
        Activity activity = getActivity();
        if(activity == null){
            Log.d(ListOfWifisFragment.class.toString(), "activity is null in onWaitForWifiScan()");
            return;
        }

        if (results == null) {
            Snackbar snackbar = Snackbar
                    .make(view, "Keine Wifi Netze gefunden", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Nochmal versuchen!", (View v)->{
                        WifiManager wifi =(WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                        if(!wifi.isWifiEnabled()){
                            //Does this happen?
                            Log.e("ListOfWifisFragment","Wifi nicht aktiviert zum Scannen");
                        }
                        ((SmartWlanConfActivity)getActivity()).setInitialFragment();
                    });
            int colorSnackRetry = ResourcesCompat.getColor(activity.getResources(), R.color.colorSnackRetry, null);
            snackbar.setActionTextColor(colorSnackRetry);
            snackbar.show();
            return;
        }
        //Scan results if not empty show list
        LinearLayout splash = view.findViewById(R.id.splash);
        RecyclerView list = view.findViewById(R.id.list);
        splash.setVisibility(View.GONE);
        list.setVisibility(View.VISIBLE);
        wifiList.clear();

        // Add results to list of wifis
        for(int i = 0; i < results.size(); i++){
            ScanResult result = results.get(i);
            if (!result.SSID.isEmpty() && result.frequency <= Config.WIFI_BANDWIDTH) {
                wifiList.add(result);
                wifiAdapter.notifyDataSetChanged();
            }
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
