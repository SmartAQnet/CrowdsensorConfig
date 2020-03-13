package edu.kit.teco.smartwlanconf.ui.fragments;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;

import java.math.BigInteger;
import java.net.InetAddress;

import java.net.UnknownHostException;
import java.util.HashMap;

import edu.kit.teco.smartwlanconf.R;
import edu.kit.teco.smartwlanconf.SmartWlanConfApplication;
import edu.kit.teco.smartwlanconf.ui.Config;
import edu.kit.teco.smartwlanconf.ui.SmartWlanConfActivity;
import edu.kit.teco.smartwlanconf.ui.utils.HttpNodePost;

import static android.content.Context.WIFI_SERVICE;

/**
 * A simple {@link Fragment} subclass.
 * When this fragment is calles the Users phone is now connected to the nodes wifi.
 *
 * This fragment sends the wifi credentials of the user wifi to the node
 * which is then restarted. The users phone is then reconnected to the
 * users wifi, after successful reconnection ShowNodeWebsiteFragment is called.
 */
public class RestartNodeFragment extends WifiFragment {

    private OnNodeRestartedListener mListener;

    public RestartNodeFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.restart_node_fragment, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Connect to sensor wifi
        connectToWifi(((SmartWlanConfActivity)getActivity()).getmNodeSSID(),
                Config.NODE_PWD,
                this);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnNodeRestartedListener) {
            mListener = (OnNodeRestartedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    //connect to sensor wifi
    @Override
    public void onWaitForWifiConnection(boolean success){
        //Returning from Async call, check if view is still active
        //If not working check if setting a destroyed tag in onDetach() is a solution
        View view = getView();
        if(view == null){
            //Has to be tested if a simple return produces no errors, or an Exception has to be thrown
            return;
        }
        if (success) {
            //Send wifi credentials to sensor node and restart it
            connectNodeWithUserWifi();
            mListener.onNodeRestartedSuccess();
        } else {
            Snackbar snackbar = Snackbar
                    .make(view, "Verbindung zu Sensor fehlgeschlagen", Snackbar.LENGTH_LONG);
            int colorSnackRetry = ResourcesCompat.getColor(getActivity().getResources(), R.color.colorSnackRetry, null);
            snackbar.setActionTextColor(colorSnackRetry);
            snackbar.show();
            //Send wifi credentials to sensor node and restart it
            connectNodeWithUserWifi();
        }
    }


    //Sending wifi credentials with http to node restarts the node
    private void connectNodeWithUserWifi(){

        Context context = getContext();
        //As the user's phone is connected to the wifi of the node
        //it's IP is the gateway IP, so you have to look for it
        String gatewayIP;
        try {
            gatewayIP = lookupGateway((Activity) context);
        } catch (NullPointerException e) {
            Log.d(RestartNodeFragment.class.toString(), "lookupGateway() returned null");
            return;
        }

        HttpNodePost request = new HttpNodePost(context.getApplicationContext());
        try {
            //URL to send wifi credentials
            final String wlanUrl = "http://" + gatewayIP + "/_ac/connect";
            //Set credentials
            HashMap<String, String> credentials = getNodeWifiCredentials((Activity)context);
            //Send Data via http
            boolean result = request.execute(wlanUrl,
                    credentials.get("SSID"),
                    credentials.get("PWD")).get();
            //Check if credentials could be sent
            if(!result){
                Snackbar snackbar = Snackbar
                        .make(getView(), "Wifi Daten konnten nicht an Knoten geschickt werden!", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Nochmal versuchen!", (View v) -> this.connectNodeWithUserWifi());
                int colorSnackRetry = ResourcesCompat.getColor(context.getResources(), R.color.colorSnackRetry, null);
                snackbar.setActionTextColor(colorSnackRetry);
                snackbar.show();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    //Gets Gateway IP, which is the ip adress of the node
    private String lookupGateway(Activity activity) throws NullPointerException {
        final WifiManager manager = (WifiManager) activity.getApplicationContext().getSystemService(WIFI_SERVICE);
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
            return (InetAddress.getByAddress(ipAdress)).getHostAddress();
        } catch (UnknownHostException e){
            e.printStackTrace();
            throw new NullPointerException();
        }
    }

    //Put wifi credentials in a Hashmap
    private HashMap<String, String> getNodeWifiCredentials(Activity activity){
        HashMap<String, String> credentials = new HashMap<>();
        credentials.put("SSID", ((SmartWlanConfActivity) activity).getmWlanSSID());
        credentials.put("PWD",((SmartWlanConfActivity) activity).getmWlanPwd());
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
