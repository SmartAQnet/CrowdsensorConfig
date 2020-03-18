package edu.kit.teco.smartwlanconf.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.Manifest;

import android.content.Intent;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import com.google.zxing.integration.android.IntentIntegrator;


import edu.kit.teco.smartwlanconf.R;
import edu.kit.teco.smartwlanconf.SmartWlanConfApplication;
import edu.kit.teco.smartwlanconf.ui.fragments.GetUserWifiCredentialsFragment;
import edu.kit.teco.smartwlanconf.ui.fragments.ListOfSensorsFragment;
import edu.kit.teco.smartwlanconf.ui.fragments.ListOfWifisFragment;
import edu.kit.teco.smartwlanconf.ui.fragments.NodeNotFound;
import edu.kit.teco.smartwlanconf.ui.fragments.RestartNodeFragment;
import edu.kit.teco.smartwlanconf.ui.fragments.ShowNodeWebsiteFragment;
import edu.kit.teco.smartwlanconf.ui.utils.WifiConnectionUtils;


// This is the Main activity of the application
// It starts the different Fragments used throughout the application
// and receives the Callback from the QR Code Scanner
public class SmartWlanConfActivity extends AppCompatActivity implements
        ListOfWifisFragment.OnWifiListFragmentInteractionListener,
        GetUserWifiCredentialsFragment.OnGetUserWifiCredentialsListener,
        RestartNodeFragment.OnNodeRestartedListener,
        ShowNodeWebsiteFragment.OnShowNodeSiteListener,
        NodeNotFound.OnAfterNodeNotFound,
        ListOfSensorsFragment.OnSensorListInteractionListener{



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
        setTitle(Config.APP_TITLE);

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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(getSupportFragmentManager().getBackStackEntryCount()==0){
            setInitialFragment();
        }
    }

    //The different fragments are managed here

    //This shows the fragment with the list of available sensors
    public void setInitialFragment(){
        Fragment newFragment = ListOfSensorsFragment.newInstance(1);
        replaceFragment(newFragment);
    }

    //This shows ListOfWifisFragment
    public void onSensorListInteraction(ScanResult scanResult){
        mNodeSSID = scanResult.SSID;
        Fragment newFragment = ListOfWifisFragment.newInstance(1);
        replaceFragment(newFragment);
    }

    //This shows the fragment that checks credentials of selected Wifi
    public void onWifiListFragmentInteraction(ScanResult scanResult){
        mWlanSSID = scanResult.SSID;
        Fragment newFragment = GetUserWifiCredentialsFragment.newInstance(getApplicationContext(), true, mWlanSSID);
        replaceFragment(newFragment);
    }

    //This shows the fragment that tries to connect to node wifi
    public void onGotUserWifiCredentials(boolean firstTime){
        //Todo: firstTime entfernen
        Fragment newFragment;
        newFragment = new RestartNodeFragment();
        replaceFragment(newFragment);
    }

    //This starts the ShowNodeWebsiteFragment that opens an external Browser with website of the node
    public void onNodeRestartedSuccess(){
        Fragment newFragment = new ShowNodeWebsiteFragment();
        replaceFragment(newFragment);
    }

    // This is what should be done after trying to open the nodes website
    // At the moment the application just starts all over again
    public void onAfterShowNodeSuccess(boolean success){
        Fragment newFragment;
        if(success) {
            //On success just restart app
            newFragment = ListOfWifisFragment.newInstance(1);
        } else {
            //Node not found, try to to find error
            newFragment = NodeNotFound.newInstance();
        }
        replaceFragment(newFragment);
    }

    // This is what should be done after trying to open the nodes website
    // At the moment the application just starts all over again
    public void onAfterNodeNotFound(boolean success){
        Fragment newFragment;
        if(success) {
            //On success just restart app
            newFragment = ListOfWifisFragment.newInstance(1);
        } else {
            //wifi password was wrong, go to GetUserWifiCredentialsFragment
            newFragment = GetUserWifiCredentialsFragment.newInstance(getApplicationContext(), false, mWlanSSID
            );
        }
        replaceFragment(newFragment);
    }

    // Replacement of the actual fragment with the new one
    public void replaceFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit();
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
