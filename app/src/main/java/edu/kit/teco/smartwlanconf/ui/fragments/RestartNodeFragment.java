package edu.kit.teco.smartwlanconf.ui.fragments;


import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import java.math.BigInteger;
import java.net.InetAddress;

import java.net.UnknownHostException;
import java.util.HashMap;

import edu.kit.teco.smartwlanconf.SmartWlanConfApplication;
import edu.kit.teco.smartwlanconf.ui.SmartWlanConfActivity;
import edu.kit.teco.smartwlanconf.ui.utils.HttpPostRequest;


import static android.content.Context.WIFI_SERVICE;

/**
 * A simple {@link Fragment} subclass.
 */
public class RestartNodeFragment extends AbstractWaitForWifiConnectionFragment {

    private OnNodeRestartedListener mListener;

    public RestartNodeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //Connection to node is established data can be sent to node
        connectNodeWithUserWifi();
        //Node is now restarting, connect to user wifi and look for node with ShowNodeWebsiteFragment
        Context context = getContext().getApplicationContext();
        ((SmartWlanConfApplication) context).
                getWifi().
                connectWithWifi_withContext(context, ((SmartWlanConfActivity) getActivity()).getmWlanSSID(), ((SmartWlanConfActivity) getActivity()).getmWlanPwd(), this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnNodeRestartedListener) {
            mListener = (OnNodeRestartedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    //Waits for connection with user wifi to be established
    public void onWaitForWifiConnection(Boolean success){
        if (success) {
            mListener.onNodeRestartedSuccess();
        } else {
            //TODO: Should not happen as credentials for wifi has already been checked
        }
    }

    //Sending wifi data to node restarts the node
    private void connectNodeWithUserWifi(){

        //IP of Gateway is HTTP-Server address of Node
        final String ip = lookupGateway();

        HttpPostRequest request = new HttpPostRequest(getContext().getApplicationContext());
        //Todo: second send wlan credentials -> connect node with wlan
        try {
            //Todo: use getWlanData()
            final String wlanUrl = "http://" + ip + "/_ac/connect";
            HashMap<String, String> credentials = getWlanData();
            request.execute(wlanUrl, credentials.get("SSID"), credentials.get("PWD")).get();
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

    //Read SSID and Password and prepare for HTTP Post
    private HashMap<String, String> getWlanData(){
        HashMap<String, String> credentials = new HashMap<>();
        credentials.put("SSID", ((SmartWlanConfActivity) getActivity()).getmWlanSSID());
        credentials.put("PWD",((SmartWlanConfActivity) getActivity()).getmWlanPwd());
        return credentials;
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
    public interface OnNodeRestartedListener {
        void onNodeRestartedSuccess();
    }
}
