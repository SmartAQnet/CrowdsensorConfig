package edu.kit.teco.smartwlanconf.ui.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;


import java.util.ArrayList;
import java.util.List;

import edu.kit.teco.smartwlanconf.SmartWlanConfApplication;
import edu.kit.teco.smartwlanconf.ui.utils.WifiScanRunnable;

//This class is the superclass of ListOfWifisFragment
//It is only used to declare the callback methods used in ListOfWifisFragment
public class WifiFragment extends Fragment {

    private BroadcastReceiver wifiScanBroadcastreceiver;
    private IntentFilter wifiscanIntentFilter;
    private List<ScanResult> wifiList = new ArrayList<>();

    public WifiFragment(){
        //This is the intent that reports scan results
        wifiscanIntentFilter = new IntentFilter();
        wifiscanIntentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
    }

    //Callback method to wait for a wifi connection
    public void onWaitForWifiConnection(boolean success){
        Log.d(WifiFragment.class.toString(), "Just a stub, no need to call!");
    }

    //Callback method to wait for wifi scan
    public void onWaitForWifiScan(@Nullable List<ScanResult> wifiList) {
        stopScanning(this);
    }

    public void startScanning(WifiFragment fragment) {
        WifiScanRunnable wifiScanRunnable;
        // Create wifiscanner
        wifiScanRunnable = new WifiScanRunnable(fragment, SmartWlanConfApplication.getWifi(getContext()), wifiscanIntentFilter);
        //remember wifiscanner to be able to stop scanning
        (new Thread(wifiScanRunnable)).start();
    }

    public void stopScanning(WifiFragment wifiFragment){
        // Try to stop scanning, otherwise scanning will keep on sending results to
        // Fragment that has already been stopped
        try{
            wifiFragment.getActivity().getApplication().unregisterReceiver(wifiFragment.getWifiScanBroadcastreceiver());
        } catch (Exception e){
            //Nothing to do
            Log.d(WifiScanRunnable.class.toString(),"Unable to unregister " + wifiFragment.getClass().toString());
        }
    }

    //Connects to given wifi
    @RequiresPermission(Manifest.permission.CHANGE_WIFI_STATE)
    public void connectToWifi(String ssid, String pwd, WifiFragment fragment){
        Activity activity = getActivity();
        if(activity == null){
            //Has to be tested if a simple return produces no errors, or if an Exception has to be thrown
            return;
        }
        //Some phones seems to loose Permission to Change Wifi state
        //Check here and see if it helps
        PackageManager pm = activity.getPackageManager();
        int hasPerm = pm.checkPermission(
                Manifest.permission.CHANGE_WIFI_STATE,
                activity.getPackageName());
        if (hasPerm != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CHANGE_WIFI_MULTICAST_STATE}, 556);
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CHANGE_WIFI_STATE}, 557);
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CHANGE_NETWORK_STATE}, 558);
        }
        //reconnect to user wifi
        SmartWlanConfApplication
                .getWifi(activity)
                .connectWithWifi_withContext(activity, ssid, pwd, fragment);
    }

    public BroadcastReceiver getWifiScanBroadcastreceiver(){
        return wifiScanBroadcastreceiver;
    }

    public void setWifiScanBroadcastreceiver(BroadcastReceiver br){
        wifiScanBroadcastreceiver = br;
    }

    public List<ScanResult> getWifiList(){
        return wifiList;
    }
}
