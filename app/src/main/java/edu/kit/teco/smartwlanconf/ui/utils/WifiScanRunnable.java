package edu.kit.teco.smartwlanconf.ui.utils;

import edu.kit.teco.smartwlanconf.ui.SmartWlanConfActivity;
import edu.kit.teco.smartwlanconf.ui.fragments.WifiFragment;

//This is the runnable used to run a concurrent wifiscan
public class WifiScanRunnable implements Runnable{
        private volatile boolean running = true;
        private volatile boolean hasStarted = false;
        private SmartWlanConfActivity activity;
        private WifiFragment wifiFragment;
        private WifiConnectionUtils wifi;

        //Set fragment that calls the scanner and attribute used to start scan
        public WifiScanRunnable(WifiFragment fragment, WifiConnectionUtils wifi){
            this.activity = (SmartWlanConfActivity) fragment.getActivity();
            this.wifiFragment = fragment;
            this.wifi = wifi;
        }

        public void run()
        {
            while(running){
                //Start scan only once
                if(!hasStarted) {
                    hasStarted = true;
                    wifi.scanWifi(wifiFragment, activity);
                }
            }
        }

        public void stop(){
            running = false;
            hasStarted = false;
        }
}
