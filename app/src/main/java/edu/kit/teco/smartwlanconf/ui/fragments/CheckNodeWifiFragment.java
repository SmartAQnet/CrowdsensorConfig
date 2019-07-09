package edu.kit.teco.smartwlanconf.ui.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import edu.kit.teco.smartwlanconf.R;
import edu.kit.teco.smartwlanconf.SmartWlanConfApplication;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View this_view = inflater.inflate(R.layout.check_node_wifi_fragment, container, false);
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
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void setCheckNodeWifiButtonListener(View view){
        final Button findAddressButton = view.findViewById(R.id.btn_check_node);
        Context context = view.getContext().getApplicationContext();
        findAddressButton.setOnClickListener((View v)-> {
            String ssid = ((EditText) view.findViewById(R.id.node_id)).getText().toString();
            //Set SSID in parent activity
            ((SmartWlanConfActivity) getActivity()).setmNodeSSID(ssid);
            SmartWlanConfApplication
                    .getWifi(getActivity())
                    .connectWithWifi_withContext(context, ssid, ((SmartWlanConfActivity) getActivity()).getmNodePwd(), this);
            /*((SmartWlanConfApplication) context).
                    getWifi().
                    connectWithWifi_withContext(context, ssid, ((SmartWlanConfActivity) getActivity()).getmNodePwd(), this);*/

        });
    }

    public void onWaitForWifiConnection(Boolean success){
        if(success){
            //Show geolocation
            if (mListener != null) {
                mListener.onCheckNodeWifiSuccess();
            } else {
                //TODO: Fehlerbehandlung wenn kein Listener vorhanden
            }
        } else {
            //Todo: Keine Verbindung zu Knoten falsche SSID -> Set Error Text für Eingabefeld
            System.out.println("jjj");
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
        // TODO: Update argument type and name
        void onCheckNodeWifiSuccess();
    }
}
