package edu.kit.teco.smartwlanconf.ui.fragments;


import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.thanosfisherman.wifiutils.WifiUtils;

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
public class RestartNodeFragment extends AbstractWaitForWifiConnectionFragment {

    private OnNodeRestartedListener mListener;

    public RestartNodeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.restart_node_fragment, container, false);

        //Connection to node is established data can be sent to node
        connectNodeWithUserWifi();
        //Node is now restarting, connect to user wifi and look for node with ShowNodeWebsiteFragment
        connectToUserWifi();

        return layout;
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
            final RestartNodeFragment myfrag = this;
            Snackbar snackbar = Snackbar
                    .make(getView(), "Wifi Verbindung fehlgeschlagen", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Nochmal versuchen!", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            myfrag.connectToUserWifi();
                        }
                    });
            int colorSnackRetry = ResourcesCompat.getColor(getActivity().getResources(), R.color.colorSnackRetry, null);
            snackbar.setActionTextColor(colorSnackRetry);
            snackbar.show();
        }
    }

    private void connectToUserWifi(){
        Context context = getActivity();
        SmartWlanConfApplication
                .getWifi(context)
                .connectWithWifi_withContext(context, ((SmartWlanConfActivity) context).getmWlanSSID(), ((SmartWlanConfActivity) context).getmWlanPwd(), this);
    }

    //Sending wifi data to node restarts the node
    private void connectNodeWithUserWifi(){

        //IP of Gateway is HTTP-Server address of Node
        final String gatewayIP = lookupGateway();

        HttpNodePost request = new HttpNodePost(getContext().getApplicationContext());
        try {
            final String wlanUrl = "http://" + gatewayIP + "/_ac/connect";
            HashMap<String, String> credentials = getNodeWifiCredentials();
            boolean result = request.execute(wlanUrl,
                    credentials.get("SSID"),
                    credentials.get("PWD")).get();
            if(!result){
                final RestartNodeFragment myfrag = this;
                Snackbar snackbar = Snackbar
                        .make(getView(), "Wifi Daten konnten nicht an Knoten geschickt werden!", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Nochmal versuchen!", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                myfrag.connectNodeWithUserWifi();
                            }
                        });
                int colorSnackRetry = ResourcesCompat.getColor(getActivity().getResources(), R.color.colorSnackRetry, null);
                snackbar.setActionTextColor(colorSnackRetry);
                snackbar.show();
            }
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
    private HashMap<String, String> getNodeWifiCredentials(){
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
