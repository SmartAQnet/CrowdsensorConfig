package edu.kit.teco.smartwlanconf.ui.fragments;

import android.app.Activity;
import android.net.wifi.ScanResult;
import android.view.View;

import androidx.fragment.app.Fragment;

import java.util.List;

//This class is the superclass of ListOfWifisFragment
//It is only used to declare the callback methods used in ListOfWifisFragment
public class WifiFragment extends Fragment {

    //Method that returns to onWaitForWifiConnection
    public void connectToWifi(Activity activity, View view, String ssid, String pwd, WifiFragment fragment){}
    //Callback method to wait for a wifi connection
    public void onWaitForWifiConnection(boolean success){}
    //Callback method to wait for wifi scan
    public void onWaitForWifiScan(List<ScanResult> wifiList){}

}
