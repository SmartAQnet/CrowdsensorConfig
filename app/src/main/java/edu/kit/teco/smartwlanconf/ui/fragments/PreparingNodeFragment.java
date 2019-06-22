package edu.kit.teco.smartwlanconf.ui.fragments;


import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Enumeration;

import edu.kit.teco.smartwlanconf.R;
import edu.kit.teco.smartwlanconf.ui.SmartWlanConfActivity;
import edu.kit.teco.smartwlanconf.ui.utils.HttpPostRequest;


import static android.content.Context.WIFI_SERVICE;

/**
 * A simple {@link Fragment} subclass.
 */
public class PreparingNodeFragment extends WifiFragment {


    public PreparingNodeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //TODO: Remove from here
        //setNodedata();
        getNodeWlanIP();
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.show_node_site_fragment, container, false);
    }

    @Override
    public void onWaitForWifiConnection(){
        //Connection established now set Data on Node
        //TODO: activate here
        //setNodedata();
        getNodeWlanIP();
    }

    //First: send geolacation to node
    //Second: send Wlan credentials to node
    //Third: wait 2 Minutes for Node to establish connection
    private void setNodedata(){
        //TODO: getData() anlegen Daten in Hashmap speichern
        //IP of Gateway is HTTP-Server of Node
        final String ip = lookupGateway();

        //Todo: first send location
        //Todo: second send wlan credentials -> connect node with wlan
        HttpPostRequest request = new HttpPostRequest(getContext().getApplicationContext());
        try {
            final String wlanUrl = "http://172.20.251.95/_ac/connect";
            final String ssid = "TP-Link_84FC";
            final String pass = "13027537";
            request.execute(wlanUrl, ssid, pass).get();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    //Gets Gateway IP, which is the url for the Node
    private String lookupGateway(){
        final WifiManager manager = (WifiManager) getActivity().getApplicationContext().getSystemService(WIFI_SERVICE);
        final DhcpInfo dhcp = manager.getDhcpInfo();
        byte[] ipAdress = BigInteger.valueOf(dhcp.gateway).toByteArray();
        //IpAdress has to be reversed
        for(int i=0; i<ipAdress.length/2; i++){
            Byte temp = ipAdress[i];
            ipAdress[i] = ipAdress[ipAdress.length -i -1];
            ipAdress[ipAdress.length -i -1] = temp;
        }
        try {
            //ipAdress to String
            final String hostAdress = (InetAddress.getByAddress(ipAdress)).getHostAddress();
            return hostAdress;
        } catch (UnknownHostException e){
            e.printStackTrace();
        }
        return null;
    }

    //Read Longitude and Latitude from Json and prepare for HTTP Post
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

    //Read SSID and Password and prepare for HTTP Post
    private String getWlanData(){
        final String ssid = ((SmartWlanConfActivity) getActivity()).getmWlanSSID();
        final String pwd = ((SmartWlanConfActivity) getActivity()).getmWlanPwd();
        return "SSID=" + ssid + "&Passphrase=" + pwd;
    }

    public void getNodeWlanIP(){

    }
}
