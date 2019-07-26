package edu.kit.teco.smartwlanconf.ui;

public final class Config {
    public final static String SSIDPARAM = "SSID";
    public final static String PWDPARAM = "Passphrase";
    public final static String NETWORK_INTERFACE_TYPE = "wlan0";
    public final static String NODE_ID_ERROR = "Node ID ist falsch! Bitte erneut eingeben.";

    // Only select 2.5 GHz Wifi
    public final static int WIFI_BANDWIDTH = 2500;

    //Node Password is a constant
    public final static String NODE_PWD = "12345678";

    //Data to find Node in wifi network through Bonjour Service
    public final static String NODE_DOMAIN= "local.";
    public final static String NODE_REQ_TYPE = "_http._tcp";

}
