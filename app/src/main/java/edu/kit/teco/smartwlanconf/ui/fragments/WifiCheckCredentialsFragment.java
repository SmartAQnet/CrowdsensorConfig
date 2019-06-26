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
import edu.kit.teco.smartwlanconf.ui.SmartWlanConfActivity;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link WifiCheckCredentialsFragment.OnWifiConnectInteractionListener} interface
 * to handle interaction events.
 * Use the {@link WifiCheckCredentialsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WifiCheckCredentialsFragment extends WifiFragment {

    private static final String ARG_SSID = "SSID";
    private OnWifiConnectInteractionListener mListener;
    private View my_view;

    public WifiCheckCredentialsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param ssid SSID.
     * @return A new instance of fragment WifiCheckCredentialsFragment.
     */
    public static WifiCheckCredentialsFragment newInstance(String ssid) {
        WifiCheckCredentialsFragment fragment = new WifiCheckCredentialsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SSID, ssid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            //Set SSID for Wlan in parent
            ((SmartWlanConfActivity) getActivity()).setmWlanSSID(getArguments().getString(ARG_SSID));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View this_view = inflater.inflate(R.layout.wifi_fragment_connect, container, false);
        ((EditText) this_view.findViewById(R.id.ssid)).setText(((SmartWlanConfActivity) getActivity()).getmWlanSSID());
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
        my_view = view;

        final Button connectButton = view.findViewById(R.id.btnConnect);
        connectButton.setOnClickListener((View v)-> {
            String pwd = ((EditText) view.findViewById(R.id.pwd)).getText().toString();
            //Set pwd for Wlan in parent
            ((SmartWlanConfActivity) getActivity()).setmWlanPwd(pwd);
            Context context = view.getContext().getApplicationContext();
            //TODO: Change Back to pwd
            ((SmartWlanConfApplication) context).
                    getWifi().
                    connectWithWifi_withContext(context, ((SmartWlanConfActivity) getActivity()).getmWlanSSID(), pwd, this);
        });
    }

    public void onWaitForWifiConnection (){
        if (mListener != null) {
            Toast.makeText(my_view.getContext().getApplicationContext()
                    ,"Verbunden mit " + ((EditText) my_view.findViewById(R.id.ssid)).getText().toString()
                    ,Toast.LENGTH_LONG).show();
            mListener.onWifiConnectButtonPressedInteraction();
        } else {
            //TODO: Fehlerbehandlung wenn kein Listener vorhanden
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
    public interface OnWifiConnectInteractionListener {
        void onWifiConnectButtonPressedInteraction();
    }
}
