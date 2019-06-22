package edu.kit.teco.smartwlanconf.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import androidx.fragment.app.FragmentTransaction;

import android.net.wifi.ScanResult;
import android.os.Bundle;

import edu.kit.teco.smartwlanconf.R;
import edu.kit.teco.smartwlanconf.SmartWlanConfApplication;
import edu.kit.teco.smartwlanconf.ui.fragments.GetGeoLocationFragment;
import edu.kit.teco.smartwlanconf.ui.fragments.ShowNodeSiteFragment;
import edu.kit.teco.smartwlanconf.ui.fragments.WifiConnectFragment;
import edu.kit.teco.smartwlanconf.ui.fragments.WifiFragment;
import edu.kit.teco.smartwlanconf.ui.fragments.WifiListFragment;
import edu.kit.teco.smartwlanconf.ui.utils.WifiConnectionUtils;

//MainActivity that implements the listeners of its fragments
public class SmartWlanConfActivity extends AppCompatActivity implements WifiListFragment.OnWifiListFragmentInteractionListener,
                                                                        WifiConnectFragment.OnWifiConnectInteractionListener,
                                                                        GetGeoLocationFragment.OnGetLocationPressedListener{
    private String mNodeSSID = "179216";
    private String mNodePwd = "12345678";
    private String mWlanSSID = "";
    private String mWlanPwd = "";
    private String mGeoLocation = "";
    private String mAddress ="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifi_activity);

        //Although permission is set in Manifest, it is necessary to request permission here
        //TODO: Check if this could be set in SmartWlanConfApplication
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 555);
        //Check Wifi
        //TODO: Prüfe ob das geht
        //if(!((SmartWlanConfApplication) this.getApplicationContext()).getWifi().enableWifi(this)) {
        if(!SmartWlanConfApplication.getWifi(this).enableWifi(this)) {
            //TODO: Fehler kein Wifi
        }
        if (savedInstanceState == null) {
            setInitialFragment();
        }
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        //TODO: Achtung alle eintragen
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
        /*getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new ShowNodeSiteFragment())
                //TODO: Zurück ändern
                //.replace(R.id.container, WifiListFragment.newInstance(1))
                .commitNow();*/
    }

    //This shows the fragment that connects to a selected Wifi
    public void onWifiListFragmentInteraction(ScanResult scanResult){
        Fragment newfragment = WifiConnectFragment.newInstance(scanResult.SSID);
        replaceFragment(newfragment);
    }

    //This shows the fragment that gets the geolocation of an address
    public void onWifiConnectButtonPressedInteraction(){
        Fragment newfragment = new GetGeoLocationFragment();
        replaceFragment(newfragment);
    }

    public void onGetLocationPressedInteraction(){
        WifiConnectionUtils wifi = SmartWlanConfApplication.getWifi(this);
        WifiFragment newFragment = new ShowNodeSiteFragment();
        replaceFragment(newFragment);
        wifi.connectWithWifi_withContext(this, mNodeSSID, mNodePwd, newFragment);
        //Todo: WLan in WifiConnectFragment, (in Parent?) speichern --> Done
        //Todo: Behandeln, wenn keine Location gefunden wird
        //Todo: Geolocation in GetAdressfragment (in Parent?) speichern --> Done
        //Todo: Mit Knoten verbinden
        //Todo: Wlan und Geolocation an Knoten senden, 120 sec warten
        //Todo: per Bonjour mit Knoten verbinden
        //Todo: Webseite des Knoten anzeigen
        //Todo: interaktive Karte für Geolocation anzeigen
    }

    //Actual fragment is replaced
    public void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    //Setter, Getter

    public void setmNodeSSID(String ssid){
        mNodeSSID = ssid;
    }
    public void setmNodePwd(String pwd){
        mNodePwd  = pwd;
    }
    public void setmWlanSSID (String ssid){
        mWlanSSID  = ssid;
    }
    public void setmWlanPwd(String pwd){
        mWlanPwd = pwd;
    }
    public void setmGeoLocation(String geolocation, String address){
        mGeoLocation = geolocation;
        mAddress = address;
    }

    public String getmNodeSSID(){
        return mNodeSSID;
    }
    public String getmNodePwd(){
        return mNodePwd;
    }
    public String getmWlanSSID (){
        return mWlanSSID;
    }
    public String getmWlanPwd(){
        return mWlanPwd;
    }
    public String getmGeoLocation(){
        return mGeoLocation;
    }

    private void testBonjour(){

    }

}
