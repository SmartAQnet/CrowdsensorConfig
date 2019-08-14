package edu.kit.teco.smartwlanconf.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import edu.kit.teco.smartwlanconf.R;
import edu.kit.teco.smartwlanconf.SmartWlanConfApplication;
import edu.kit.teco.smartwlanconf.ui.Config;
import edu.kit.teco.smartwlanconf.ui.SmartWlanConfActivity;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnCheckNodeWifiSuccessListener} interface
 * to handle interaction events.
 */
public class CheckNodeWifiFragment extends AbstractWaitForWifiConnectionFragment {

    private OnCheckNodeWifiSuccessListener mListener;

    public CheckNodeWifiFragment() {
        // Required empty public constructor
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setCheckNodeWifiButtonListener(view);
    }

    @Override
    public void onAttach(Context context) {
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

        View view = getView();
        if(view == null){
            //Has to be tested if a simple return produces no errors
            return;
        }
        super.onResume();
        LinearLayout nodeID =view.findViewById(R.id.check_node_wifi);
        nodeID.setVisibility(View.VISIBLE);
        LinearLayout progress =view.findViewById(R.id.progress_check_node_wifi);
        progress.setVisibility(View.GONE);
        boolean nodeIDError = SmartWlanConfApplication.getnodeIDError(view.getContext());
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

    private void setCheckNodeWifiButtonListener(View view){
        Activity activity = getActivity();
        if(activity == null){
            //Has to be tested if a simple return produces no errors
            return;
        }
        final Button findAddressButton = view.findViewById(R.id.btn_check_node);
        findAddressButton.setOnClickListener((View v)-> {
            String ssid = ((EditText) view.findViewById(R.id.node_id)).getText().toString();
            //Set SSID in parent activity
            ((SmartWlanConfActivity) activity).setmNodeSSID(ssid);
            SmartWlanConfApplication
                    .getWifi(activity)
                    .connectWithWifi_withContext(activity, ssid, Config.NODE_PWD, this);
            //TODO: Does not work
            View mFocusView = activity.getCurrentFocus();
            if(mFocusView != null) {
                InputMethodManager inputManager = (InputMethodManager) activity.getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                mFocusView.clearFocus();
                inputManager.hideSoftInputFromWindow(mFocusView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
            LinearLayout nodeID = view.findViewById(R.id.check_node_wifi);
            nodeID.setVisibility(View.GONE);
            LinearLayout progress = view.findViewById(R.id.progress_check_node_wifi);
            progress.setVisibility(View.VISIBLE);
        });
    }

    public void onWaitForWifiConnection(Boolean success){
        //Returning from Async call, check if view is still active
        //If not working check if setting a destroyed tag in onDetach() is a solution
        if(getView() == null){
            //Has to be tested if a simple return produces no errors
            return;
        }
        SmartWlanConfActivity activity = (SmartWlanConfActivity)getActivity();
        if(activity == null){
            //Has to be tested if a simple return produces no errors
            return;
        }

        if(success){
            //Connection with node wifi successful, open next fragment via callback
            if (mListener != null) mListener.onCheckNodeWifiSuccess();
        } else {
            //Couldn't connect to node wifi, reopen fragment that asks for node id
            activity.onCheckUserWifiCredentialsSuccess();
            //Set error to show error text with node input field
            SmartWlanConfApplication.setnodeIDError(getActivity(), true);
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
    public interface OnCheckNodeWifiSuccessListener {
        void onCheckNodeWifiSuccess();
    }
}
