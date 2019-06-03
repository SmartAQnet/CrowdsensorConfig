package edu.kit.teco.smartwlanconf;
/**
 * 1. Nach WLan credentials fragen -> Als Login Seite
 * 2. Verbinden -> Fehler Toast und zurück
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
import com.github.druk.rx2dnssd.Rx2DnssdEmbedded;

public class SmartWlanConfApplication extends Application {

    private static final String TAG = "SmartWlanConfApplication";
    private Rx2Dnssd mRxDnssd;
    private RegistrationManager mRegistrationManager;
    private RegTypeManager mRegTypeManager;

    @Override
    public void onCreate() {
        super.onCreate();

        mRxDnssd = createDnssd();
        mRegistrationManager = new RegistrationManager();
        mRegTypeManager = new RegTypeManager(this);
    }

    public static Rx2Dnssd getRxDnssd(@NonNull Context context){
        return ((SmartWlanConfApplication)context.getApplicationContext()).mRxDnssd;
    }

    public static RegistrationManager getRegistrationManager(@NonNull Context context){
        return ((SmartWlanConfApplication) context.getApplicationContext()).mRegistrationManager;
    }

    public static RegTypeManager getRegTypeManager(@NonNull Context context){
        return ((SmartWlanConfApplication) context.getApplicationContext()).mRegTypeManager;
    }

    private static final String DEVICE = "device";
    private static final String ARCH = "arch";
    private static final String DNSSD = "dnssd";

    private Rx2Dnssd createDnssd() {

        return new Rx2DnssdEmbedded(this);
    }
}
