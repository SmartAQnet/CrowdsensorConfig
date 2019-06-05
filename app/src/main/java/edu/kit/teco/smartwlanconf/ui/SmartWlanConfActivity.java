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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import edu.kit.teco.smartwlanconf.R;
import edu.kit.teco.smartwlanconf.SmartWlanConfApplication;
import edu.kit.teco.smartwlanconf.ui.fragments.WifiConnectFragment;
import edu.kit.teco.smartwlanconf.ui.fragments.WifiListFragment;
import edu.kit.teco.smartwlanconf.ui.utils.HttpGetRequest;


public class SmartWlanConfActivity extends AppCompatActivity implements WifiListFragment.OnWifiListFragmentInteractionListener, WifiConnectFragment.OnWifiConnectInteractionListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifi_activity);
        //Check Wifi
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 555);

        if(!((SmartWlanConfApplication) this.getApplicationContext()).getWifi().enableWifi(this)) {
            //TODO: Kein Wifi Fehler
        }
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, WifiListFragment.newInstance(1))
                    .commitNow();
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


    public void onWifiListFragmentInteraction(ScanResult scanResult){
        Fragment newfragment = WifiConnectFragment.newInstance(scanResult.SSID);
        replaceFragment(newfragment);
    }

    public void onWifiConnectButtonPressedInteraction(String ssid){
        HttpGetRequest request = new HttpGetRequest();
        String result;
        try {
            result = request.execute("https://nominatim.openstreetmap.org/search.php?q=11+Bachstr+76287+Rheinstetten&format=geojson").get().toString();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(result)));
            Element rootElement = document.getDocumentElement();
        } catch (Exception e){
            e.printStackTrace();
        }
        Toast.makeText(getApplicationContext(), "Bin hier", Toast.LENGTH_LONG).show();
    }

    public void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
