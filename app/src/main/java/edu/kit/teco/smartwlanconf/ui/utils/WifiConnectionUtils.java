package edu.kit.teco.smartwlanconf.ui.utils;

import android.app.Application;
import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.thanosfisherman.wifiutils.WifiUtils;
import edu.kit.teco.smartwlanconf.R;

public class WifiConnectionUtils {

    private static final WifiConnectionUtils ourInstance = new WifiConnectionUtils();

    private View thisView;
    private boolean connected = false;
    private boolean enabled = false;
    private String mSSID;
    private String mpwd;
    private Context context;

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

    public boolean connectWithWifi(String ssid, String pwd){
        WifiUtils.withContext(context.getApplicationContext())
                .connectWith(ssid, pwd)
                .onConnectionResult(this::checkConnection)
                .start();
        return connected;
    }

    private void checkConnection(boolean isSuccess){
        connected = isSuccess;
        if(isSuccess){

        } else {

        }
    }

}
