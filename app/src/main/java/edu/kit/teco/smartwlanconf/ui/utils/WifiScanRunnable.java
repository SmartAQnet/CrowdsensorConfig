package edu.kit.teco.smartwlanconf.ui.utils;

import edu.kit.teco.smartwlanconf.ui.fragments.WifiFragment;

//This is the runnable used to run a concurrent wifiscan
public class WifiScanRunnable implements Runnable{
        private volatile boolean running = true;
        private volatile boolean hasStarted = false;
        private WifiConnectionUtils wifi;
        private WifiFragment wifiFragment;

        //Set fragment that calls the scanner and attribute used to start scan
        public WifiScanRunnable(WifiFragment wifiFragment, WifiConnectionUtils wifi){
            this.wifiFragment = wifiFragment;
            this.wifi = wifi;
        }

        public void run()
        {
            while(running){
                //Start scan only once
                if(!hasStarted) {
                    wifi.scanWifi(wifiFragment);
                    hasStarted = true;
                }
            }
        }

        public void stop(){
            running = false;
        }
}
