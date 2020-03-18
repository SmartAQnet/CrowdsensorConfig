package edu.kit.teco.smartwlanconf.ui.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;

import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;


import java.util.List;

import edu.kit.teco.smartwlanconf.SmartWlanConfApplication;
import edu.kit.teco.smartwlanconf.ui.SmartWlanConfActivity;
import edu.kit.teco.smartwlanconf.ui.utils.WifiConnectionUtils;
import edu.kit.teco.smartwlanconf.ui.utils.WifiScanRunnable;

//This class is the superclass of ListOfWifisFragment
//It is only used to declare the callback methods used in ListOfWifisFragment
public class WifiFragment extends Fragment {

    private BroadcastReceiver wifiScanBroadcastreceiver;

    //Callback method to wait for a wifi connection
    public void onWaitForWifiConnection(boolean success){}
    //Callback method to wait for wifi scan
    public void onWaitForWifiScan(List<ScanResult> wifiList){}

    public void startScanning(WifiFragment fragment) {
        //create wifiscanner
        WifiScanRunnable wifiScan = new WifiScanRunnable(fragment, SmartWlanConfApplication.getWifi(getContext()));
        //remember wifiscanner to be able to stop scanning
        SmartWlanConfApplication.setWifiScan(getContext(), wifiScan);
        Thread t = new Thread(SmartWlanConfApplication.getWifiScan(getContext()));
        t.start();
    }

    public void stopScanning(SmartWlanConfActivity activity){
        //Stop wifi scannning thread
        (SmartWlanConfApplication.getWifiScan(activity)).stop();
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

}
