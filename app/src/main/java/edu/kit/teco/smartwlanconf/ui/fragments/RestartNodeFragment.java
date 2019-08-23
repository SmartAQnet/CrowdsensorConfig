package edu.kit.teco.smartwlanconf.ui.fragments;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
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
import edu.kit.teco.smartwlanconf.ui.SmartWlanConfActivity;
import edu.kit.teco.smartwlanconf.ui.utils.HttpNodePost;

import static android.content.Context.WIFI_SERVICE;

/**
 * A simple {@link Fragment} subclass.
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

        //Connection to node is established data can be sent to node
        connectNodeWithUserWifi();
        //Node is now restarting, connect to user wifi and look for node with ShowNodeWebsiteFragment
        connectToUserWifi();
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
    @Override
    public void onWaitForWifiConnection(Boolean success){
        //Returning from Async call, check if view is still active
        //If not working check if setting a destroyed tag in onDetach() is a solution
        View view = getView();
        if(view == null){
            //Has to be tested if a simple return produces no errors, or an Exception has to be thrown
            return;
        }
        if (success) {
            mListener.onNodeRestartedSuccess();
        } else {
            Snackbar snackbar = Snackbar
                    .make(view, "Wifi Verbindung fehlgeschlagen", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Nochmal versuchen!", (View v) -> connectToUserWifi());
            Activity activity = getActivity();
            if(activity == null){
                //Has to be tested if a simple return produces no errors, or if an Exception has to be thrown
                return;
            }
            int colorSnackRetry = ResourcesCompat.getColor(activity.getResources(), R.color.colorSnackRetry, null);
            snackbar.setActionTextColor(colorSnackRetry);
            snackbar.show();
        }
    }

    @RequiresPermission(Manifest.permission.CHANGE_WIFI_STATE)
    private void connectToUserWifi(){
        Activity activity = getActivity();
        if(activity == null){
            //Has to be tested if a simple return produces no errors, or if an Exception has to be thrown
            return;
        }
        //Some phones seems to loose Permission to Change Wifi state
        //Check here and see if it helps
        PackageManager pm = activity.getPackageManager();
        int hasPerm = pm.checkPermission(
                Manifest.permission.CHANGE_WIFI_STATE,
                activity.getPackageName());
        if (hasPerm != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CHANGE_WIFI_MULTICAST_STATE}, 556);
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CHANGE_WIFI_STATE}, 557);
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.CHANGE_NETWORK_STATE}, 558);
        }
        SmartWlanConfApplication
                .getWifi(activity)
                .connectWithWifi_withContext(activity, ((SmartWlanConfActivity) activity).getmWlanSSID(), ((SmartWlanConfActivity) activity).getmWlanPwd(), this);
    }

    //Sending wifi data to node restarts the node
    private void connectNodeWithUserWifi(){

        Context context = getContext();
        if(context == null){
            //Has to be tested if a simple return produces no errors, or if an Exception has to be thrown
            return;
        }

        View view = getView();
        if(view == null){
            //Has to be tested if a simple return produces no errors, or if an Exception has to be thrown
            return;
        }

        //IP of Gateway is HTTP-Server address of Node
        final String gatewayIP = lookupGateway((Activity) context);

        HttpNodePost request = new HttpNodePost(context.getApplicationContext());
        try {
            final String wlanUrl = "http://" + gatewayIP + "/_ac/connect";
            HashMap<String, String> credentials = getNodeWifiCredentials((Activity)context);
            boolean result = request.execute(wlanUrl,
                    credentials.get("SSID"),
                    credentials.get("PWD")).get();
            if(!result){
                Snackbar snackbar = Snackbar
                        .make(view, "Wifi Daten konnten nicht an Knoten geschickt werden!", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Nochmal versuchen!", (View v) -> this.connectNodeWithUserWifi());
                int colorSnackRetry = ResourcesCompat.getColor(context.getResources(), R.color.colorSnackRetry, null);
                snackbar.setActionTextColor(colorSnackRetry);
                snackbar.show();
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    //Gets Gateway IP, which is the url for the Node
    private String lookupGateway(Activity activity){
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
        }
        return null;
    }

    //Read SSID and Password and prepare for HTTP Post
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
