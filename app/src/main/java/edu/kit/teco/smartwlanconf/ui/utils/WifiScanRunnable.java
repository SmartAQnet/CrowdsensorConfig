package edu.kit.teco.smartwlanconf.ui.utils;

import android.content.IntentFilter;
import android.util.Log;

import edu.kit.teco.smartwlanconf.ui.SmartWlanConfActivity;
import edu.kit.teco.smartwlanconf.ui.fragments.WifiFragment;

//This is the runnable used to run a concurrent wifiscan
public class WifiScanRunnable implements Runnable{

        private WifiFragment wifiFragment;
        private WifiConnectionUtils wifi;
        private IntentFilter wifiScanIntentFilter;

        //Set fragment that calls the scanner and attribute used to start scan
        public WifiScanRunnable(WifiFragment fragment, WifiConnectionUtils wifi, IntentFilter wifiScanIntentFilter){
            this.wifiFragment = fragment;
            this.wifi = wifi;
            this.wifiScanIntentFilter = wifiScanIntentFilter;
        }

        public void run()
        {
            wifi.scanWifi(wifiFragment, wifiScanIntentFilter);
        }
}
