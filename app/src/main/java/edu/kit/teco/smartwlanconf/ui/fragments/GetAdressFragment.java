package edu.kit.teco.smartwlanconf.ui.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.kit.teco.smartwlanconf.R;
import edu.kit.teco.smartwlanconf.ui.utils.HttpGetRequest;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link GetAdressFragment.OnGetLocationPressedListener} interface
 * to handle interaction events.
 */
public class GetAdressFragment extends Fragment {

    private OnGetLocationPressedListener mListener;
    private OnGetLocationPressedListener callback;

    public GetAdressFragment() {
        // Required empty public constructor
    }

    public void setOnGetLocationPressedListener(OnGetLocationPressedListener callback) {
        this.callback = callback;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View this_view = inflater.inflate(R.layout.get_adress_fragment, container, false);
        setGetAddressButtonListener(this_view);
        return this_view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnGetLocationPressedListener) {
            mListener = (OnGetLocationPressedListener) context;
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

    private void setGetAddressButtonListener(View view){
        final Button connectButton = view.findViewById(R.id.btn_get_address);
        connectButton.setOnClickListener((View v)-> {
            String address = ((EditText) view.findViewById(R.id.house_number)).getText().toString()+ " ";
            address += ((EditText) view.findViewById(R.id.street)).getText().toString() + ", ";
            address += ((EditText) view.findViewById(R.id.postal_code)).getText().toString() + " ";
            address += ((EditText) view.findViewById(R.id.city)).getText().toString();
            getLocation(address);
        });
    }


    private void getLocation(String address){
        HttpGetRequest request = new HttpGetRequest(getContext().getApplicationContext());
        String get_geocode;
        String geolocation = "";
        try {
            String url = String.format("https://nominatim.openstreetmap.org/search?q=%s&format=json&polygon=1&addressdetails=1",address);
            get_geocode = request.execute(url).get();
            try {
                JSONArray array = new JSONArray(get_geocode);
                if (array.length() > 0) {
                    JSONObject jsonObject = (JSONObject) array.get(0);

                    String mLon = (String) jsonObject.get("lon");
                    String mLat = (String) jsonObject.get("lat");
                    geolocation = String.format("Longitude: %s, Latitude: %s", mLon, mLat);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        Toast.makeText(getContext().getApplicationContext(), geolocation, Toast.LENGTH_LONG).show();
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
    public interface OnGetLocationPressedListener {
        // TODO: Update argument type and name
        void onGetLocationPressedInteraction();
    }
}
