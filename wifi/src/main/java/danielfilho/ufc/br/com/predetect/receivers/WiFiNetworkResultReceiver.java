package danielfilho.ufc.br.com.predetect.receivers;

import android.os.Handler;

import br.ufc.quixada.predetect.common.receivers.NetworkObserverResultReceiver;
import danielfilho.ufc.br.com.predetect.datas.WiFiData;
import danielfilho.ufc.br.com.predetect.intefaces.WiFiObserver;

import static danielfilho.ufc.br.com.predetect.constants.PreDetectConstants.WIFI_SCANNED;

/**
 *
 * @author Daniel Filho
 * @since 2016
 *
 * Updated by Gabriel Cesar, 2018
 *
 */
public class WiFiNetworkResultReceiver extends NetworkObserverResultReceiver<WiFiData> {

    public WiFiNetworkResultReceiver(WiFiObserver wiFiObserver) {
        super(WIFI_SCANNED, wiFiObserver, null);
    }

    public WiFiNetworkResultReceiver(WiFiObserver wiFiObserver, Handler handler) {
        super(WIFI_SCANNED, wiFiObserver, handler);
    }
}
