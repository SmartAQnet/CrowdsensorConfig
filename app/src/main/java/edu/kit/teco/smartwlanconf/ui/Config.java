package edu.kit.teco.smartwlanconf.ui;


//Config file for constants used in application
//Perhaps not complete, constants in code should be moved here
public final class Config {
    //Time in seconds searching for Sensor
    public final static int TIMEOUT_FOR_MDNSSCAN = 10;

    //Characters the sensors name starts with
    public final static String SENSOR_PREFIX = "1";

    //Title bar names used for different fragments
    public final static String APP_TITLE = "Crowd Sensor Konfiguration";
    public final static String LISTOFSENSORS_TITLE = "Bitte klicken Sie auf Ihren Sensor";
    public final static String LISTOFWIFIS_TITLE = "Bitte klicken Sie auf Ihr Wifi";

    //These are the constants needed to send wifi credentials to the sensor
    public final static String SSIDPARAM = "SSID";
    public final static String PWDPARAM = "Passphrase";
    public final static String NETWORK_INTERFACE_TYPE = "wlan0";


    public final static String PWD_ERROR = "Bitte Passwort prüfen und erneut eingeben.";

    // Only select 2.5 GHz Wifi
    // Just not to run in trouble with smartphones not supporting 5GHz, maybe obsolete in future
    public final static int WIFI_BANDWIDTH = 2500;

    //At the moment Sensor Password is a constant
    public final static String SENSOR_PWD = "12345678";

    //mDNS constants needed to find Sensor in wifi network through Bonjour Service
    public final static String SENSOR_DOMAIN= "local.";
    public final static String SENSOR_REQ_TYPE = "_http._tcp";

}
