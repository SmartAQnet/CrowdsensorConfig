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
    }

    @Override
    public void onResume() {
        super.onResume();
        //Create wifi list
        setAdapter(getView());

        getView().findViewById(R.id.wifiprogress).setVisibility(View.VISIBLE);
        getView().findViewById(R.id.wifilist).setVisibility(View.GONE);
        getView().findViewById(R.id.wifilist).setVisibility(View.GONE);
        //Start scanning for sensors in async task
        startScanning();
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

    public void onStop(){
        super.onStop();
        try {
            getActivity().unregisterReceiver(getWifiScanBroadcastreceiver());
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    //This methods starts scanning and
    //sets the WifiListItemRecyclerAdapter that is used for showing scan results
    private void setAdapter(View view){
        Context context = getActivity();
        RecyclerView recyclerView = view.findViewById(R.id.wifilist);
        if (mColumnCount <= 1) {
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }
        DividerItemDecoration dividerItemDecoration= new DividerItemDecoration(context,
                LinearLayoutManager.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);
        wifiAdapter = new WifiListItemRecyclerViewAdapter(wifiList, mListener);
        recyclerView.setAdapter(wifiAdapter);
        view.findViewById(R.id.wifiprogress).setVisibility(View.VISIBLE);
        view.findViewById(R.id.wifilist).setVisibility(View.GONE);
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
            noWifiFound();
            return;
        }
        //Scan results if not empty show list
        wifiList.clear();

        // Add results to list of wifis
        for(int i = 0; i < results.size(); i++){
            ScanResult result = results.get(i);
            if (!result.SSID.isEmpty() && result.frequency <= Config.WIFI_BANDWIDTH && !result.SSID.startsWith(Config.SENSOR_PREFIX)) {
                wifiList.add(result);
                wifiAdapter.notifyDataSetChanged();
            }
        }
        if(wifiList.isEmpty()){
            noWifiFound();
        } else {
            view.findViewById(R.id.wifiprogress).setVisibility(View.GONE);
            view.findViewById(R.id.wifilist).setVisibility(View.VISIBLE);
            getActivity().setTitle(Config.LISTOFWIFIS_TITLE);
        }
    }

    private void noWifiFound(){
        Snackbar snackbar = Snackbar
                .make(getView(), "Kein Wifi gefunden!", Snackbar.LENGTH_INDEFINITE)
                .setAction("Nochmal versuchen!", (View v)->{
                    WifiManager wifi =(WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    if(!wifi.isWifiEnabled()){
                        //Does this happen?
                        Log.e("ListOfWifisFragment","Wifi nicht aktiviert zum Scannen");
                    }
                    //First stop running scanner
                    SmartWlanConfApplication.getWifiScan(getContext()).stop();
                    getView().findViewById(R.id.wifiprogress).setVisibility(View.VISIBLE);
                    getView().findViewById(R.id.wifilist).setVisibility(View.GONE);
                    startScanning();
                });
        int colorSnackRetry = ResourcesCompat.getColor(getActivity().getResources(), R.color.colorSnackRetry, null);
        snackbar.setActionTextColor(colorSnackRetry);
        snackbar.show();
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
