package edu.kit.teco.smartwlanconf.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import edu.kit.teco.smartwlanconf.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link NodeNotFound.OnAfterNodeNotFound} interface
 * to handle interaction events.
 * Use the {@link NodeNotFound#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NodeNotFound extends Fragment {

    private OnAfterNodeNotFound mListener;

    public NodeNotFound() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.

     * @return A new instance of fragment NodeNotFound.
     */
    // TODO: Rename and change types and number of parameters
    public static NodeNotFound newInstance() {
        NodeNotFound fragment = new NodeNotFound();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_node_not_found, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setCheckNodeIPButtonListener(view);
    }

    //Button to get Node id/ssid and connect to node wifi
    private void setCheckNodeIPButtonListener(View view){
        //Read node id/ssid from input
        view.findViewById(R.id.btn_check_ip).setOnClickListener((View v)-> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://" + ((EditText) getView().findViewById(R.id.node_ip)).getText().toString()));
            startActivity(browserIntent);
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnAfterNodeNotFound) {
            mListener = (OnAfterNodeNotFound) context;
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
    public interface OnAfterNodeNotFound {
        // TODO: Update argument type and name
        void onAfterNodeNotFound(boolean success);
    }
}
