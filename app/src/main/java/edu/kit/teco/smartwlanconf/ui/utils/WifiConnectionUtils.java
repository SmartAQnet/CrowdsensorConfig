package edu.kit.teco.smartwlanconf.ui.utils;

import android.content.Context;
import android.view.View;

import androidx.fragment.app.Fragment;

import com.thanosfisherman.wifiutils.WifiUtils;

import edu.kit.teco.smartwlanconf.ui.fragments.WifiConnectFragment;
import edu.kit.teco.smartwlanconf.ui.fragments.WifiFragment;

public class WifiConnectionUtils {

    private static final WifiConnectionUtils ourInstance = new WifiConnectionUtils();

    private View thisView;
    private boolean connected = false;
    private boolean enabled = false;
    private String mSSID;
    private String mpwd;
    private Context context;
    private WifiFragment calling_fragment;

    public static WifiConnectionUtils getInstance(Context context) {
        ourInstance.initWifi(context);
        return ourInstance;
    }

    private WifiConnectionUtils() {
    }

    private void initWifi(Context context){
        this.context = context;
    }

    public boolean enableWifi(Context context){
        WifiUtils.withContext(context).enableWifi(this::checkResult);
        return enabled;
    }

    private void checkResult(boolean isSuccess){
        enabled = isSuccess;
    }

    public void connectWithWifi(String ssid, String pwd, WifiFragment fragment){
        calling_fragment = fragment;
        WifiUtils.withContext(context.getApplicationContext())
                .connectWith(ssid, pwd)
                .onConnectionResult(this::checkConnection)
                .start();
    }

    private void checkConnection(boolean isSuccess){
        connected = isSuccess;
        calling_fragment.onWaitForWifiConnection();
        //TODO: Wrong way for Callback, find better one
        ((WifiConnectFragment) calling_fragment).callSmartWlanConfActivity();
    }

}
