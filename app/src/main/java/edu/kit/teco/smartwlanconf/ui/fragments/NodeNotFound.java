package edu.kit.teco.smartwlanconf.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

import edu.kit.teco.smartwlanconf.R;
import edu.kit.teco.smartwlanconf.SmartWlanConfApplication;
import edu.kit.teco.smartwlanconf.ui.Config;
import edu.kit.teco.smartwlanconf.ui.SmartWlanConfActivity;
import edu.kit.teco.smartwlanconf.ui.utils.WifiConnectionUtils;
import edu.kit.teco.smartwlanconf.ui.utils.WifiScanRunnable;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link NodeNotFound.OnAfterNodeNotFound} interface
 * to handle interaction events.
 * Use the {@link NodeNotFound#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NodeNotFound extends WifiFragment{

    private OnAfterNodeNotFound mListener;

    public NodeNotFound() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.

     * @return A new instance of fragment NodeNotFound.
     *
     * When this fragment is opened node could not be found in user's wifi
     *
     * Check if node has reopened his own wifi, by trying to connect to it => Password for user wifi was wrong
     * Otherwise mDNS lookup was not successful, user has to look for IP-adress of node on it's display
     */
    public static NodeNotFound newInstance() {
        NodeNotFound fragment = new NodeNotFound();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_node_not_found, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    //Button to get Node id/ssid and connect to node wifi
    private void setCheckNodeIPButtonListener(View view){
        //Read node id/ssid from input
        view.findViewById(R.id.btn_check_ip).setOnClickListener((View v)-> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://" + ((EditText) getView().findViewById(R.id.node_ip)).getText().toString()));
            startActivity(browserIntent);
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnAfterNodeNotFound) {
            mListener = (OnAfterNodeNotFound) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void scanForNodeWifi(){
        final WifiFragment wifiFragment = this;
        WifiConnectionUtils wifi = SmartWlanConfApplication.getWifi(getContext());
        //Start scanning for wifis in async task
        WifiScanRunnable wifiScan = new WifiScanRunnable(wifiFragment, wifi);
        //Save wifiscan to stop runnable after scanning wifis
        SmartWlanConfApplication.setWifiScan(getContext(), wifiScan);
        Thread t = new Thread(wifiScan);
        t.start();
    }

    //If not successful mDNS lookup failed, ask user for IP-address of node
    //If successful Wifi password was wrong, open CheckUserWifiCredentialsFragment
    //Callback method, when wifi scan returns it's results
    @Override
    public void onWaitForWifiScan(List<ScanResult> results){
        View view = getView();
        if(view == null){
            Log.d(ListOfWifisFragment.class.toString(), "view is null in onWaitForWifiScan()");
            return;
        }
        Activity activity = getActivity();
        if(activity == null){
            Log.d(ListOfWifisFragment.class.toString(), "activity is null in onWaitForWifiScan()");
            return;
        }

        //Todo: Überarbeiten, wenn nix gefunden
        if (results == null) {
            Snackbar snackbar = Snackbar
                    .make(view, "Keine Wifi Netze gefunden", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Nochmal versuchen!", (View v)->{
                        WifiManager wifi =(WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                        if(!wifi.isWifiEnabled()){
                            //Does this happen?
                            Log.e("ListOfWifisFragment","Wifi nicht aktiviert zum Scannen");
                        }
                        ((SmartWlanConfActivity)getActivity()).setInitialFragment();
                    });
            int colorSnackRetry = ResourcesCompat.getColor(activity.getResources(), R.color.colorSnackRetry, null);
            snackbar.setActionTextColor(colorSnackRetry);
            snackbar.show();
            return;
        }
        //Scan results if not empty show list
        //LinearLayout splash = view.findViewById(R.id.splash);
        //RecyclerView list = view.findViewById(R.id.list);
        //splash.setVisibility(View.GONE);
        //list.setVisibility(View.VISIBLE);
        List<ScanResult> wifiList = new ArrayList<>();
        wifiList.clear();

        // Look for SSID of Node
        for(int i = 0; i < results.size(); i++){
            ScanResult result = results.get(i);
            if (!result.SSID.isEmpty()
                    && result.frequency <= Config.WIFI_BANDWIDTH
                    && result.SSID == ((SmartWlanConfActivity)getActivity()).getmNodeSSID()) {
                //Todo: Found Node wifi => Wifi credentials wrong, open CheckUserWifiCredentialsFragment
                //
            }
        }
        //Todo: Wifi node not found => get IP Address from user
        //Check if IP exists and open external Browser
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
    public interface OnAfterNodeNotFound {
        // TODO: Update argument type and name
        void onAfterNodeNotFound(boolean success);
    }
}
