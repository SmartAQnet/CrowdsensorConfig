package edu.kit.teco.smartwlanconf.ui.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSuggestion;
import android.os.Build;

import androidx.lifecycle.Lifecycle;

import com.thanosfisherman.wifiutils.WifiUtils;

import java.util.ArrayList;
import java.util.List;


import edu.kit.teco.smartwlanconf.SmartWlanConfApplication;
import edu.kit.teco.smartwlanconf.ui.fragments.WifiFragment;

public class WifiConnectionUtils {

    private static WifiConnectionUtils ourInstance;
    private boolean enabled = false;
    private WifiFragment calling_fragment;
    private WifiManager wifiManager;

    public static WifiConnectionUtils getInstance() {

        if(ourInstance == null){
            ourInstance = new WifiConnectionUtils();
        }

        return ourInstance;
    }

    private WifiConnectionUtils() { }

    public boolean enableWifi(Context context){
        WifiUtils.withContext(context).enableWifi(this::checkResult);
        return enabled;
    }

    private void checkResult(boolean isSuccess){
        enabled = isSuccess;
    }

    // Passwords for wifi cannot be tested, if there is a valid wifi configuration on the smartphone
    // For Android Versions below Q Google says:
    //https://developer.android.com/about/versions/marshmallow/android-6.0-changes.html dazu:
    //
    //Wi-Fi and Networking Changes
    //This release introduces the following behavior changes to the Wi-Fi and networking APIs.
    //
    //Your apps can now change the state of WifiConfiguration objects only if you created these objects.
    //You are not permitted to modify or delete WifiConfiguration objects created by the user or by other apps.
    // So at the moment the only solution below Android Q is to request the user to remove the configured network
    // by which he want to connect. This method then checks if the network is removed by user
    // The Code for Android from Q and upwards just tries to remove the configured network, so that the password
    // can be checked when reconnect to it

    private void checkCurrentWifi(String ssid){
        //This code could actually not be tested so far because there is no smartphon available with android Q
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            final WifiNetworkSuggestion suggestion =
                    new WifiNetworkSuggestion.Builder()
                            .setSsid(ssid)
                            .build();
            List<WifiNetworkSuggestion> suggestions;
            suggestions = new ArrayList<>();
            suggestions.add(suggestion);
            wifiManager.removeNetworkSuggestions(suggestions);
            //wifiManager.addNetworkSuggestions(suggestions);
        } else {
            for(WifiConfiguration i:wifiManager.getConfiguredNetworks()){
                if(i.SSID.equals(ssid)){
                    // Network is not removed by user, not yet determined what should be done
                    return;
                }
            }
        }
    }

    //Connects to wifi with given credentials -- bssid not supported
    public void connectWithWifi_withContext(Context context, String ssid, String pwd, WifiFragment fragment){
        //Usage of this method should be further evaluated
        checkCurrentWifi(ssid);
        // Set calling fragment for callback
        calling_fragment = fragment;
        WifiUtils.withContext(context)
                .connectWith(ssid, pwd)
                .onConnectionResult(this::receiveConnectionResult)
                .start();
    }

    //isSuccess is true if connection to wifi is established
    private void receiveConnectionResult(boolean isSuccess){
        //return to calling fragment
        calling_fragment.onWaitForWifiConnection(isSuccess);
    }


    //Scans for available wifis, result of scan is received through broadcast
    void scanWifi(WifiFragment wifiFragment){
        Context context;
        try {
            context = wifiFragment.getActivity().getApplicationContext();
        } catch (NullPointerException e){
            e.printStackTrace();
            return;
        }

        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        //Receiver for Wifi Scan
        wifiFragment.setWifiScanBroadcastreceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                boolean success = intent.getBooleanExtra(
                        WifiManager.EXTRA_RESULTS_UPDATED, false);
                scanSuccess(success, wifiFragment);
            }
        });

        try {
            context.registerReceiver(wifiFragment.getWifiScanBroadcastreceiver(), SmartWlanConfApplication.getWifiscanIntentfilter(context));
        } catch (Exception e) {
            e.printStackTrace();
        }

        //Start Scanning
        boolean success = wifiManager.startScan();
        if (!success) {
            // scan failure handling
            scanSuccess(success, wifiFragment);
        }
    }

    //Report scanresults back to calling fragment
    private void scanSuccess(boolean success, WifiFragment wifiFragment) {
        if (success) {
                wifiFragment.onWaitForWifiScan(wifiManager.getScanResults());
        } else {
            wifiFragment.onWaitForWifiScan(null);
        }
    }
}
