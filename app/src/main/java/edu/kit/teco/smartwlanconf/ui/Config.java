package edu.kit.teco.smartwlanconf.ui;


//Config file for constants used in application
//Perhaps not complete, constants in code should be moved here
public final class Config {
    //These are the constants needed to send wifi credentials to the node
    public final static String SSIDPARAM = "SSID";
    public final static String PWDPARAM = "Passphrase";
    public final static String NETWORK_INTERFACE_TYPE = "wlan0";


    public final static String NODE_ID_ERROR = "Node ID ist falsch! Bitte erneut eingeben.";

    // Only select 2.5 GHz Wifi
    // Just not to run in trouble with smartphones not supporting 5GHz, maybe obsolete in future
    public final static int WIFI_BANDWIDTH = 2500;

    //At the moment Node Password is a constant
    public final static String NODE_PWD = "12345678";

    //mDNS constants needed to find Node in wifi network through Bonjour Service
    public final static String NODE_DOMAIN= "local.";
    public final static String NODE_REQ_TYPE = "_http._tcp";

}
