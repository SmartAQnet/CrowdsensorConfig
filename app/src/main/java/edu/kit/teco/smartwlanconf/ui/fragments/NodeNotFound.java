package edu.kit.teco.smartwlanconf.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_node_not_found, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        setCheckNodeIPButtonListener(getView());
        getView().findViewById(R.id.progress_check_node_wifi_after_failed_connection).setVisibility(View.VISIBLE);
        getView().findViewById(R.id.look_for_node_ip).setVisibility(View.GONE);
        startScanning();
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


    /**
     * If scan for sensor is not successful then mDNS lookup failed => ask user for IP-address of node
     * If scan for sensor is successful then wifi password was wrong => open GetUserWifiCredentialsFragment
     */
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

        //No results
        if (results == null) {
            Snackbar snackbar = Snackbar
                    .make(view, "Keine Wifi gefunden. Wifi aktiv?", Snackbar.LENGTH_LONG)
                    .setAction("Nochmal versuchen!", (View v)->{
                        getView().findViewById(R.id.progress_check_node_wifi_after_failed_connection).setVisibility(View.VISIBLE);
                        getView().findViewById(R.id.look_for_node_ip).setVisibility(View.GONE);
                        startScanning();
                    });
            int colorSnackRetry = ResourcesCompat.getColor(activity.getResources(), R.color.colorSnackRetry, null);
            snackbar.setActionTextColor(colorSnackRetry);
            snackbar.show();
            return;
        }
        List<ScanResult> wifiList = new ArrayList<>();
        wifiList.clear();

        // Look for SSID of Node
        for(int i = 0; i < results.size(); i++){
            ScanResult result = results.get(i);
            if (!result.SSID.isEmpty()
                    && result.frequency <= Config.WIFI_BANDWIDTH
                    && result.SSID == ((SmartWlanConfActivity)getActivity()).getmNodeSSID()) {
                //Open GetUserWifiCredentialsFragment
                mListener.onAfterNodeNotFound(true);
            }
        }
        //Stop running scan
        SmartWlanConfApplication.getWifiScan(getContext()).stop();
        getView().findViewById(R.id.progress_check_node_wifi_after_failed_connection).setVisibility(View.GONE);
        getView().findViewById(R.id.look_for_node_ip).setVisibility(View.VISIBLE);
    }

    //Button to get Node id/ssid and connect to node wifi
    private void setCheckNodeIPButtonListener(View view){
        //Read node id/ssid from input
        view.findViewById(R.id.btn_check_ip).setOnClickListener((View v)-> {
            IPTest ipTest = new IPTest();
            String uri = "http://" + ((EditText) getView().findViewById(R.id.node_ip)).getText().toString();
            try {
                URL url = new URL(uri);
                boolean isIPReachable = ipTest.execute(url).get();
                if(isIPReachable){
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    startActivity(browserIntent);
                    //Open first page of app
                    mListener.onAfterNodeNotFound(false);
                } else {
                    //TODO: IP nicht erreichbar nochmal eingeben Fehlerhinweis an Eingabe
                }
            } catch (MalformedURLException urle){
                urle.printStackTrace();
            } catch(Exception e){
                e.printStackTrace();
            }
        });
    }


    class IPTest extends AsyncTask<URL, Void, Boolean>{
        @Override
        protected Boolean doInBackground(URL... urls) {
            ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnected()) {
                try {
                    HttpURLConnection urlc = (HttpURLConnection) urls[0].openConnection();
                    urlc.setConnectTimeout(10 * 1000);          // 10 s.
                    urlc.connect();
                    //Just look if there is a response code
                    if (urlc.getResponseCode() != -1) {
                        Log.wtf("Connection", "Success !");
                        return true;
                    } else {
                        return false;
                    }
                } catch (IOException e) {
                    return false;
                }
            }
            return false;
        }
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
        void onAfterNodeNotFound(boolean nodeFound);
    }
}