package edu.kit.teco.smartwlanconf.ui.fragments;

import android.net.wifi.ScanResult;
import android.view.View;

import androidx.fragment.app.Fragment;

import java.util.List;

public class WifiFragment extends Fragment {

    //Callback method to wait for a wifi connection
    public void onWaitForWifiConnection(Boolean success){};
    public void onWaitForWifiScan(List<ScanResult> wifiList){};

}
