package edu.kit.teco.smartwlanconf.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import edu.kit.teco.smartwlanconf.R;
import edu.kit.teco.smartwlanconf.ui.Config;
import edu.kit.teco.smartwlanconf.ui.SmartWlanConfActivity;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SensorNotFoundFragment.OnAfterSensorNotFound} interface
 * to handle interaction events.
 * Use the {@link SensorNotFoundFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SensorNotFoundFragment extends WifiFragment{

    private OnAfterSensorNotFound mListener;
    private Snackbar snackbar;

    public SensorNotFoundFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.

     * @return A new instance of fragment SensorNotFound.
     *
     * When this fragment is opened, sensor could not be found in user's wifi
     *
     * Check if sensor has reopened his own wifi, by trying to connect to it => Password for user wifi was wrong
     * Otherwise mDNS lookup was not successful, user has to look for IP-adress of sensor on it's display
     */
    public static SensorNotFoundFragment newInstance() {
        SensorNotFoundFragment fragment = new SensorNotFoundFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.sensor_not_found_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        setCheckSensorIPButtonListener(getView());
        getView().findViewById(R.id.progress_check_sensor_wifi_after_failed_connection).setVisibility(View.VISIBLE);
        getView().findViewById(R.id.ask_for_sensor_ip).setVisibility(View.GONE);
        startScanning(this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnAfterSensorNotFound) {
            mListener = (OnAfterSensorNotFound) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(snackbar != null){
            snackbar.dismiss();
        }
        stopScanning(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    /**
     * If scan for sensor is not successful then mDNS lookup failed => ask user for IP-address of sensor
     * If scan for sensor is successful then wifi password was wrong => open GetUserWifiCredentialsFragment
     */
    @Override
    public void onWaitForWifiScan(@Nullable List<ScanResult> results) {

        super.onWaitForWifiScan(results);

        View view = getView();
        if (view == null) {
            Log.d(ListOfWifisFragment.class.toString(), "view is null in onWaitForWifiScan()");
            return;
        }
        Activity activity = getActivity();
        if (activity == null) {
            Log.d(ListOfWifisFragment.class.toString(), "activity is null in onWaitForWifiScan()");
            return;
        }

        //No results
        if (results == null) {
            noWifiFound();
            return;
        }

        checkResult(results);
    }

    private void checkResult(List<ScanResult> results){
        boolean wrongPassword = false;

        // Look for SSID of sensor
        for(int i = 0; i < results.size(); i++){
            ScanResult result = results.get(i);
            if (!result.SSID.isEmpty()
                    && result.frequency <= Config.WIFI_BANDWIDTH
                    && result.SSID.equals(((SmartWlanConfActivity)getActivity()).getmSensorSSID())) {
                wrongPassword = true;
                //Open GetUserWifiCredentialsFragment
                mListener.onAfterSensorNotFound(true);
            }
        }
        if(!wrongPassword) {
            getView().findViewById(R.id.progress_check_sensor_wifi_after_failed_connection).setVisibility(View.GONE);
            getView().findViewById(R.id.ask_for_sensor_ip).setVisibility(View.VISIBLE);
        }
    }

    private void noWifiFound(){
        int colorSnackRetry = ResourcesCompat.getColor(getActivity().getResources(), R.color.colorSnackRetry, null);
        snackbar = Snackbar.make(getView(), "Keine Wifi gefunden. Wifi aktiv?", Snackbar.LENGTH_INDEFINITE)
                .setAction("Neue Suche!", (View v)->{
                    getView().findViewById(R.id.progress_check_sensor_wifi_after_failed_connection).setVisibility(View.VISIBLE);
                    getView().findViewById(R.id.ask_for_sensor_ip).setVisibility(View.GONE);
                    startScanning(this);
                });
        snackbar.setActionTextColor(colorSnackRetry);
        snackbar.show();
    }

    //Button to get Sensor id/ssid
    private void setCheckSensorIPButtonListener(View view){
        //Read sensor id/ssid from input
        view.findViewById(R.id.btn_check_ip).setOnClickListener((View v)-> {
            IPTest ipTest = new IPTest();
            String uri = "http://" + ((EditText) getView().findViewById(R.id.sensor_ip)).getText().toString();
            try {
                URL url = new URL(uri);
                boolean isIPReachable = ipTest.execute(url).get();
                if(isIPReachable){
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    startActivity(browserIntent);
                    //Open first page of app
                    mListener.onAfterSensorNotFound(false);
                } else {
                    int colorSnackRetry = ResourcesCompat.getColor(getActivity().getResources(), R.color.colorSnackRetry, null);
                    snackbar = Snackbar.make(view, "IP-Adresse nicht erreichbar. Bitte prüfen?", Snackbar.LENGTH_LONG);
                    snackbar.setActionTextColor(colorSnackRetry);
                    snackbar.show();
                }
            } catch (MalformedURLException urle){
                urle.printStackTrace();
            } catch(Exception e){
                e.printStackTrace();
            }
        });
    }


    class IPTest extends AsyncTask<URL, Void, Boolean>{
        @Override
        protected Boolean doInBackground(URL... urls) {
            ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnected()) {
                try {
                    HttpURLConnection urlc = (HttpURLConnection) urls[0].openConnection();
                    urlc.setConnectTimeout(10 * 1000);          // 10 s.
                    urlc.connect();
                    //Just look if there is a response code
                    if (urlc.getResponseCode() != -1) {
                        Log.wtf("Connection", "Success !");
                        return true;
                    } else {
                        return false;
                    }
                } catch (IOException e) {
                    return false;
                }
            }
            return false;
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnAfterSensorNotFound {
        void onAfterSensorNotFound(boolean wrongPassword);
    }
}