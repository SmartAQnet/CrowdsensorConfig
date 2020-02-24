package edu.kit.teco.smartwlanconf.ui.utils;

import edu.kit.teco.smartwlanconf.ui.fragments.WifiFragment;

public class WifiScanRunnable implements Runnable{
        private volatile boolean running = true;
        private volatile boolean hasStarted = false;
        private WifiConnectionUtils wifi;
        private WifiFragment wifiFragment;
        public WifiScanRunnable(WifiFragment wifiFragment, WifiConnectionUtils wifi){
            this.wifiFragment = wifiFragment;
            this.wifi = wifi;
        }

        public void run()
        {
            while(running){
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
