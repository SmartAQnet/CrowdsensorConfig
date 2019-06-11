package edu.kit.teco.smartwlanconf.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import edu.kit.teco.smartwlanconf.R;
import edu.kit.teco.smartwlanconf.SmartWlanConfApplication;
import edu.kit.teco.smartwlanconf.ui.fragments.GetAdressFragment;
import edu.kit.teco.smartwlanconf.ui.fragments.WifiConnectFragment;
import edu.kit.teco.smartwlanconf.ui.fragments.WifiListFragment;
import edu.kit.teco.smartwlanconf.ui.utils.HttpGetRequest;


public class SmartWlanConfActivity extends AppCompatActivity implements WifiListFragment.OnWifiListFragmentInteractionListener,
                                                                        WifiConnectFragment.OnWifiConnectInteractionListener,
                                                                        GetAdressFragment.OnGetLocationPressedListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifi_activity);

        //Although permission is set in Manifest, it is necessary to request permission here TODO: Check if this could be set in SmartWlanConfApplication
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 555);
        //Check Wifi
        if(!((SmartWlanConfApplication) this.getApplicationContext()).getWifi().enableWifi(this)) {
            //TODO: Kein Wifi Fehler
        }
        if (savedInstanceState == null) {
            setInitialFragment();
        }
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        if (fragment instanceof WifiListFragment) {
            WifiListFragment wifiListFragment = (WifiListFragment) fragment;
            wifiListFragment.setOnWifiSelectedListener(this);
        }
        if (fragment instanceof WifiConnectFragment) {
            WifiConnectFragment wificonnectFragment = (WifiConnectFragment) fragment;
            wificonnectFragment.setOnButtonPressedListener(this);
        }
    }

    //The different fragments are managed here

    //This shows the fragment with the list of available Wifis
    private void setInitialFragment(){
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, WifiListFragment.newInstance(1))
                .commitNow();
    }

    //This shows the fragment that connects to a selected Wifi
    public void onWifiListFragmentInteraction(ScanResult scanResult){
        Fragment newfragment = WifiConnectFragment.newInstance(scanResult.SSID);
        replaceFragment(newfragment);
    }

    //This shows the fragment that gets the geolocation of an address
    public void onWifiConnectButtonPressedInteraction(){
        Fragment newfragment = new GetAdressFragment();
        replaceFragment(newfragment);
    }

    public void onGetLocationPressedInteraction(){

    }

    //Actual fragment is replaced
    public void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
