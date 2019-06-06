package edu.kit.teco.smartwlanconf.ui.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.get_adress_fragment, container, false);
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

    private void getLocation(){
        HttpGetRequest request = new HttpGetRequest(getContext().getApplicationContext());
        String result;
        try {
            result = request.execute("https://nominatim.openstreetmap.org/search.php?q=11+Bachstr+76287+Rheinstetten&format=json&polygon=1&addressdetails=1").get();
            try {
                JSONArray array = new JSONArray(result);
                if (array.length() > 0) {
                    JSONObject jsonObject = (JSONObject) array.get(0);

                    String mLon = (String) jsonObject.get("lon");
                    String mLat = (String) jsonObject.get("lat");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (Exception e){
            e.printStackTrace();
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
    public interface OnGetLocationPressedListener {
        // TODO: Update argument type and name
        void onGetLocationPressedInteraction(Uri uri);
    }
}
