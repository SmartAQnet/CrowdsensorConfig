package edu.kit.teco.smartwlanconf.ui.fragments;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import edu.kit.teco.smartwlanconf.R;
import edu.kit.teco.smartwlanconf.SmartWlanConfApplication;
import edu.kit.teco.smartwlanconf.ui.SmartWlanConfActivity;

import static androidx.constraintlayout.widget.Constraints.TAG;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnCheckUserWifiCredentialsSuccessListener} interface
 * to handle interaction events.
 * Use the {@link CheckUserWifiCredentialsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CheckUserWifiCredentialsFragment extends WifiFragment {

    private static final String ARG_SSID = "SSID";
    private OnCheckUserWifiCredentialsSuccessListener mListener;
    private NsdManager.DiscoveryListener discoveryListener;
    private NsdManager nsdManager;
    private boolean firstTime;

    public CheckUserWifiCredentialsFragment() {
        // Required empty public constructor
    }

    public CheckUserWifiCredentialsFragment(boolean firstTime) {
        this.firstTime = firstTime;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param context, firstTime
     * @return A new instance of fragment CheckUserWifiCredentialsFragment.
     *
     *
     * If it's not the first time a new fragment is created, reason is that the node has not been found in user wifi
     * Most likely the user has given wrong password for wifi
     */
    public static CheckUserWifiCredentialsFragment newInstance(Context context, boolean firstTime) {
        CheckUserWifiCredentialsFragment fragment = new CheckUserWifiCredentialsFragment(firstTime);
        Bundle args = new Bundle();
        args.putString(ARG_SSID, SmartWlanConfApplication.getUserWifiSSID(context));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            SmartWlanConfActivity activity = (SmartWlanConfActivity) getActivity();
            if(activity == null){
                //Has to be tested if a simple return produces no errors, or an Exception has to be thrown
                return;
            }
            //Set Wlan SSID
            ((SmartWlanConfActivity) getActivity()).setmWlanSSID(getArguments().getString(ARG_SSID));
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if(!firstTime){
            //Todo: Show Hint that password might have been wrong
        }
        return inflater.inflate(R.layout.wifi_check_credentials_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SmartWlanConfActivity activity = (SmartWlanConfActivity) getActivity();
        if(activity == null){
            //Has to be tested if a simple return produces no errors, or an Exception has to be thrown
            return;
        }
        ((EditText) view.findViewById(R.id.ssid)).setText(activity.getmWlanSSID());
        setConnectButtonListener(view, activity);
    }

    @Override
    public void onResume() {
        super.onResume();
        //Hide Progress Bar
        getView().findViewById(R.id.getusercredentials).setVisibility(View.VISIBLE);
        getView().findViewById(R.id.checkingWifi).setVisibility(View.GONE);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnCheckUserWifiCredentialsSuccessListener) {
            mListener = (OnCheckUserWifiCredentialsSuccessListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void setConnectButtonListener(View view, SmartWlanConfActivity activity){

        final Button connectButton = view.findViewById(R.id.btnConnect);
        connectButton.setOnClickListener((View v)-> {
            //Hide screen keyboard
            InputMethodManager inputManager = (InputMethodManager)
                    v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow((null == activity.getCurrentFocus()) ?
                    null : activity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            //Show Progress Bar
            getView().findViewById(R.id.getusercredentials).setVisibility(View.GONE);
            getView().findViewById(R.id.checkingWifi).setVisibility(View.VISIBLE);

            String pwd = ((EditText) view.findViewById(R.id.pwd)).getText().toString();
            //Set  Wlan Password in Parent Activity
            activity.setmWlanPwd(pwd);
            SmartWlanConfApplication
                    .getWifi(activity)
                    .connectWithWifi_withContext(activity.getApplicationContext(), activity.getmWlanSSID(), pwd, this);
        });
    }


    public void initializeDiscoveryListener() {

        // Instantiate a new DiscoveryListener
        NsdManager.DiscoveryListener discoveryListener = new NsdManager.DiscoveryListener() {

            // Called as soon as service discovery begins.
            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                // A service was found! Do something with it.
                Log.d(TAG, "Service discovery success" + service);
                /*if (!service.getServiceType().equals(SERVICE_TYPE)) {
                    // Service type is the string containing the protocol and
                    // transport layer for this service.
                    Log.d(TAG, "Unknown Service Type: " + service.getServiceType());
                } else if (service.getServiceName().equals(serviceName)) {
                    // The name of the service tells the user what they'd be
                    // connecting to. It could be "Bob's Chat App".
                    Log.d(TAG, "Same machine: " + serviceName);
                } else if (service.getServiceName().contains("NsdChat")){
                    nsdManager.resolveService(service, resolveListener);
                }*/
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                // When the network service is no longer available.
                // Internal bookkeeping code goes here.
                Log.e(TAG, "service lost: " + service);
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(TAG, "Discovery stopped: " + serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                nsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                nsdManager.stopServiceDiscovery(this);
            }
        };
    }


    public void onWaitForWifiConnection (Boolean success){
        //Returning from Async call, check if view is still active
        //If not working check if setting a destroyed tag in onDetach() is a solution
        View view = getView();
        if(view == null){
            //Has to be tested if a simple return produces no errors, or an Exception has to be thrown
            return;
        }

        if(success){
            if (mListener != null) {
                Toast.makeText(view.getContext().getApplicationContext()
                        ,"Verbunden mit " + ((EditText) view.findViewById(R.id.ssid)).getText().toString()
                        ,Toast.LENGTH_LONG).show();
                mListener.onCheckUserWifiCredentialsSuccess();
            }
        } else {
            //Hide Progress Bar
            getView().findViewById(R.id.getusercredentials).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.checkingWifi).setVisibility(View.GONE);
            Snackbar snackbar = Snackbar
                    .make(view, "Wifi Verbindung fehlgeschlagen, falsches Passwort?", Snackbar.LENGTH_LONG);
            snackbar.show();
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
    public interface OnCheckUserWifiCredentialsSuccessListener {
        void onCheckUserWifiCredentialsSuccess();
    }
}
