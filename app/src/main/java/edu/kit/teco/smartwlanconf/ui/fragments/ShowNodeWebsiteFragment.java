package edu.kit.teco.smartwlanconf.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.druk.rx2dnssd.Rx2DnssdBindable;

import edu.kit.teco.smartwlanconf.R;
import edu.kit.teco.smartwlanconf.SmartWlanConfApplication;
import edu.kit.teco.smartwlanconf.ui.Config;
import edu.kit.teco.smartwlanconf.ui.SmartWlanConfActivity;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link //ShowNodeWebsiteFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class ShowNodeWebsiteFragment extends Fragment {

    private OnShowNodeSideListener mListener;

    //the node's ip adress in user wifi network
    private String mNodeIP="";
    //Necessary for stoping discovery of Bonjour services
    private Disposable mDisposable;

    public ShowNodeWebsiteFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startDiscovery();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.show_node_website_fragment, container, false);
        return layout;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnShowNodeSideListener) {
            mListener = (OnShowNodeSideListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        //mListener = null;
    }

    private void startDiscovery() {
        String mNodeServiceName = ((SmartWlanConfActivity) getActivity()).getmNodeSSID();
        Rx2DnssdBindable mRxDnssd = (Rx2DnssdBindable) SmartWlanConfApplication.getRxDnssd(getActivity());

        //Searching for Bounjour Services from https://github.com/andriydruk/RxDNSSD
        mDisposable = mRxDnssd.browse(Config.NODE_REQ_TYPE, Config.NODE_DOMAIN)
                .compose(mRxDnssd.resolve())
                .compose(mRxDnssd.queryIPRecords())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mDNSService -> {
                    if(mDNSService.getServiceName().equals(mNodeServiceName)){
                        mNodeIP = mDNSService.getInet4Address().toString();
                        continueAfterDiscovery();
                    }
                }, throwable -> {
                    Log.e("DNSSD", "Error: ", throwable);
                });
    }

    private void continueAfterDiscovery(){
        stopDiscovery();
        //open webview with node ip
        Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://"  + mNodeIP));
        startActivity(browserIntent);
        //Return to list of wifis
        mListener.onAfterShowNode();
    }

    private void stopDiscovery() {
        if (mDisposable != null) {
            mDisposable.dispose();
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
    public interface OnShowNodeSideListener {
        void onAfterShowNode();
    }
}
