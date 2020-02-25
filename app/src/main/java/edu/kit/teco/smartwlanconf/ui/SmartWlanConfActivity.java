package edu.kit.teco.smartwlanconf.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;

import android.content.Intent;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import com.google.zxing.integration.android.IntentIntegrator;


import edu.kit.teco.smartwlanconf.R;
import edu.kit.teco.smartwlanconf.SmartWlanConfApplication;
import edu.kit.teco.smartwlanconf.ui.fragments.CheckNodeWifiFragment;
import edu.kit.teco.smartwlanconf.ui.fragments.CheckUserWifiCredentialsFragment;
import edu.kit.teco.smartwlanconf.ui.fragments.ListOfWifisFragment;
import edu.kit.teco.smartwlanconf.ui.fragments.RestartNodeFragment;
import edu.kit.teco.smartwlanconf.ui.fragments.ShowNodeWebsiteFragment;
import edu.kit.teco.smartwlanconf.ui.fragments.WifiFragment;
import edu.kit.teco.smartwlanconf.ui.utils.WifiConnectionUtils;


// This is the Main activity of the application
// It starts the different Fragments used throughout the application
// and receives the Callback from the QR Code Scanner
public class SmartWlanConfActivity extends AppCompatActivity implements
        ListOfWifisFragment.OnWifiListFragmentInteractionListener,
        CheckUserWifiCredentialsFragment.OnCheckUserWifiCredentialsSuccessListener,
        CheckNodeWifiFragment.OnCheckNodeWifiSuccessListener,
        RestartNodeFragment.OnNodeRestartedListener,
        ShowNodeWebsiteFragment.OnShowNodeSideListener{



    //Data to connect with wifi network of node
    private String mNodeSSID = "";
    //Data to connect with wifi network of user
    private String mWlanSSID = "";
    private String mWlanPwd = "";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifi_activity);

        //Although permission is set in Manifest, it is necessary to request permission here,
        //can not be set in SmartWlanConfApplication because of context
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 555);

        if (savedInstanceState == null) {
            setInitialFragment();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    //The different fragments are managed here

    //This shows the fragment with the list of available Wifis
    public void setInitialFragment(){
        Fragment newFragment = ListOfWifisFragment.newInstance(1);
        replaceFragment(newFragment);
    }

    //This shows the fragment that checks credentials of selected Wifi
    public void onWifiListFragmentInteraction(ScanResult scanResult){
        //Stop receiver from getting results from Wifi Scan
        WifiConnectionUtils wifi = SmartWlanConfApplication.getWifi(getApplicationContext());
        getApplication().unregisterReceiver(wifi.getWifiScanReceiver());
        //Stop running wifi scannning thread
        (SmartWlanConfApplication.getWifiScan(getApplicationContext())).stop();
        Fragment newFragment = CheckUserWifiCredentialsFragment.newInstance(scanResult.SSID);
        replaceFragment(newFragment);
    }

    //This shows the fragment that tries to connect to node wifi
    public void onCheckUserWifiCredentialsSuccess(){
        Fragment newFragment = new CheckNodeWifiFragment();
        replaceFragment(newFragment);
    }

    //This starts the RestartNodeFragment that initializes the node for wifi connection
    public void onCheckNodeWifiSuccess(){
        WifiFragment newFragment = new RestartNodeFragment();
        replaceFragment(newFragment);
    }

    //This starts the ShowNodeWebsiteFragment that opens an external Browser with website of the node
    public void onNodeRestartedSuccess(){
        Fragment newFragment = new ShowNodeWebsiteFragment();
        replaceFragment(newFragment);
    }

    // This is what should be done after trying to open the nodes website
    // At the moment the application just starts all over again
    public void onAfterShowNode(){
        Fragment newFragment = ListOfWifisFragment.newInstance(1);
        replaceFragment(newFragment);
    }

    // Replacement of the actual fragment with the new one
    public void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    // This method is the callback for the QR code scanner and just calls the associated method in the fragment
    // There should be a solution that runs only by calling a callback in the fragment, but this was the easier
    // way to get going
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == IntentIntegrator.REQUEST_CODE) {
            // I want the first child because I know he called the scanner
            getSupportFragmentManager()
                    .getFragments().get(0)
                    .onActivityResult(requestCode, resultCode, data);
        }
    }



    //Setter, Getter

    public void setmNodeSSID(String ssid){
        mNodeSSID = ssid;
    }
    public void setmWlanSSID (String ssid){
        mWlanSSID  = ssid;
    }
    public void setmWlanPwd(String pwd){
        mWlanPwd = pwd;
    }

    public String getmNodeSSID(){
        return mNodeSSID;
    }
    public String getmWlanSSID (){
        return mWlanSSID;
    }
    public String getmWlanPwd(){
        return mWlanPwd;
    }

}
