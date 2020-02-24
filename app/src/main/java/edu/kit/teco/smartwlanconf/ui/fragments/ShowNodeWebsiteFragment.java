package edu.kit.teco.smartwlanconf.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.druk.rx2dnssd.Rx2DnssdBindable;
import com.google.android.material.snackbar.Snackbar;

import java.net.Inet4Address;
import java.util.Observable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import edu.kit.teco.smartwlanconf.R;
import edu.kit.teco.smartwlanconf.SmartWlanConfApplication;
import edu.kit.teco.smartwlanconf.ui.Config;
import edu.kit.teco.smartwlanconf.ui.SmartWlanConfActivity;
import io.reactivex.Flowable;
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
    //Time in seconds searching for Node
    private int TIMEOUT = 40;

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.show_node_website_fragment, container, false);
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
                    .timeout(TIMEOUT, TimeUnit.SECONDS)
                    .onExceptionResumeNext(
                            throwable -> {
                                showNodeDiscoveryError();
                                continueAfterDiscovery(false);}
                    )
                    .doOnError(throwable -> {
                            showNodeDiscoveryError();
                            continueAfterDiscovery(false);
                    })
                    .subscribe(mDNSService -> {
                        if(mDNSService.getServiceName().equals(mNodeServiceName)){
                            Inet4Address ip4 = mDNSService.getInet4Address();
                            try {
                                if (ip4 == null) {
                                    throw new NullPointerException();
                                }
                                mNodeIP = mDNSService.getInet4Address().toString();
                            } catch (NullPointerException e){
                                Log.e("ShowNodeWebSiteFragment", "ip4 null in startDiscovery");
                                continueAfterDiscovery(false);
                            }
                            continueAfterDiscovery(true);
                        }
                    }, throwable -> {
                        Log.e("ShowNodeWebSiteFragment", "DNSSDError: ", throwable);
                        showNodeDiscoveryError();
                        continueAfterDiscovery(false);});
        } catch (Exception e) {
            //Activity no longer active, do nothing
            Log.e("ShowNodeWebSiteFragment", "Activity null in startDiscovery");
        }
    }

    private void showNodeDiscoveryError(){
        Snackbar snackbar = Snackbar
                .make(getView(), "Knoten nicht im Wlan gefunden!", Snackbar.LENGTH_LONG);
        int colorSnackRetry = ResourcesCompat.getColor(getResources(), R.color.colorSnackRetry, null);
        snackbar.setActionTextColor(colorSnackRetry);
        snackbar.show();
    }

    private void continueAfterDiscovery(Boolean success){
        stopDiscovery();
        if (success) {
            //open webview with node ip
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://" + mNodeIP));
            startActivity(browserIntent);
            //Returning from Async call, check if view is still active
            //If not working check if setting a destroyed tag in onDetach() is a solution
            if (getView() == null) {
                //Has to be tested if a simple return produces no errors
                return;
            }
            mListener.onAfterShowNode();
        } else {
            //Todo: Just restart or show something else?
            mListener.onAfterShowNode();
        }
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
