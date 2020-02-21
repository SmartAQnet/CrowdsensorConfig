package edu.kit.teco.smartwlanconf.ui.utils;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSuggestion;
import android.os.Build;

import androidx.core.app.ActivityCompat;

import com.thanosfisherman.wifiutils.WifiUtils;

import java.util.ArrayList;
import java.util.List;


import edu.kit.teco.smartwlanconf.ui.fragments.WifiFragment;

public class WifiConnectionUtils {

    private static WifiConnectionUtils ourInstance;
    private boolean enabled = false;
    private WifiFragment calling_fragment;
    private WifiManager wifiManager;
    private BroadcastReceiver wifiScanReceiver;

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

    //Only needed to reconnect to network in case that wifi password has changed
    public void resetCurrentWifi(Context context, String ssid, String pwd){
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            final WifiNetworkSuggestion suggestion =
                    new WifiNetworkSuggestion.Builder()
                            .setSsid(ssid)
                            .build();
            List<WifiNetworkSuggestion> suggestions;
            suggestions = new ArrayList<>();
            suggestions.add(suggestion);
            wifiManager.removeNetworkSuggestions(suggestions);
        } else {
            List<WifiConfiguration> list= wifiManager.getConfiguredNetworks();
            WifiConfiguration conf = new WifiConfiguration();
            //conf.SSID = "\"" + ssid + "\"";
            conf.SSID = ssid;
            //conf.preSharedKey = "\""+ pwd +"\"";
            conf.preSharedKey = pwd;
            int networkID = wifiManager.updateNetwork(conf);
            if(networkID>0){
                list = wifiManager.getConfiguredNetworks();
                for(WifiConfiguration i:list){
                    if(i.networkId == networkID){
                        wifiManager.disconnect();
                        //wifiManager.enableNetwork(i.networkId, true);
                        return;
                    }
                }
            } else {
                //Error
                System.out.println("Wifi not reset!");
            }
        }
    }

    public void connectWithWifi_withContext(Context context, String ssid, String pwd, WifiFragment fragment){
        calling_fragment = fragment;
        resetCurrentWifi(context, ssid,pwd);
        WifiUtils.withContext(context)
                .connectWith(ssid, pwd)
                .onConnectionResult(this::checkConnection)
                .start();
    }

    private void checkConnection(boolean isSuccess){
        calling_fragment.onWaitForWifiConnection(isSuccess);
    }

    public void scanWifi(WifiFragment wifiFragment){
        Context context;
        try {
            context = wifiFragment.getActivity().getApplicationContext();
        } catch (Exception e){
            e.printStackTrace();
            return;
        }

        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                boolean success = intent.getBooleanExtra(
                        WifiManager.EXTRA_RESULTS_UPDATED, false);
                scanSuccess(success, wifiFragment);
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        context.registerReceiver(wifiScanReceiver, intentFilter);

        boolean success = wifiManager.startScan();
        if (!success) {
            // scan failure handling
            scanSuccess(success, wifiFragment);
        }
    }

    private void scanSuccess(boolean success, WifiFragment wifiFragment){
        if(success) {
            wifiFragment.onWaitForWifiScan(wifiManager.getScanResults());
            return;
        }
        try {
            wifiFragment.getActivity().unregisterReceiver(wifiScanReceiver);
            wifiFragment.onWaitForWifiScan(null);
        } catch(NullPointerException e){
            e.printStackTrace();
        }
    }

}
