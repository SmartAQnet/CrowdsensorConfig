package edu.kit.teco.smartwlanconf.ui.fragments;

import android.content.Context;
import android.content.Intent;
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
import java.util.concurrent.TimeUnit;
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
 *
 * This is just a landing page, wifi credentials have been sent to the node.
 * When the node is connected to the users wifi it can be located via mDNS
 * If the node can be found, his website is opened in external browser
 * otherwise the app returns to CheckUserWifiCredentialsFragment
 */
public class ShowNodeWebsiteFragment extends WifiFragment {

    private OnShowNodeSiteListener mListener;

    //the node's ip adress in user wifi network
    private String mNodeIP;

    //Necessary for stoping discovery of Bonjour services
    private Disposable mDisposable;

    // Required empty public constructor
    public ShowNodeWebsiteFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.show_node_website_fragment, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        connectToWifi(((SmartWlanConfActivity)getActivity()).getmWlanSSID(),
                ((SmartWlanConfActivity)getActivity()).getmWlanPwd(),
                this);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnShowNodeSiteListener) {
            mListener = (OnShowNodeSiteListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    //Reconnect to user wifi
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
            //Start looking for node with bonjour service (mDNS)
            startDiscovery();
        } else {
            Snackbar snackbar = Snackbar
                    .make(view, "Verbindung zu" + ((SmartWlanConfActivity) getActivity()).getmWlanSSID()+ " fehlgeschlagen.", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Nochmal versuchen!", (View v)->{
                        connectToWifi(((SmartWlanConfActivity)getActivity()).getmWlanSSID(),
                                ((SmartWlanConfActivity)getActivity()).getmWlanPwd(),
                                this);
                });

            int colorSnackRetry = ResourcesCompat.getColor(getActivity().getResources(), R.color.colorSnackRetry, null);
            snackbar.setActionTextColor(colorSnackRetry);
            snackbar.show();
        }
    }

    //Start searching for sensor with mDNS
    private void startDiscovery() {
        try {
            SmartWlanConfActivity activity = ((SmartWlanConfActivity) getActivity());
            if(activity == null){
                throw new NullPointerException();
            }
            //SSID of node is the same as it's ID, which is his service name
            String mNodeServiceName = activity.getmNodeSSID();
            Rx2DnssdBindable mRxDnssd = (Rx2DnssdBindable) SmartWlanConfApplication.getRxDnssd(getActivity());

            //Searching for Bounjour Services from https://github.com/andriydruk/RxDNSSD
            mDisposable = mRxDnssd.browse(Config.NODE_REQ_TYPE, Config.NODE_DOMAIN)
                    .compose(mRxDnssd.resolve())
                    .compose(mRxDnssd.queryIPRecords())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .timeout(Config.TIMEOUT_FOR_MDNSSCAN, TimeUnit.SECONDS)
                    .onExceptionResumeNext(
                            throwable -> {
                                showNodeDiscoveryError();}
                    )
                    .doOnError(throwable -> {
                            showNodeDiscoveryError();
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
                    });
        } catch (Exception e) {
            //Activity no longer active, do nothing
            Log.e("ShowNodeWebSiteFragment", "Activity null in startDiscovery");
        }
    }

    //Show error if node cannot be found
    private void showNodeDiscoveryError(){
        View view = getView();
        if(view != null) {
            Snackbar snackbar = Snackbar
                    .make(getView(), "Sensor nicht im Wlan gefunden!", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Weiter!", (View v)-> continueAfterDiscovery(false));

            int colorSnackRetry = ResourcesCompat.getColor(getResources(), R.color.colorSnackRetry, null);
            snackbar.setActionTextColor(colorSnackRetry);
            snackbar.show();
        } else {
            Log.d(ShowNodeWebsiteFragment.class.toString(), "View is null");
            continueAfterDiscovery(false);
        }
    }

    //Continue after looking for node
    private void continueAfterDiscovery(boolean success){
        stopDiscovery();
        //Open website of node to continue installation of node
        if (success) {
            //open webview with node ip
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://" + mNodeIP));
            startActivity(browserIntent);
        }
        mListener.onAfterShowNodeSuccess(success);
    }

    //Stop looking for node
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
    public interface OnShowNodeSiteListener {
        void onAfterShowNodeSuccess(boolean success);
    }
}
