package edu.kit.teco.smartwlanconf.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;

import com.thanosfisherman.wifiutils.WifiUtils;

import java.util.List;

import edu.kit.teco.smartwlanconf.R;
import edu.kit.teco.smartwlanconf.ui.fragments.WifiFragment;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class WifiActivity extends AppCompatActivity implements WifiFragment.OnListFragmentInteractionListener{

    private boolean enabled = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifi_activity);
        //Check Wifi
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 555);
        WifiUtils.withContext(getApplicationContext()).enableWifi(this::checkResult);
        if(!enabled){
            //TODO: Throw Error
        }
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, WifiFragment.newInstance(1))
                    .commitNow();
        }
    }

    private void checkResult(boolean isSuccess){
        enabled = isSuccess;
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        if (fragment instanceof WifiFragment) {
            WifiFragment wifiFragment = (WifiFragment) fragment;
            wifiFragment.setOnHeadlineSelectedListener(this);
        }
    }


    public void onListFragmentInteraction(ScanResult scanResult){

    }

}
