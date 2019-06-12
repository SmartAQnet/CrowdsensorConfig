package edu.kit.teco.smartwlanconf.ui.fragments;


import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import androidx.core.content.res.TypedArrayUtils;
import androidx.fragment.app.Fragment;

import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import edu.kit.teco.smartwlanconf.R;
import edu.kit.teco.smartwlanconf.ui.SmartWlanConfActivity;
import edu.kit.teco.smartwlanconf.ui.utils.HttpPostRequest;

import static android.content.Context.WIFI_SERVICE;

/**
 * A simple {@link Fragment} subclass.
 */
public class ShowNodeSiteFragment extends WifiFragment {


    public ShowNodeSiteFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.show_node_site_fragment, container, false);
    }
    @Override
    public void onWaitForWifiConnection(){
        //Connection established now set Data on Node
        setNodedata();
    }

    //First: send geolacation to node
    //Second: send Wlan credentials to node
    //Third: wait 2 Minutes for Node to establish connection
    private void setNodedata(){
        final String ip = lookupGateway();

        final String locationUrl = "http://" + ip;
        final String locationData = getLocationData();
        //Send location to Node
        if(HttpPostRequest.sendData(locationUrl, locationData)){
            //Send Wlan credentials to Node
            final String wlanUrl = locationUrl + "/_ac/connect";
            final String wlanData = getWlanData();
            if(HttpPostRequest.sendData(wlanUrl, wlanData)){
                //Todo: 2 Minuten warten mit Spinner
            } else {
                //Todo: Wlan Credentials nicht gesendet
            }
        } else {
            //Todo: Geolocation nicht gesendet
        }
    }

    private String lookupGateway(){
        final WifiManager manager = (WifiManager) getActivity().getApplicationContext().getSystemService(WIFI_SERVICE);
        final DhcpInfo dhcp = manager.getDhcpInfo();
        byte[] ipAdress = BigInteger.valueOf(dhcp.gateway).toByteArray();
        for(int i=0; i<ipAdress.length/2; i++){
            Byte temp = ipAdress[i];
            ipAdress[i] = ipAdress[ipAdress.length -i -1];
            ipAdress[ipAdress.length -i -1] = temp;
        }
        try {
            final String hostAdress = (InetAddress.getByAddress(ipAdress)).getHostAddress();
            return hostAdress;
        } catch (UnknownHostException e){
            e.printStackTrace();
        }
        return null;
    }

    private String getLocationData(){
        final String location = ((SmartWlanConfActivity) getActivity()).getmGeoLocation();
        try {
            JSONArray array = new JSONArray(location);
            if (array.length() > 0) {
                JSONObject jsonObject = (JSONObject) array.get(0);
                final String mLon = (String) jsonObject.get("lon");
                final String mLat = (String) jsonObject.get("lat");
                return "latitude=" + mLat + "&longitude=" + mLon;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getWlanData(){
        final String ssid = ((SmartWlanConfActivity) getActivity()).getmWlanSSID();
        final String pwd = ((SmartWlanConfActivity) getActivity()).getmWlanPwd();
        return "SSID=" + ssid + "&Passphrase=" + pwd;
    }
}
