package edu.kit.teco.smartwlanconf.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.google.android.material.snackbar.Snackbar;
import com.thanosfisherman.wifiutils.WifiUtils;

import edu.kit.teco.smartwlanconf.R;
import edu.kit.teco.smartwlanconf.ui.Config;
import edu.kit.teco.smartwlanconf.ui.adapter.WifiListItemRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;


/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnWifiListFragmentInteractionListener}
 * interface.
 */
public class ListOfWifisFragment extends Fragment{

    private static final String ARG_COLUMN_COUNT = "Verfügbare Wlan";
    private int mColumnCount = 1;
    private OnWifiListFragmentInteractionListener mListener;
    private List<ScanResult> wifiList = new ArrayList<>();

    private WifiListItemRecyclerViewAdapter wifiAdapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ListOfWifisFragment() {

    }

    public static ListOfWifisFragment newInstance(int columnCount) {
        ListOfWifisFragment fragment = new ListOfWifisFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
        this.setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.wifi_fragment_item_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (!setAdapter(view)){
            //Todo: Muss da was gemacht werden?
            System.out.println("Adapter für WIFI  Liste nicht gesetzt!");
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnWifiListFragmentInteractionListener) {
            mListener = (OnWifiListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private boolean setAdapter(View view){
        Context context = getContext();
        if(context == null){
            return false;
        }
        // Set the adapter
        if (view instanceof FrameLayout) {
            RecyclerView recyclerView = view.findViewById(R.id.list);
            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }
            DividerItemDecoration dividerItemDecoration= new DividerItemDecoration(context,
                    LinearLayoutManager.VERTICAL);
            recyclerView.addItemDecoration(dividerItemDecoration);
            WifiUtils.withContext(context).scanWifi(this::getScanResults).start();
            wifiAdapter = new WifiListItemRecyclerViewAdapter(wifiList, mListener);
            recyclerView.setAdapter(wifiAdapter);
            return true;
        }
        return false;
    }

    private void getScanResults(@NonNull final List<ScanResult> results){
        View view = getView();
        if(view == null){
            //Has to be tested if a simple return produces no errors
            return;
        }
        Activity activity = getActivity();
        if(activity == null){
            //Has to be tested if a simple return produces no errors
            return;
        }

        if (results.isEmpty())
        {
            Snackbar snackbar = Snackbar
                    .make(view, "Keine Wifi Netze gefunden", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Nochmal versuchen!", (View v)->
                            WifiUtils.withContext(getActivity()).scanWifi(ListOfWifisFragment.this::getScanResults).start());
            int colorSnackRetry = ResourcesCompat.getColor(activity.getResources(), R.color.colorSnackRetry, null);
            snackbar.setActionTextColor(colorSnackRetry);
            snackbar.show();
            return;
        }
        //Scan results not empty hide splash screen
        LinearLayout splash = view.findViewById(R.id.splash);
        RecyclerView list = view.findViewById(R.id.list);
        splash.setVisibility(View.GONE);
        list.setVisibility(View.VISIBLE);
        wifiList.clear();
        int netCount = results.size() - 1;
        while (netCount >= 0) {
            ScanResult result = results.get(netCount);
            if (!result.SSID.isEmpty() && result.frequency <= Config.WIFI_BANDWIDTH) {
                wifiList.add(result);
                wifiAdapter.notifyDataSetChanged();
            }
            --netCount;
        }

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnWifiListFragmentInteractionListener {
        void onWifiListFragmentInteraction(ScanResult scanResult);
    }
}
