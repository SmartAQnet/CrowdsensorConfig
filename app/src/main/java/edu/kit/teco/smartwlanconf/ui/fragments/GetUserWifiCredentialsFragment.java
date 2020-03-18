package edu.kit.teco.smartwlanconf.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.snackbar.Snackbar;

import edu.kit.teco.smartwlanconf.R;
import edu.kit.teco.smartwlanconf.SmartWlanConfApplication;
import edu.kit.teco.smartwlanconf.ui.Config;
import edu.kit.teco.smartwlanconf.ui.SmartWlanConfActivity;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnCheckUserWifiCredentialsSuccessListener} interface
 * to handle interaction events.
 * Use the {@link CheckUserWifiCredentialsFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 * This fragment just gets the password for selected wifi from user
 */

/** Todo: Rename to GetUserWifiCredentials
 *  Todo: Show hint when opened from NodeNotFound
 *  Todo: Adapt XML layout
*/

public class GetUserWifiCredentialsFragment extends WifiFragment {

    private static final String ARG_SSID = "SSID";
    private static final String ARG_FIRSTTIME = "FirstTime";
    private OnGetUserWifiCredentialsListener mListener;
    private boolean firstTime;

    public GetUserWifiCredentialsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param context, firstTime
     * @return A new instance of fragment CheckUserWifiCredentialsFragment.
     *
     *
     * If it's not the first time a new fragment is created, reason is that the node has not been found in user wifi
     * Most likely the user has given wrong password for wifi
     */
    public static GetUserWifiCredentialsFragment newInstance(Context context, boolean firstTime, String userWifiSSID) {
        GetUserWifiCredentialsFragment fragment = new GetUserWifiCredentialsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SSID, userWifiSSID);
        args.putBoolean(ARG_FIRSTTIME, firstTime);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            SmartWlanConfActivity activity = (SmartWlanConfActivity) getActivity();
            //Set Wlan SSID
            ((SmartWlanConfActivity) getActivity()).setmWlanSSID(getArguments().getString(ARG_SSID));
            firstTime = getArguments().getBoolean(ARG_FIRSTTIME);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.get_wifi_credentials_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(!firstTime){
            //Todo: Show Hint that password might have been wrong
        }
        ((EditText) getView().findViewById(R.id.ssid)).setText(((SmartWlanConfActivity)getActivity()).getmWlanSSID());
        setConnectButtonListener(getView(), ((SmartWlanConfActivity)getActivity()));

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnGetUserWifiCredentialsListener) {
            mListener = (OnGetUserWifiCredentialsListener) context;
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

    private void setConnectButtonListener(View view, SmartWlanConfActivity activity){

        final Button getPwdButton = view.findViewById(R.id.btnGetPwd);
        getPwdButton.setOnClickListener((View v)-> {

            //Hide screen keyboard
            InputMethodManager inputManager = (InputMethodManager)
                    v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow((null == activity.getCurrentFocus()) ?
                    null : activity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            String pwd = ((EditText) view.findViewById(R.id.pwd)).getText().toString();
            //Set  Wlan Password in Parent Activity
            activity.setmWlanPwd(pwd);
            //Remove Error hint
            ((EditText)view.findViewById(R.id.ssid)).setError(null);
            connectToWifi(activity.getmWlanSSID(), activity.getmWlanPwd(), this);
        });
    }

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
            mListener.onGotUserWifiCredentials(firstTime);
        } else {
            Snackbar snackbar = Snackbar
                    .make(getView(), "Wifi Verbindung fehlgeschlagen bitte Passwort prüfen!", Snackbar.LENGTH_LONG)
                    .setAction("Nochmal versuchen!", (View v) -> {
                        EditText node_id = view.findViewById(R.id.ssid);
                        node_id.setError(Config.SSID_ERROR);
                        this.setConnectButtonListener(view, (SmartWlanConfActivity)getActivity());
                    });
            int colorSnackRetry = ResourcesCompat.getColor(getActivity().getResources(), R.color.colorSnackRetry, null);
            snackbar.setActionTextColor(colorSnackRetry);
            snackbar.show();

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
    public interface OnGetUserWifiCredentialsListener {
        void onGotUserWifiCredentials(boolean firstTime);
    }
}
