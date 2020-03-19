package edu.kit.teco.smartwlanconf.ui.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import edu.kit.teco.smartwlanconf.R;
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

/**
 *  Todo: Show hint when opened from NodeNotFound
 *  Todo: Adapt XML layout
*/

public class GetUserWifiCredentialsFragment extends WifiFragment {

    private static final String ARG_WRONGPWD = "WrongPwd";
    private OnGetUserWifiCredentialsListener mListener;
    private boolean wrongPassword;

    public GetUserWifiCredentialsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param firstTime
     * @return A new instance of fragment CheckUserWifiCredentialsFragment.
     *
     *
     * If it's not the first time a new fragment is created, reason is that the node has not been found in user wifi
     * Most likely the user has given wrong password for wifi
     */
    public static GetUserWifiCredentialsFragment newInstance(boolean firstTime) {
        GetUserWifiCredentialsFragment fragment = new GetUserWifiCredentialsFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_WRONGPWD, firstTime);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            wrongPassword = getArguments().getBoolean(ARG_WRONGPWD);
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
    public void onStart() {
        super.onStart();
        if(wrongPassword){
            wrongPassword = false;
            Snackbar snackbar = Snackbar
                    .make(getView(), "Bitte Passwort für ihr Wlan prüfen", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Ok!", (View v)->{
                            Log.d(GetUserWifiCredentialsFragment.class.toString(),"Sensor nicht im Wlan gefunden!");
                    });
            int colorSnackRetry = ResourcesCompat.getColor(getActivity().getResources(), R.color.colorSnackRetry, null);
            snackbar.setActionTextColor(colorSnackRetry);
            snackbar.show();
            EditText sensorpwd = getView().findViewById(R.id.pwd);
            sensorpwd.setError(Config.PWD_ERROR);
        }
        ((EditText) getView().findViewById(R.id.ssid)).setText(((SmartWlanConfActivity)getActivity()).getmWlanSSID());
        setConnectButtonListener();

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

    private void setConnectButtonListener(){

        WifiFragment wifiFragment = this;

        ((EditText)getView().findViewById(R.id.pwd)).setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE)
                {
                    String pwd = ((EditText) getView().findViewById(R.id.pwd)).getText().toString();
                    //Set  Wlan Password in Parent Activity
                    ((SmartWlanConfActivity) getActivity()).setmWlanPwd(pwd);
                    //Remove Error hint
                    ((EditText)getView().findViewById(R.id.ssid)).setError(null);
                    connectToWifi(((SmartWlanConfActivity) getActivity()).getmWlanSSID(), ((SmartWlanConfActivity) getActivity()).getmWlanPwd(), wifiFragment);
                    return true;
                }
                return false;
            }
        });

        final Button getPwdButton = getView().findViewById(R.id.btnGetPwd);
        getPwdButton.setOnClickListener((View v)-> {
            String pwd = ((EditText) getView().findViewById(R.id.pwd)).getText().toString();
            //Set  Wlan Password in Parent Activity
            ((SmartWlanConfActivity) getActivity()).setmWlanPwd(pwd);
            //Remove Error hint
            ((EditText)getView().findViewById(R.id.ssid)).setError(null);
            connectToWifi(((SmartWlanConfActivity) getActivity()).getmWlanSSID(), ((SmartWlanConfActivity) getActivity()).getmWlanPwd(), this);
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
            mListener.onGotUserWifiCredentials();
        } else {
            Snackbar snackbar = Snackbar
                    .make(getView(), "Wifi Verbindung fehlgeschlagen bitte Passwort prüfen!", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Nochmal versuchen!", (View v) -> {
                        EditText sensorpwd = view.findViewById(R.id.pwd);
                        sensorpwd.setError(Config.PWD_ERROR);
                        this.setConnectButtonListener();
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
        void onGotUserWifiCredentials();
    }
}
