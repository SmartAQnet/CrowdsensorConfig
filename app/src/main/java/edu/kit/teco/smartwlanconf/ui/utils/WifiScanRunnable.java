package edu.kit.teco.smartwlanconf.ui.utils;

import android.content.BroadcastReceiver;

import com.thanosfisherman.wifiutils.wifiScan.WifiScanReceiver;

import edu.kit.teco.smartwlanconf.SmartWlanConfApplication;
import edu.kit.teco.smartwlanconf.ui.fragments.WifiFragment;

//This is the runnable used to run a concurrent wifiscan
public class WifiScanRunnable implements Runnable{
        private volatile boolean running = true;
        private volatile boolean hasStarted = false;
        private WifiFragment wifiFragment;

        //Set fragment that calls the scanner and attribute used to start scan
        public WifiScanRunnable(WifiFragment wifiFragment){
            this.wifiFragment = wifiFragment;
        }

        public void run()
        {
            while(running){
                //Start scan only once
                if(!hasStarted) {
                    hasStarted = true;
                    SmartWlanConfApplication.getWifi(wifiFragment.getContext()).scanWifi(wifiFragment);
                }
            }
        }

        public void stop(){
            running = false;
            hasStarted = false;
        }
}
