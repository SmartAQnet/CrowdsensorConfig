package edu.kit.teco.smartwlanconf.ui.utils;

import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.thanosfisherman.wifiutils.WifiUtils;
import edu.kit.teco.smartwlanconf.R;
import edu.kit.teco.smartwlanconf.ui.fragments.WifiConnectFragment;

public class WifiConnectionUtils {
    private static final WifiConnectionUtils ourInstance = new WifiConnectionUtils();
    private View thisView;
    private boolean connected = false;
    private boolean enabled = false;
    private String mSSID;
    private WifiConnectFragment.OnWifiConnectInteractionListener mListener;

    public static WifiConnectionUtils getInstance(String mSSID) {
        ourInstance.setmSSID(mSSID);
        return ourInstance;
    }

    private WifiConnectionUtils() {
    }

    private void setmSSID(String mSSID){
        this.mSSID = mSSID;
    }

    public boolean enableWifi(Context context){
        WifiUtils.withContext(context).enableWifi(this::checkResult);
        return enabled;
    }

    private void checkResult(boolean isSuccess){
        enabled = isSuccess;
    }

    public boolean connectWithWifi(View v){
        thisView = (View) v.getParent();
        String mSSID = ((EditText) thisView.findViewById(R.id.ssid)).getText().toString();
        String mPassword = ((EditText) thisView.findViewById(R.id.pwd)).getText().toString();
        WifiUtils.withContext(thisView.getContext())
                .connectWith(mSSID, mPassword)
                .onConnectionResult(this::checkConnection)
                .start();
        return connected;
    }

    private void checkConnection(boolean isSuccess){
        connected = isSuccess;
        if(isSuccess){
            Toast.makeText(thisView.getContext()
                    ,"Verbunden mit " + ((EditText) thisView.findViewById(R.id.ssid)).getText().toString()
                    ,Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(thisView.getContext(),"Falsches Passwort",Toast.LENGTH_LONG).show();
            ((EditText) thisView.findViewById(R.id.pwd)).setText("");
        }
    }

}
