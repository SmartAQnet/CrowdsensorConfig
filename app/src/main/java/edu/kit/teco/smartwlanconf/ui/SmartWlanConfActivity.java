package edu.kit.teco.smartwlanconf.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import androidx.fragment.app.FragmentTransaction;

import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.widget.EditText;

import edu.kit.teco.smartwlanconf.R;
import edu.kit.teco.smartwlanconf.SmartWlanConfApplication;
import edu.kit.teco.smartwlanconf.ui.fragments.CheckNodeWifiFragment;
import edu.kit.teco.smartwlanconf.ui.fragments.RestartNodeFragment;
import edu.kit.teco.smartwlanconf.ui.fragments.ShowNodeWebsiteFragment;
import edu.kit.teco.smartwlanconf.ui.fragments.CheckUserWifiCredentialsFragment;
import edu.kit.teco.smartwlanconf.ui.fragments.AbstractWaitForWifiConnectionFragment;
import edu.kit.teco.smartwlanconf.ui.fragments.ListOfWifisFragment;


//MainActivity that implements the listeners of its fragments
public class SmartWlanConfActivity extends AppCompatActivity implements
        ListOfWifisFragment.OnWifiListFragmentInteractionListener,
        CheckUserWifiCredentialsFragment.OnCheckUserWifiCredentialsSuccessListener,
        CheckNodeWifiFragment.OnCheckNodeWifiSuccessListener,
        RestartNodeFragment.OnNodeRestartedListener,
        ShowNodeWebsiteFragment.OnShowNodeSideListener{



    //Data to connect with wifi network of node
    private String mNodeSSID = "";
    private String mNodePwd = "12345678"; //TODO: Das ist eine Konstante
    private String mNodeWlanIP = "";
    //Data to connect with wifi network of user
    private String mWlanSSID = "";
    private String mWlanPwd = "";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifi_activity);

        //Although permission is set in Manifest, it is necessary to request permission here
        //TODO: Check if this could be set in SmartWlanConfApplication
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 555);
        //Check Wifi
        //TODO: Prüfe ob das geht
        if(!SmartWlanConfApplication.getWifi(this).enableWifi(this)) {
            //TODO: Fehler Wifi ausgeschaltet? -> Snackbar mit retry
        }
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
    private void setInitialFragment(){
        getSupportFragmentManager().beginTransaction()
                //Open with one column
                .replace(R.id.container, ListOfWifisFragment.newInstance(1))
                .commitNow();
    }

    //This shows the fragment that checks credentials of selected Wifi
    public void onWifiListFragmentInteraction(ScanResult scanResult){
        Fragment newfragment = CheckUserWifiCredentialsFragment.newInstance(scanResult.SSID);
        replaceFragment(newfragment);
    }

    //This shows the fragment that tries to connect to node wifi
    public void onCheckUserWifiCredentialsSuccess(){
        Fragment newfragment = new CheckNodeWifiFragment();
        replaceFragment(newfragment);
    }

    //This starts the RestartNodeFragment that initializes the node for wifi connection
    public void onCheckNodeWifiSuccess(){
        AbstractWaitForWifiConnectionFragment newFragment = new RestartNodeFragment();
        replaceFragment(newFragment);
    }

    //This starts the ShowNodeWebsiteFragment that opens an external Browser with website of the node
    public void onNodeRestartedSuccess(){
        Fragment newFragment = new ShowNodeWebsiteFragment();
        replaceFragment(newFragment);
    }

    //This starts the ShowNodeWebsiteFragment that opens an external Browser with website of the node
    public void onAfterShowNode(){
        Fragment newFragment = ListOfWifisFragment.newInstance(1);
        replaceFragment(newFragment);
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

    public String getmNodeWlanIP() {return mNodeWlanIP;}
}
