package edu.kit.teco.smartwlanconf.ui.fragments;

import android.net.wifi.ScanResult;

import androidx.fragment.app.Fragment;

import java.util.List;

//This class is the superclass of ListOfWifisFragment
//It is only used to declare the callback methods used in ListOfWifisFragment
public class WifiFragment extends Fragment {

    //Callback method to wait for a wifi connection
    public void onWaitForWifiConnection(Boolean success){}
    //Callback method wifi scan
    public void onWaitForWifiScan(List<ScanResult> wifiList){}

}
