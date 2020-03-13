package edu.kit.teco.smartwlanconf.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.view.View;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import edu.kit.teco.smartwlanconf.R;
import edu.kit.teco.smartwlanconf.SmartWlanConfApplication;
import edu.kit.teco.smartwlanconf.ui.adapter.SensorListItemRecyclerViewAdapter;
import edu.kit.teco.smartwlanconf.ui.utils.WifiConnectionUtils;
import edu.kit.teco.smartwlanconf.ui.utils.WifiScanRunnable;

//This class is the superclass of ListOfWifisFragment
//It is only used to declare the callback methods used in ListOfWifisFragment
public class WifiFragment extends Fragment {

    //Method that returns to onWaitForWifiConnection
    public void connectToWifi(Activity activity, View view, String ssid, String pwd, WifiFragment fragment){}
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
}
