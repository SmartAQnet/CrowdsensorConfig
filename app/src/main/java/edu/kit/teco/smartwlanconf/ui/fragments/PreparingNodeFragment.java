package edu.kit.teco.smartwlanconf.ui.fragments;


import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;

import com.github.druk.rx2dnssd.Rx2Dnssd;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.net.InetAddress;

import java.net.UnknownHostException;

import edu.kit.teco.smartwlanconf.SmartWlanConfApplication;
import edu.kit.teco.smartwlanconf.ui.SmartWlanConfActivity;
import edu.kit.teco.smartwlanconf.ui.utils.HttpPostRequest;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


import static android.content.Context.WIFI_SERVICE;

/**
 * A simple {@link Fragment} subclass.
 */
public class PreparingNodeFragment extends WifiFragment {

    private OnConnectToUserWifiListener mListener;

    public PreparingNodeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setNodedata();
        //Data on Node are set and node is restarted to connect with wifi
        //Now app has to connect to wifi
        mListener.onConnectToUserWifiSuccess();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnConnectToUserWifiListener) {
            mListener = (OnConnectToUserWifiListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }


    public void onWaitForWifiConnection(){
        //Connection to local node wifi established now set Wlan and Geolocation Data on Node
        //TODO: activate here
        //setNodedata();
    }

    //Smartphone is now connected with wifi of node
    //First: send geolacation to node
    //Second: send Wlan credentials to node
    //Third: wait 2 Minutes for Node to establish connection
    private void setNodedata(){
        //TODO: getData() anlegen Daten in Hashmap speichern
        //IP of Gateway is HTTP-Server of Node
        final String ip = lookupGateway();

        HttpPostRequest request = new HttpPostRequest(getContext().getApplicationContext());
        //Todo: first send location
        request.execute();
        try {
            //Todo: replace with variables
            final String wlanUrl = "http://172.20.251.95/config";
            request.execute(wlanUrl,getLocationData()).get();
            //TODO: Wait 30 seconds for node restart
        } catch(Exception e) {
            e.printStackTrace();
        }
        //Todo: second send wlan credentials -> connect node with wlan
        try {
            //Todo: use getWlanData()
            final String wlanUrl = "http://172.20.251.95/_ac/connect";
            final String ssid = "TP-Link_84FC";
            final String pass = "13027537";
            request.execute(wlanUrl, ssid, pass).get();
            //TODO: Wait 30 seconds for restart
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnConnectToUserWifiListener {
        // TODO: Update argument type and name
        void onConnectToUserWifiSuccess();
    }
}
