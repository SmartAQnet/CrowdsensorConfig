package edu.kit.teco.smartwlanconf.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import edu.kit.teco.smartwlanconf.R;
import edu.kit.teco.smartwlanconf.SmartWlanConfApplication;
import edu.kit.teco.smartwlanconf.ui.Config;
import edu.kit.teco.smartwlanconf.ui.SmartWlanConfActivity;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnCheckNodeWifiSuccessListener} interface
 * to handle interaction events.
 *
 * Fragment gets Node ID from user input which is also the SSID of the node wifi
 * and tries to connect to node wifi.
 *
 * User may also try to scan ID from node display
 *
 */
public class CheckNodeWifiFragment extends WifiFragment{

    private OnCheckNodeWifiSuccessListener mListener;

    public CheckNodeWifiFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.check_node_wifi_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setCheckNodeWifiButtonListener(view);
        setScanQRCode(view);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnCheckNodeWifiSuccessListener) {
            mListener = (OnCheckNodeWifiSuccessListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        View view = getView();

        LinearLayout nodeID =view.findViewById(R.id.check_node_wifi);
        nodeID.setVisibility(View.VISIBLE);
        LinearLayout progress =view.findViewById(R.id.progress_check_node_wifi);
        progress.setVisibility(View.GONE);
        boolean nodeIDError = SmartWlanConfApplication.getnodeIDError(view.getContext());
        //if nodeIDError is set user has specified wrong id
        if (nodeIDError) {
            EditText node_id = view.findViewById(R.id.node_id);
            node_id.setError(Config.NODE_ID_ERROR);
        }
        SmartWlanConfApplication.setnodeIDError(view.getContext(), false);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    //Button to get Node id/ssid and connect to node wifi
    private void setCheckNodeWifiButtonListener(View view){
        final Button checkNodeIDButton = view.findViewById(R.id.btn_check_node);
        //Read node id/ssid from input
        checkNodeIDButton.setOnClickListener((View v)->
                connectToWifi(getActivity(), view, getNodeSSID(getActivity()), "a", this));
    }

    //Connect to node wifi
    @Override
    public void connectToWifi(Activity activity, View view, String ssid, String pwd, WifiFragment fragment){
        //Show progress bar and try to connect
        LinearLayout nodeID = view.findViewById(R.id.check_node_wifi);
        nodeID.setVisibility(View.GONE);
        LinearLayout progress = view.findViewById(R.id.progress_check_node_wifi);
        progress.setVisibility(View.VISIBLE);
        SmartWlanConfApplication
                .getWifi(activity)
                .connectWithWifi_withContext(activity, ssid, pwd, this);
    }

    //Read and save Node id/ssid
    private String getNodeSSID(Activity activity){
        String ssid = ((EditText) getView().findViewById(R.id.node_id)).getText().toString();
        //Set SSID in parent activity
        ((SmartWlanConfActivity) activity).setmNodeSSID(ssid);
        //This should hide screen keyboard, but it's not working
        View mFocusView = activity.getCurrentFocus();
        if(mFocusView != null) {
            InputMethodManager inputManager = (InputMethodManager) activity.getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            mFocusView.clearFocus();
            inputManager.hideSoftInputFromWindow(mFocusView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
        ////
        return ssid;
    }

    //Sets the button to start  scanning QR Code from node
    private void setScanQRCode(View view){
        Activity activity = getActivity();
        if(activity == null){
            //Has to be tested if a simple return produces no errors
            return;
        }
        final Button findAddressButton = view.findViewById(R.id.btn_scan_qr);
        findAddressButton.setOnClickListener((View v)->
            new IntentIntegrator(getActivity())
                    .setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES)
                    .setPrompt("Scan Code")
                    .setCameraId(0)
                    .setBeepEnabled(true)
                    .setBarcodeImageEnabled(false)
                    .initiateScan());
    }

    //Callback for connection attempt to node wifi
    @Override
    public void onWaitForWifiConnection(boolean success){
        //Returning from Async call, check if view is still active
        //If not working check if setting a destroyed tag in onDetach() is a solution
        if(getView() == null){
            Log.d(CheckNodeWifiFragment.class.toString(), "view null in onWaitForWifiConnection");
            return;
        }
        SmartWlanConfActivity activity = (SmartWlanConfActivity)getActivity();
        if(activity == null){
            Log.d(CheckNodeWifiFragment.class.toString(), "activity null in onWaitForWifiConnection");
            return;
        }

        if(success){
            //Connection with node wifi successful, open next fragment via callback
            if (mListener != null) mListener.onCheckNodeWifiSuccess();
        } else {
            //Couldn't connect to node wifi, reopen fragment that asks for node id
            activity.onGotUserWifiCredentials();
            //Set error to show error text with node input field
            SmartWlanConfApplication.setnodeIDError(getActivity(), true);
        }
    }

    //Callback method for QR code scanning
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        String barcode = result.getContents();
        connectToWifi(getActivity(), getView(), barcode, Config.NODE_PWD, this);
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
    public interface OnCheckNodeWifiSuccessListener {
        void onCheckNodeWifiSuccess();
    }
}