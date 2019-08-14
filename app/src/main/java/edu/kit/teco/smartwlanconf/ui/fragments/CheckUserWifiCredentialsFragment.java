package edu.kit.teco.smartwlanconf.ui.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import edu.kit.teco.smartwlanconf.R;
import edu.kit.teco.smartwlanconf.SmartWlanConfApplication;
import edu.kit.teco.smartwlanconf.ui.SmartWlanConfActivity;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnCheckUserWifiCredentialsSuccessListener} interface
 * to handle interaction events.
 * Use the {@link CheckUserWifiCredentialsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CheckUserWifiCredentialsFragment extends AbstractWaitForWifiConnectionFragment {

    private static final String ARG_SSID = "SSID";
    private OnCheckUserWifiCredentialsSuccessListener mListener;

    public CheckUserWifiCredentialsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param ssid SSID.
     * @return A new instance of fragment CheckUserWifiCredentialsFragment.
     */
    public static CheckUserWifiCredentialsFragment newInstance(String ssid) {
        CheckUserWifiCredentialsFragment fragment = new CheckUserWifiCredentialsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SSID, ssid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            SmartWlanConfActivity activity = (SmartWlanConfActivity) getActivity();
            if(activity == null){
                //Has to be tested if a simple return produces no errors, or an Exception has to be thrown
                return;
            }
            //Set SSID for Wlan in parent
            ((SmartWlanConfActivity) getActivity()).setmWlanSSID(getArguments().getString(ARG_SSID));
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.wifi_check_credentials_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SmartWlanConfActivity activity = (SmartWlanConfActivity) getActivity();
        if(activity == null){
            //Has to be tested if a simple return produces no errors, or an Exception has to be thrown
            return;
        }
        ((EditText) view.findViewById(R.id.ssid)).setText(activity.getmWlanSSID());
        setConnectButtonListener(view, activity);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnCheckUserWifiCredentialsSuccessListener) {
            mListener = (OnCheckUserWifiCredentialsSuccessListener) context;
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

        final Button connectButton = view.findViewById(R.id.btnConnect);
        connectButton.setOnClickListener((View v)-> {
            String pwd = ((EditText) view.findViewById(R.id.pwd)).getText().toString();
            //Set  Wlan Passwod in Parent Activity
            activity.setmWlanPwd(pwd);
            Context context = view.getContext().getApplicationContext();
            SmartWlanConfApplication
                    .getWifi(activity)
                    .connectWithWifi_withContext(context, activity.getmWlanSSID(), pwd, this);
        });
    }

    public void onWaitForWifiConnection (Boolean success){
        //Returning from Async call, check if view is still active
        //If not working check if setting a destroyed tag in onDetach() is a solution
        View view = getView();
        if(view == null){
            //Has to be tested if a simple return produces no errors, or an Exception has to be thrown
            return;
        }

        if(success){
            if (mListener != null) {
                Toast.makeText(view.getContext().getApplicationContext()
                        ,"Verbunden mit " + ((EditText) view.findViewById(R.id.ssid)).getText().toString()
                        ,Toast.LENGTH_LONG).show();
                mListener.onCheckUserWifiCredentialsSuccess();
            }
        } else {
            Snackbar snackbar = Snackbar
                    .make(view, "Wifi Verbindung fehlgeschlagen, falsches Passwort?", Snackbar.LENGTH_LONG);
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
    public interface OnCheckUserWifiCredentialsSuccessListener {
        void onCheckUserWifiCredentialsSuccess();
    }
}
