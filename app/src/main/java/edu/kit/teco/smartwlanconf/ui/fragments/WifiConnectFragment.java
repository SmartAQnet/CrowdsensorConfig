package edu.kit.teco.smartwlanconf.ui.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.thanosfisherman.wifiutils.WifiUtils;

import org.w3c.dom.Text;

import edu.kit.teco.smartwlanconf.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link WifiConnectFragment.OnWifiConnectInteractionListener} interface
 * to handle interaction events.
 * Use the {@link WifiConnectFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WifiConnectFragment extends Fragment {
    private static final String ARG_SSID = "SSID";

    private View myView;
    private String mSSID;
    private OnWifiConnectInteractionListener callback;
    private OnWifiConnectInteractionListener mListener;
    private Boolean connectedToWifi = false;

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

        View wifi_connect_view = inflater.inflate(R.layout.wifi_fragment_connect, container, false);
        ((EditText) wifi_connect_view.findViewById(R.id.ssid)).setText(mSSID);
        setConnectButtonListener(wifi_connect_view);
        myView = wifi_connect_view;
        return wifi_connect_view;
    }

    public void onButtonPressed() {
        if (mListener != null) {
            mListener.onWifiConnectButtonPressedInteraction(mSSID);
        }
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

    public interface OnWifiConnectInteractionListener {
        // TODO: Update argument type and name
        void onWifiConnectButtonPressedInteraction(String ssid);
    }

    private void setConnectButtonListener(View view){
        final Button connectButton = view.findViewById(R.id.btnConnect);
        connectButton.setOnClickListener((View v)-> {
                connectWithWifi(v);
        });
    }

    private void connectWithWifi(View v){
        View parentView = (View) v.getParent();
        String mSSID = ((EditText) parentView.findViewById(R.id.ssid)).getText().toString();
        String mPassword = ((EditText) parentView.findViewById(R.id.pwd)).getText().toString();
        WifiUtils.withContext(getActivity().getApplicationContext())
                .connectWith(mSSID, mPassword)
                .onConnectionResult(this::checkConnection)
                .start();
    }

    private void checkConnection(boolean isSuccess){
        if(isSuccess){
            Toast.makeText(getView().getContext()
                    ,"Verbunden mit " + ((EditText) getView().findViewById(R.id.ssid)).getText().toString()
                    ,Toast.LENGTH_LONG).show();
            if (mListener != null) {
                mListener.onWifiConnectButtonPressedInteraction(mSSID);
            } else {
                //TODO: Fehlerbehandlung wenn kein Listener vorhanden
            }
        } else {
            Toast.makeText(getView().getContext(),"Falsches Passwort",Toast.LENGTH_LONG).show();
            ((EditText) getView().findViewById(R.id.pwd)).setText("");
        }
    }
}
