package edu.kit.teco.smartwlanconf.ui.fragments;

import androidx.fragment.app.Fragment;

public abstract class AbstractWaitForWifiConnectionFragment extends Fragment {

    public abstract void onWaitForWifiConnection(Boolean success);

}
