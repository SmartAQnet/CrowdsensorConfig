package edu.kit.teco.smartwlanconf.ui.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

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
 *
 * This fragment just gets the password for selected wifi from user and open CheckNodeWifiFragment in first try
 * when coming from NodeNotFound skip CheckNodeWifiFragment and goto RestartNodeFragment
 */

/** Todo: Rename to GetUserWifiCredentials
 *  Show hint when opened from NodeNotFound
 *  Adapt XML layout
 *  Skip CheckNodeWifiFragment when coming from NodeNotFound
*/
public class GetUserWifiCredentialsFragment extends Fragment {

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
    public static GetUserWifiCredentialsFragment newInstance(Context context, boolean firstTime) {
        GetUserWifiCredentialsFragment fragment = new GetUserWifiCredentialsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_SSID, SmartWlanConfApplication.getUserWifiSSID(context));
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
        if(!firstTime){
            //Todo: Show Hint that password might have been wrong
        }
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
    public void onResume() {
        super.onResume();
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
            mListener.onGotUserWifiCredentials();
        });
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
