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
public class ShowNodeSiteFragment extends WifiFragment {


    public ShowNodeSiteFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setNodedata();
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

        try{
            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();

            for (NetworkInterface nif: Collections.list(nets)) {
                //do something with the network interface
                String name = nif.getName();
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        //Send location to Node
        HttpPostRequest request = new HttpPostRequest(getContext().getApplicationContext());
        try {
            if (request.execute(locationUrl, locationData).get()) {
                //Send Wlan credentials to Node
                final String wlanUrl = locationUrl + "/_ac/connect";
                final String wlanData = getWlanData();
                if (request.execute(locationUrl, locationData).get()) {
                    //Todo: 2 Minuten warten mit Spinner
                } else {
                    //Todo: Wlan Credentials nicht gesendet
                }
            } else {
                //Todo: Geolocation nicht gesendet
            }
        } catch(Exception e) {
            e.printStackTrace();
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
