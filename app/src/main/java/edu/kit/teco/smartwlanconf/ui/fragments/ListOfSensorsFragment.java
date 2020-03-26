package edu.kit.teco.smartwlanconf.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import edu.kit.teco.smartwlanconf.R;
import edu.kit.teco.smartwlanconf.SmartWlanConfApplication;
import edu.kit.teco.smartwlanconf.ui.Config;
import edu.kit.teco.smartwlanconf.ui.SmartWlanConfActivity;
import edu.kit.teco.smartwlanconf.ui.adapter.SensorListItemRecyclerViewAdapter;



/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnSensorListInteractionListener}
 * interface.
 *
 * This fragment shows the results of a scan for available wifis
 */
public class ListOfSensorsFragment extends WifiFragment{

    private static final String ARG_COLUMN_COUNT = "Verfügbare Sensoren";
    private int mColumnCount = 1;
    private OnSensorListInteractionListener mListener;
    private SensorListItemRecyclerViewAdapter sensorsAdapter;
    private Snackbar snackbar;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ListOfSensorsFragment() {

    }

    //columncount sets the number of columns used to display wifis
    public static ListOfSensorsFragment newInstance(int columnCount) {
        ListOfSensorsFragment fragment = new ListOfSensorsFragment();
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
        return inflater.inflate(R.layout.sensor_item_list_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();

        //Create list of Sensors
        setAdapter(getView());

        //Show splash screen
        LinearLayout splash = getView().findViewById(R.id.splash);
        RecyclerView list = getView().findViewById(R.id.wifilist);
        splash.setVisibility(View.VISIBLE);
        list.setVisibility(View.GONE);
        //Change title to tell user what to do
        getActivity().setTitle(Config.LISTOFSENSORS_TITLE);
        startScanning(this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSensorListInteractionListener) {
            mListener = (OnSensorListInteractionListener) context;
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

    @Override
    public void onStop() {
        super.onStop();
        if(snackbar != null){
            snackbar.dismiss();
        }
        stopScanning(this );
    }

    //This methods starts scanning and
    //sets the SensorListItemRecyclerAdapter that is used for showing scan results
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
        sensorsAdapter = new SensorListItemRecyclerViewAdapter(getWifiList(), mListener);
        recyclerView.setAdapter(sensorsAdapter);
    }

    //Callback method, when wifi scan returns it's results
    @Override
    public void onWaitForWifiScan(@Nullable List<ScanResult> results) {

        super.onWaitForWifiScan(results);

        View view = getView();
        if (view == null) {
            Log.d(ListOfSensorsFragment.class.toString(), "view is null in onWaitForWifiScan()");
            return;
        }
        Activity activity = getActivity();
        if (activity == null) {
            Log.d(ListOfSensorsFragment.class.toString(), "activity is null in onWaitForWifiScan()");
            return;
        }

        if (results == null) {
            noSensorFound();
            return;
        }

        showResults(results);
    }

    private void showResults(List<ScanResult>results){
        //Scan results if not empty show list
        LinearLayout splash = getView().findViewById(R.id.splash);
        RecyclerView list = getView().findViewById(R.id.wifilist);
        splash.setVisibility(View.GONE);
        list.setVisibility(View.VISIBLE);
        getWifiList().clear();

        // Add results to list of wifis
        for(int i = 0; i < results.size(); i++){
            ScanResult result = results.get(i);
            if (!result.SSID.isEmpty() && result.frequency <= Config.WIFI_BANDWIDTH && result.SSID.startsWith(Config.SENSOR_PREFIX)) {
                getWifiList().add(result);
                sensorsAdapter.notifyDataSetChanged();
            }
        }
        if(getWifiList().isEmpty()) noSensorFound();
    }

    private void noSensorFound(){
        snackbar = Snackbar
                .make(getView(), "Kein Sensor gefunden!", Snackbar.LENGTH_INDEFINITE)
                .setAction("Nochmal versuchen!", (View v)->{
                    WifiManager wifi =(WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    if(!wifi.isWifiEnabled()){
                        //Does this happen?
                        Log.e("ListOfWifisFragment","Wifi nicht aktiviert zum Scannen");
                    }
                    //Show splash screen
                    LinearLayout splash = getView().findViewById(R.id.splash);
                    RecyclerView list = getView().findViewById(R.id.wifilist);
                    splash.setVisibility(View.VISIBLE);
                    list.setVisibility(View.GONE);
                    startScanning(this);
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
    public interface OnSensorListInteractionListener {
        void onSensorListInteraction(ScanResult scanResult);
    }
}
