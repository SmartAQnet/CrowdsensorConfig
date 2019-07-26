package edu.kit.teco.smartwlanconf.ui.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private View this_view;

    public CheckNodeWifiFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this_view = inflater.inflate(R.layout.check_node_wifi_fragment, container, false);
        setCheckNodeWifiButtonListener(this_view);
        return this_view;
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
        super.onResume();
        LinearLayout nodeID =this_view.findViewById(R.id.check_node_wifi);
        nodeID.setVisibility(View.VISIBLE);
        LinearLayout progress =this_view.findViewById(R.id.progress_check_node_wifi);
        progress.setVisibility(View.GONE);
        boolean nodeIDError = SmartWlanConfApplication.getnodeIDError(this.getActivity());
        if (nodeIDError) {
            EditText node_id = getActivity().findViewById(R.id.node_id);
            node_id.setError(Config.NODE_ID_ERROR);
        }
        SmartWlanConfApplication.setnodeIDError(getActivity(), false);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void setCheckNodeWifiButtonListener(View view){
        final Button findAddressButton = view.findViewById(R.id.btn_check_node);
        Context context = getActivity();
        findAddressButton.setOnClickListener((View v)-> {
            String ssid = ((EditText) view.findViewById(R.id.node_id)).getText().toString();
            //Set SSID in parent activity
            ((SmartWlanConfActivity) context).setmNodeSSID(ssid);
            SmartWlanConfApplication
                    .getWifi(context)
                    .connectWithWifi_withContext(context, ssid, Config.NODE_PWD, this);
            //TODO: Does not work
            InputMethodManager inputManager = (InputMethodManager) getActivity().getApplicationContext().getSystemService(context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            LinearLayout nodeID =this_view.findViewById(R.id.check_node_wifi);
            nodeID.setVisibility(View.GONE);
            LinearLayout progress =this_view.findViewById(R.id.progress_check_node_wifi);
            progress.setVisibility(View.VISIBLE);
        });
    }

    public void onWaitForWifiConnection(Boolean success){
        if(success){
            //Connection with node wifi successful, open next fragment via callback
            if (mListener != null) mListener.onCheckNodeWifiSuccess();
            else {
                //Should never happen
            }
        } else {
            //Couldn't connect to node wifi, reopen fragment that asks for node id
            ((SmartWlanConfActivity) getActivity()).onCheckUserWifiCredentialsSuccess();
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
