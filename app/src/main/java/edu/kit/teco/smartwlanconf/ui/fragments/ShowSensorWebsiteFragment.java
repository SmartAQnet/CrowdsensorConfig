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
 * {@link //ShowSensorWebsiteFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 *
 * This is just a landing page, wifi credentials have been sent to the sensor.
 * When the sensor is connected to the users wifi it can be located via mDNS
 * If the sensor can be found, his website is opened in external browser
 * otherwise the app returns to CheckUserWifiCredentialsFragment
 */
public class ShowSensorWebsiteFragment extends WifiFragment {

    private OnShowSensorSiteListener mListener;

    //the sensor's ip adress in user wifi network
    private String mSensorIP;

    //Necessary for stoping discovery of Bonjour services
    private Disposable mDisposable;

    // Required empty public constructor
    public ShowSensorWebsiteFragment() {}

    private Snackbar snackbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.show_sensor_website_fragment, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        connectToWifi(((SmartWlanConfActivity)getActivity()).getmWlanSSID(),
                ((SmartWlanConfActivity)getActivity()).getmWlanPwd(),
                this);
    }

    @Override
    public void onStop() {
        super.onStop();
        if(snackbar != null){
            snackbar.dismiss();
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnShowSensorSiteListener) {
            mListener = (OnShowSensorSiteListener) context;
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
            //Start looking for sensor with bonjour service (mDNS)
            startDiscovery();
        } else {
            snackbar = Snackbar
                    .make(view, "Verbindung zu " + ((SmartWlanConfActivity) getActivity()).getmWlanSSID()+ " fehlgeschlagen.", Snackbar.LENGTH_INDEFINITE)
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
            //SSID of sensor is the same as it's ID, which is his service name
            String mSensorServiceName = ((SmartWlanConfActivity) getActivity()).getmSensorSSID();
            Rx2DnssdBindable mRxDnssd = (Rx2DnssdBindable) SmartWlanConfApplication.getRxDnssd(getActivity());

            //Searching for Bounjour Services from https://github.com/andriydruk/RxDNSSD
            mDisposable = mRxDnssd.browse(Config.SENSOR_REQ_TYPE, Config.SENSOR_DOMAIN)
                    .compose(mRxDnssd.resolve())
                    .compose(mRxDnssd.queryIPRecords())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .timeout(Config.TIMEOUT_FOR_MDNSSCAN, TimeUnit.SECONDS)
                    .onExceptionResumeNext(
                            throwable -> {
                                showSensorDiscoveryError();}
                    )
                    .doOnError(throwable -> {
                            showSensorDiscoveryError();
                    })
                    .subscribe(mDNSService -> {
                        if(mDNSService.getServiceName().equals(mSensorServiceName)){
                            Inet4Address ip4 = mDNSService.getInet4Address();
                            try {
                                if (ip4 == null) {
                                    throw new NullPointerException();
                                }
                                mSensorIP = mDNSService.getInet4Address().toString();
                            } catch (NullPointerException e){
                                Log.e("ShowSensorWebSite", "ip4 null in startDiscovery");
                                continueAfterDiscovery(false);
                            }
                            continueAfterDiscovery(true);
                        }
                    }, throwable -> {
                        Log.e("ShowSensorWebSite", "DNSSDError: ", throwable);
                        showSensorDiscoveryError();
                    });
        } catch (Exception e) {
            //Activity no longer active, do nothing
            Log.e("ShowSensorWebSite", "Activity null in startDiscovery");
        }
    }

    //Show error if sensor cannot be found
    private void showSensorDiscoveryError(){
        View view = getView();
        if(view != null) {
            int colorSnackRetry = ResourcesCompat.getColor(getResources(), R.color.colorSnackRetry, null);
            snackbar = Snackbar.make(getView(), "Sensor nicht im Wlan gefunden!", Snackbar.LENGTH_INDEFINITE);
            snackbar.setAction("Weiter!", (View v)-> continueAfterDiscovery(false))
                    .setActionTextColor(colorSnackRetry)
                    .show();
        } else {
            Log.d(ShowSensorWebsiteFragment.class.toString(), "View is null");
            continueAfterDiscovery(false);
        }
    }

    //Continue after looking for sensor
    private void continueAfterDiscovery(boolean success){
        stopDiscovery();
        //Open website of sensor to continue installation of sensor
        if (success) {
            //open webview with sensor ip
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://" + mSensorIP));
            startActivity(browserIntent);
        }
        mListener.onAfterShowSensorSuccess(success);
    }

    //Stop looking for sensor
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
    public interface OnShowSensorSiteListener {
        void onAfterShowSensorSuccess(boolean success);
    }
}
