package edu.kit.teco.smartwlanconf.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import com.github.druk.rx2dnssd.Rx2DnssdBindable;
import java.net.Inet4Address;

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
    public void onConfigurationChanged(Configuration config){
        super.onConfigurationChanged(config);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startDiscovery();
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

    private void startDiscovery() {
        try {
            SmartWlanConfActivity activity = ((SmartWlanConfActivity) getActivity());
            if(activity == null){
                throw new NullPointerException();
            }
            String mNodeServiceName = activity.getmNodeSSID();
            Rx2DnssdBindable mRxDnssd = (Rx2DnssdBindable) SmartWlanConfApplication.getRxDnssd(getActivity());

            //Searching for Bounjour Services from https://github.com/andriydruk/RxDNSSD
            mDisposable = mRxDnssd.browse(Config.NODE_REQ_TYPE, Config.NODE_DOMAIN)
                    .compose(mRxDnssd.resolve())
                    .compose(mRxDnssd.queryIPRecords())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(mDNSService -> {
                        if(mDNSService.getServiceName().equals(mNodeServiceName)){
                            Inet4Address ip4 = mDNSService.getInet4Address();
                            try {
                                if (ip4 == null) {
                                    throw new NullPointerException();
                                }
                                mNodeIP = mDNSService.getInet4Address().toString();
                            } catch (NullPointerException e){
                                Log.e("Activity", "ip4 null in startDiscovery");
                                //TODO: was machen?
                            }
                            continueAfterDiscovery();
                        }
                    }, throwable -> Log.e("DNSSD", "Error: ", throwable));

        } catch (NullPointerException e) {
            Log.e("Activity", "Activity null in startDiscovery");
            //TODO: Was tun?
        }
    }

    private void continueAfterDiscovery(){
        //Returning from Async call, check if view is still active
        //If not working check if setting a destroyed tag in onDetach() is a solution
        if(getView() == null){
            //Has to be tested if a simple return produces no errors
            return;
        }
        stopDiscovery();
        //open webview with node ip
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://"  + mNodeIP));
        startActivity(browserIntent);
        //return to list of wifis
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
