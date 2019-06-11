package edu.kit.teco.smartwlanconf.ui.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import edu.kit.teco.smartwlanconf.R;
import edu.kit.teco.smartwlanconf.SmartWlanConfApplication;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link WifiConnectFragment.OnWifiConnectInteractionListener} interface
 * to handle interaction events.
 * Use the {@link WifiConnectFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WifiConnectFragment extends WifiFragment {
    private static final String ARG_SSID = "SSID";

    private String mSSID;
    private OnWifiConnectInteractionListener callback;
    private OnWifiConnectInteractionListener mListener;
    private View view_for_callback;

    public WifiConnectFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param ssid SSID.
     * @return A new instance of fragment WifiConnectFragment.
     */
    public static WifiConnectFragment newInstance(String ssid) {
        WifiConnectFragment fragment = new WifiConnectFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SSID, ssid);
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnButtonPressedListener(OnWifiConnectInteractionListener callback) {
        this.callback = callback;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mSSID = getArguments().getString(ARG_SSID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View this_view = inflater.inflate(R.layout.wifi_fragment_connect, container, false);
        ((EditText) this_view.findViewById(R.id.ssid)).setText(mSSID);
        setConnectButtonListener(this_view);
        return this_view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnWifiConnectInteractionListener) {
            mListener = (OnWifiConnectInteractionListener) context;
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

    private void setConnectButtonListener(View view){
        view_for_callback = view;
        final Button connectButton = view.findViewById(R.id.btnConnect);
        connectButton.setOnClickListener((View v)-> {
            String pwd = ((EditText) view.findViewById(R.id.pwd)).getText().toString();
            //TODO: Change Back to pwd
            ((SmartWlanConfApplication) view.getContext().getApplicationContext()).getWifi().connectWithWifi(mSSID, "13027537", this);
        });
    }

    @Override
    public void onWaitForWifiConnection (){

    }

    public void callSmartWlanConfActivity(){
        if (mListener != null) {
            Toast.makeText(view_for_callback.getContext().getApplicationContext()
                    ,"Verbunden mit " + ((EditText) view_for_callback.findViewById(R.id.ssid)).getText().toString()
                    ,Toast.LENGTH_LONG).show();
            mListener.onWifiConnectButtonPressedInteraction();
        } else {
            //TODO: Fehlerbehandlung wenn kein Listener vorhanden
        }
    }

    public interface OnWifiConnectInteractionListener {
        void onWifiConnectButtonPressedInteraction();
    }
}
