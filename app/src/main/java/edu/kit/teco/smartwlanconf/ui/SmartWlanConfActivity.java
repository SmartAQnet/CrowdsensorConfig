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
import edu.kit.teco.smartwlanconf.ui.fragments.PreparingNodeFragment;
import edu.kit.teco.smartwlanconf.ui.fragments.ShowNodeWebsiteFragment;
import edu.kit.teco.smartwlanconf.ui.fragments.WifiCheckCredentialsFragment;
import edu.kit.teco.smartwlanconf.ui.fragments.WifiFragment;
import edu.kit.teco.smartwlanconf.ui.fragments.WifiListFragment;
import edu.kit.teco.smartwlanconf.ui.utils.WifiConnectionUtils;


//MainActivity that implements the listeners of its fragments
public class SmartWlanConfActivity extends AppCompatActivity implements WifiListFragment.OnWifiListFragmentInteractionListener,
                                                                        WifiCheckCredentialsFragment.OnWifiConnectInteractionListener,
                                                                        GetGeoLocationFragment.OnGetLocationSuccessListener,
                                                                        PreparingNodeFragment.OnConnectToUserWifiListener{



    //Data to connect with wifi network of node
    private String mNodeSSID = "179216";
    private String mNodePwd = "12345678"; //TODO: Das ist eine Konstante
    private String mNodeWlanIP = "";
    //Data to connect with wifi network of user
    private String mWlanSSID = "";
    private String mWlanPwd = "";
    //Geo location data
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
        if(!((SmartWlanConfApplication) this.getApplicationContext()).getWifi().enableWifi(this)) {
        //if(!SmartWlanConfApplication.getWifi(this).enableWifi(this)) {
            //TODO: Fehler Wifi ausgeschaltet? -> Snackbar mit retry
        }
        if (savedInstanceState == null) {
            setInitialFragment();
        }
    }

    //The different fragments are managed here

    //This shows the fragment with the list of available Wifis
    private void setInitialFragment(){
        getSupportFragmentManager().beginTransaction()
                //Open with one column
                .replace(R.id.container, WifiListFragment.newInstance(1))
                .commitNow();
    }

    //This shows the fragment that checks credentials of selected Wifi
    public void onWifiListFragmentInteraction(ScanResult scanResult){
        Fragment newfragment = WifiCheckCredentialsFragment.newInstance(scanResult.SSID);
        replaceFragment(newfragment);
    }

    //This shows the fragment that gets the geolocation of an address
    public void onWifiConnectButtonPressedInteraction(){
        Fragment newfragment = new GetGeoLocationFragment();
        replaceFragment(newfragment);
    }

    //This starts the PreparingNodeFragment that initializes the node for wifi connection
    public void onGetLocationSuccess(){
        WifiConnectionUtils wifi = SmartWlanConfApplication.getWifi(this);
        WifiFragment newFragment = new PreparingNodeFragment();
        replaceFragment(newFragment);
        //Connect with node wifi
        wifi.connectWithWifi_withContext(this, mNodeSSID, mNodePwd, newFragment);
        //Todo: WLan in WifiCheckCredentialsFragment, (in Parent?) speichern --> Done
        //Todo: Behandeln, wenn keine Location gefunden wird
        //Todo: Geolocation in GetAdressfragment (in Parent?) speichern --> Done
        //Todo: Mit Knoten verbinden
        //Todo: Wlan und Geolocation an Knoten senden, 120 sec warten
        //Todo: per Bonjour mit Knoten verbinden
        //Todo: Webseite des Knoten anzeigen
        //Todo: interaktive Karte für Geolocation anzeigen
    }

    //This starts the PreparingNodeFragment that initializes the node for wifi connection
    public void onConnectToUserWifiSuccess(){
        WifiConnectionUtils wifi = SmartWlanConfApplication.getWifi(this);
        WifiFragment newFragment = new ShowNodeWebsiteFragment();
        replaceFragment(newFragment);
        //Connect with user wifi
        wifi.connectWithWifi_withContext(this, mWlanSSID, mWlanPwd, newFragment);
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
    public void setmNodeWlanIP(String nodeWlanIP){ mNodeWlanIP = nodeWlanIP; }

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
    public String getmAddress() { return mAddress; }
    public String getmNodeWlanIP() {return mNodeWlanIP;}
}
