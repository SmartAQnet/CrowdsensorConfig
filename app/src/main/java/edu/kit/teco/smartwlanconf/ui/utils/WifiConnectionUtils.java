package edu.kit.teco.smartwlanconf.ui.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;

import com.thanosfisherman.wifiutils.WifiUtils;

import edu.kit.teco.smartwlanconf.ui.fragments.WifiFragment;

public class WifiConnectionUtils {

    private static final WifiConnectionUtils ourInstance = new WifiConnectionUtils();
    private boolean enabled = false;
    private WifiFragment calling_fragment;
    private WifiManager wifiManager;
    private BroadcastReceiver wifiScanReceiver;

    public static WifiConnectionUtils getInstance() {
        return ourInstance;
    }

    private WifiConnectionUtils() { }

    public boolean enableWifi(Context context){
        WifiUtils.withContext(context).enableWifi(this::checkResult);
        return enabled;
    }

    private void checkResult(boolean isSuccess){
        enabled = isSuccess;
    }

    public void connectWithWifi_withContext(Context context, String ssid, String pwd, WifiFragment fragment){
        calling_fragment = fragment;
        WifiUtils.withContext(context)
                .connectWith(ssid, pwd)
                .onConnectionResult(this::checkConnection)
                .start();
    }

    private void checkConnection(boolean isSuccess){
        calling_fragment.onWaitForWifiConnection(isSuccess);
    }

    public void scanWifi(WifiFragment wifiFragment){
        Context context = wifiFragment.getActivity().getApplicationContext();
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                boolean success = intent.getBooleanExtra(
                        WifiManager.EXTRA_RESULTS_UPDATED, false);
                        scanSuccess(success, wifiFragment);
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        context.registerReceiver(wifiScanReceiver, intentFilter);

        boolean success = wifiManager.startScan();
        if (!success) {
            // scan failure handling
            scanSuccess(success, wifiFragment);
        }
    }

    private void scanSuccess(boolean success, WifiFragment wifiFragment){
        if(success) {
            wifiFragment.onWaitForWifiScan(wifiManager.getScanResults());
            return;
        }
        wifiFragment.getActivity().unregisterReceiver(wifiScanReceiver);
        wifiFragment.onWaitForWifiScan(null);
    }

}
