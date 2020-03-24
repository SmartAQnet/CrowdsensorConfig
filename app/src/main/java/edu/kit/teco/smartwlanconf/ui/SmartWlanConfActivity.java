package edu.kit.teco.smartwlanconf.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;

import android.content.Intent;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import com.google.zxing.integration.android.IntentIntegrator;


import edu.kit.teco.smartwlanconf.R;
import edu.kit.teco.smartwlanconf.ui.fragments.GetUserWifiCredentialsFragment;
import edu.kit.teco.smartwlanconf.ui.fragments.ListOfSensorsFragment;
import edu.kit.teco.smartwlanconf.ui.fragments.ListOfWifisFragment;
import edu.kit.teco.smartwlanconf.ui.fragments.SensorNotFoundFragment;
import edu.kit.teco.smartwlanconf.ui.fragments.RestartSensorFragment;
import edu.kit.teco.smartwlanconf.ui.fragments.ShowSensorWebsiteFragment;


// This is the Main activity of the application
// It starts the different Fragments used throughout the application
// and receives the Callback from the QR Code Scanner
public class SmartWlanConfActivity extends AppCompatActivity implements
        ListOfWifisFragment.OnWifiListFragmentInteractionListener,
        GetUserWifiCredentialsFragment.OnGetUserWifiCredentialsListener,
        RestartSensorFragment.OnSensorRestartedListener,
        ShowSensorWebsiteFragment.OnShowSensorSiteListener,
        SensorNotFoundFragment.OnAfterSensorNotFound,
        ListOfSensorsFragment.OnSensorListInteractionListener{



    //Data to connect with wifi network of sensor
    private String mSensorSSID = "";
    //Data to connect with wifi network of user
    private String mWlanSSID = "";
    private String mWlanPwd = "";
    private ListOfSensorsFragment listOfSensorsFragment;
    private ListOfWifisFragment listOfWifisFragment;
    private GetUserWifiCredentialsFragment getUserWifiCredentialsFragment;
    private RestartSensorFragment restartSensorFragment;
    private SensorNotFoundFragment sensorNotFound;
    private ShowSensorWebsiteFragment showSensorWebsiteFragment;

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
        if(listOfSensorsFragment == null) {
            listOfSensorsFragment = ListOfSensorsFragment.newInstance(1);
        }
        replaceFragment(listOfSensorsFragment);
    }

    //This shows ListOfWifisFragment
    public void onSensorListInteraction(ScanResult scanResult){
        mSensorSSID = scanResult.SSID;
        if(listOfWifisFragment == null) {
            listOfWifisFragment = ListOfWifisFragment.newInstance(1);
        }
        replaceFragment(listOfWifisFragment);
    }

    //This shows the fragment that checks credentials of selected Wifi
    public void onWifiListFragmentInteraction(ScanResult scanResult){
        mWlanSSID = scanResult.SSID;
        if(getUserWifiCredentialsFragment == null) {
            getUserWifiCredentialsFragment = GetUserWifiCredentialsFragment.newInstance(false);
        }
        replaceFragment(getUserWifiCredentialsFragment);
    }

    //This shows the fragment that tries to connect to sensor wifi
    public void onGotUserWifiCredentials(){
        if(restartSensorFragment == null) {
            restartSensorFragment = new RestartSensorFragment();
        }
        replaceFragment(restartSensorFragment );
    }

    //This starts the ShowSensorWebsiteFragment that opens an external Browser with website of the sensor
    public void onSensorRestartedSuccess(){

        if(showSensorWebsiteFragment == null){
            showSensorWebsiteFragment = new ShowSensorWebsiteFragment();
        }
        replaceFragment(showSensorWebsiteFragment);
    }

    // This is what should be done after trying to open the sensors website
    // At the moment the application just starts all over again
    public void onAfterShowSensorSuccess(boolean success){
        Fragment newFragment;
        if(success) {
            //On success just restart app
            if(listOfSensorsFragment == null) {
                listOfSensorsFragment = ListOfSensorsFragment.newInstance(1);
            }
            newFragment = listOfSensorsFragment;
        } else {
            //Sensor not found, try to to find error
            if(sensorNotFound== null) {
                sensorNotFound = SensorNotFoundFragment.newInstance();
            }
            newFragment = sensorNotFound;
        }
        replaceFragment(newFragment);
    }

    // This is what should be done after trying to open the sensors website
    // At the moment the application just starts all over again
    public void onAfterSensorNotFound(boolean wrongPassword){
        Fragment newFragment;
        if(!wrongPassword) {
            //On success just restart app
            if(listOfWifisFragment == null) {
                listOfWifisFragment = ListOfWifisFragment.newInstance(1);
            }
            newFragment = listOfWifisFragment;
        } else {
            //wifi password was wrong, go to GetUserWifiCredentialsFragment
            if(getUserWifiCredentialsFragment == null) {
                getUserWifiCredentialsFragment = GetUserWifiCredentialsFragment.newInstance(wrongPassword);
            }
            getUserWifiCredentialsFragment.setWrongPassword(wrongPassword);
            newFragment = getUserWifiCredentialsFragment;
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
    public void setmWlanPwd(String pwd){
        mWlanPwd = pwd;
    }

    public String getmSensorSSID(){
        return mSensorSSID;
    }
    public String getmWlanSSID (){
        return mWlanSSID;
    }
    public String getmWlanPwd(){
        return mWlanPwd;
    }

}
