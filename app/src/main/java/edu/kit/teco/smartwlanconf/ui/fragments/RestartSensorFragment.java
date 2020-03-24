package edu.kit.teco.smartwlanconf.ui.fragments;


import android.app.Activity;
import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

import java.math.BigInteger;
import java.net.InetAddress;

import java.util.HashMap;

import edu.kit.teco.smartwlanconf.R;
import edu.kit.teco.smartwlanconf.ui.Config;
import edu.kit.teco.smartwlanconf.ui.SmartWlanConfActivity;
import edu.kit.teco.smartwlanconf.ui.utils.HttpSensorPost;

import static android.content.Context.WIFI_SERVICE;

/**
 * A simple {@link Fragment} subclass.
 * When this fragment is calles the Users phone is now connected to the sensors wifi.
 *
 * This fragment sends the wifi credentials of the user wifi to the sensor
 * which is then restarted. The users phone is then reconnected to the
 * users wifi, after successful reconnection ShowSensorWebsiteFragment is called.
 */
public class RestartSensorFragment extends WifiFragment {

    private OnSensorRestartedListener mListener;
    private Snackbar snackbar;

    public RestartSensorFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.restart_sensor_fragment, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().setTitle(Config.APP_TITLE);
        //Connect to sensor wifi
        connectToWifi(((SmartWlanConfActivity)getActivity()).getmSensorSSID(),
                Config.SENSOR_PWD,
                this);
    }

    @Override
    public void onStop() {
        super.onStop();
        if(snackbar != null) {
            snackbar.dismiss();
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnSensorRestartedListener) {
            mListener = (OnSensorRestartedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    //connect to sensor wifi
    @Override
    public void onWaitForWifiConnection(boolean success){
        //Returning from Async call, check if view is still active
        //If not working check if setting a destroyed tag in onDetach() is a solution
        View view = getView();
        if(view == null){
            //Has to be tested if a simple return produces no errors, or an Exception has to be thrown
            return;
        }
        if (success) {
            //Send wifi credentials to sensor and restart it
            connectSensorWithUserWifi();
        } else {
            snackbar = Snackbar
                    .make(view, "Verbindung zu Sensor fehlgeschlagen", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Nochmal versuchen!", (View v)->{
                        connectToWifi(((SmartWlanConfActivity)getActivity()).getmSensorSSID(),
                                Config.SENSOR_PWD,
                                this);
                    });
            int colorSnackRetry = ResourcesCompat.getColor(getActivity().getResources(), R.color.colorSnackRetry, null);
            snackbar.setActionTextColor(colorSnackRetry);
            snackbar.show();
        }
    }


    //Sending wifi credentials with http to sensor restarts it
    private void connectSensorWithUserWifi(){

        Activity activity = getActivity();
        //As the user's phone is connected to the wifi of the sensor
        //it's IP is the gateway IP, so you have to look for it
        String gatewayIP;
        try {
            gatewayIP = lookupGateway(activity);
        } catch (NullPointerException e) {
            Log.d(RestartSensorFragment.class.toString(), "lookupGateway() returned null");
            return;
        }

        HttpSensorPost request = new HttpSensorPost(activity.getApplicationContext());
        try {
            //URL to send wifi credentials
            final String wlanUrl = "http://" + gatewayIP + "/_ac/connect";
            //Set credentials
            HashMap<String, String> credentials = getSensorWifiCredentials(activity);
            //Send Data via http
            boolean result = request.execute(wlanUrl,
                    credentials.get("SSID"),
                    credentials.get("PWD")).get();
            //Check if credentials could be sent
            if(!result){
                int colorSnackRetry = ResourcesCompat.getColor(activity.getResources(), R.color.colorSnackRetry, null);

                snackbar = Snackbar.make(getView(), "Wifi Daten konnten nicht an Knoten geschickt werden!", Snackbar.LENGTH_INDEFINITE);
                snackbar.setAction("Nochmal versuchen!", (View v) -> this.connectSensorWithUserWifi())
                        .setActionTextColor(colorSnackRetry)
                        .show();
            } else {
                mListener.onSensorRestartedSuccess();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    //Gets Gateway IP, which is the ip adress of the sensor
    private String lookupGateway(Activity activity) throws NullPointerException {
        final WifiManager manager = (WifiManager) activity.getApplicationContext().getSystemService(WIFI_SERVICE);
        final DhcpInfo dhcp = manager.getDhcpInfo();
        byte[] ipAdress = BigInteger.valueOf(dhcp.gateway).toByteArray();
        //IpAdress has to be reversed
        for(int i=0; i<ipAdress.length/2; i++){
            Byte temp = ipAdress[i];
            ipAdress[i] = ipAdress[ipAdress.length -i -1];
            ipAdress[ipAdress.length -i -1] = temp;
        }
        try {
            //ipAdress to String
            return (InetAddress.getByAddress(ipAdress)).getHostAddress();
        } catch (Exception e){
            e.printStackTrace();
            throw new NullPointerException();
        }
    }

    //Put wifi credentials in a Hashmap
    private HashMap<String, String> getSensorWifiCredentials(Activity activity){
        HashMap<String, String> credentials = new HashMap<>();
        credentials.put("SSID", ((SmartWlanConfActivity) activity).getmWlanSSID());
        credentials.put("PWD",((SmartWlanConfActivity) activity).getmWlanPwd());
        return credentials;
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
    public interface OnSensorRestartedListener {
        void onSensorRestartedSuccess();
    }
}
