package edu.kit.teco.smartwlanconf;
/**
 * 1. Wlan Liste anzeigen
 * 2. Verbinden mit gewähltem Wlan-> Fehler Toast und zurück
 * 3. Eingabeformular Adresse -> Nicht gefunden Toast
 * 4. Anzeigen in Webview und Punkt auswählen
 * 5. Wlan des Sensors suchen und verbinden -> Fehler Toast
 * 6. Daten an Sensor senden und auf Reload warten -> Kann bis zu 2 Minuten dauern
 * 7. Per mDNS mit Knoten verbinden
 * 8. Webseite des Knoten anzeigen
 */

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;

import com.github.druk.rx2dnssd.Rx2Dnssd;
import com.github.druk.rx2dnssd.Rx2DnssdBindable;
import com.github.druk.rx2dnssd.Rx2DnssdEmbedded;

import edu.kit.teco.smartwlanconf.ui.utils.WifiConnectionUtils;

public class SmartWlanConfApplication extends Application {

    private static final String TAG = "SmartWlanConfApplication";
    private Rx2Dnssd mRxDnssd;
    private RegistrationManager mRegistrationManager;
    private RegTypeManager mRegTypeManager;
    private WifiConnectionUtils mWifi;

    @Override
    public void onCreate() {
        super.onCreate();

        mRxDnssd = createDnssd();
        //mRegistrationManager = new RegistrationManager();
        //mRegTypeManager = new RegTypeManager(this);
        mWifi = WifiConnectionUtils.getInstance();
    }

    public static Rx2Dnssd getRxDnssd(@NonNull Context context){
        return ((SmartWlanConfApplication)context.getApplicationContext()).mRxDnssd;
    }

    public WifiConnectionUtils getWifi(){
        return mWifi;
    }

    public static WifiConnectionUtils getWifi(@NonNull Context context){
        return ((SmartWlanConfApplication) context.getApplicationContext()).mWifi;
    }

    private static final String DEVICE = "device";
    private static final String ARCH = "arch";
    private static final String DNSSD = "dnssd";

    private Rx2Dnssd createDnssd() {
        return new Rx2DnssdBindable(this);
    }
}
