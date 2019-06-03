package edu.kit.teco.smartwlanconf.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import androidx.fragment.app.FragmentTransaction;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.widget.Toast;

import com.thanosfisherman.wifiutils.WifiUtils;


import edu.kit.teco.smartwlanconf.R;
import edu.kit.teco.smartwlanconf.ui.fragments.WifiConnectFragment;
import edu.kit.teco.smartwlanconf.ui.fragments.WifiFragment;


public class WifiActivity extends AppCompatActivity implements WifiFragment.OnListFragmentInteractionListener, WifiConnectFragment.OnWifiConnectInteractionListener{

    private Fragment myFragment;

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
            wifiFragment.setOnWifiSelectedListener(this);
            myFragment = fragment;
        }
        if (fragment instanceof WifiConnectFragment) {
            WifiConnectFragment wificonnectFragment = (WifiConnectFragment) fragment;
            wificonnectFragment.setOnButtonPressedListener(this);
            myFragment = fragment;
        }
    }


    public void onListFragmentInteraction(ScanResult scanResult){
        Fragment newfragment = WifiConnectFragment.newInstance(scanResult.SSID);
        replaceFragment(newfragment);
    }

    public void onWifiConnectButtonPressedInteraction(String ssid){
        Toast.makeText(getApplicationContext(), "Bin hier", Toast.LENGTH_LONG);
    }

    public void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
