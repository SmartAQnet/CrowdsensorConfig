package edu.kit.teco.smartwlanconf.ui.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.view.View;

import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.kit.teco.smartwlanconf.R;
import edu.kit.teco.smartwlanconf.SmartWlanConfApplication;
import edu.kit.teco.smartwlanconf.ui.SmartWlanConfActivity;
import edu.kit.teco.smartwlanconf.ui.adapter.SensorListItemRecyclerViewAdapter;
import edu.kit.teco.smartwlanconf.ui.utils.WifiConnectionUtils;
import edu.kit.teco.smartwlanconf.ui.utils.WifiScanRunnable;

//This class is the superclass of ListOfWifisFragment
//It is only used to declare the callback methods used in ListOfWifisFragment
public class WifiFragment extends Fragment {

    //Callback method to wait for a wifi connection
    public void onWaitForWifiConnection(boolean success){}
    //Callback method to wait for wifi scan
    public void onWaitForWifiScan(List<ScanResult> wifiList){}

    public void startScanning(){
        WifiConnectionUtils wifi = SmartWlanConfApplication.getWifi(getContext());
        //create wifiscanner
        WifiScanRunnable wifiScan = new WifiScanRunnable(this, wifi);
        //remember wifiscanner to be able to stop scanning
        SmartWlanConfApplication.setWifiScan(getContext(), wifiScan);
        Thread t = new Thread(SmartWlanConfApplication.getWifiScan(getContext()));
        t.start();
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
                .connectWithWifi_withContext(activity, ((SmartWlanConfActivity) activity).getmWlanSSID(), ((SmartWlanConfActivity) activity).getmWlanPwd(), fragment);
    }

}
