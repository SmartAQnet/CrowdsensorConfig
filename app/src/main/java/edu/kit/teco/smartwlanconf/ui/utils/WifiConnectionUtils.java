package edu.kit.teco.smartwlanconf.ui.utils;

import android.content.Context;

import com.thanosfisherman.wifiutils.WifiUtils;
import edu.kit.teco.smartwlanconf.ui.fragments.AbstractWaitForWifiConnectionFragment;

public class WifiConnectionUtils {

    private static final WifiConnectionUtils ourInstance = new WifiConnectionUtils();

    private boolean enabled = false;
    private AbstractWaitForWifiConnectionFragment calling_fragment;

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

    public void connectWithWifi_withContext(Context context, String ssid, String pwd, AbstractWaitForWifiConnectionFragment fragment){
        calling_fragment = fragment;
        WifiUtils.withContext(context)
                .connectWith(ssid, pwd)
                .onConnectionResult(this::checkConnection)
                .start();
    }

    private void checkConnection(boolean isSuccess){
        calling_fragment.onWaitForWifiConnection(isSuccess);
    }

}
