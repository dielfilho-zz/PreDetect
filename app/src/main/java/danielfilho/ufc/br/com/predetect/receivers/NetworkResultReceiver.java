package danielfilho.ufc.br.com.predetect.receivers;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;

import java.util.List;

import danielfilho.ufc.br.com.predetect.constants.PredectConstants;
import danielfilho.ufc.br.com.predetect.datas.WiFiData;
import danielfilho.ufc.br.com.predetect.intefaces.WiFiObserver;

/**
 * Created by Daniel Filho on 6/15/16.
 */
public class NetworkResultReceiver extends ResultReceiver {

    private WiFiObserver wiFiObserver;

    public NetworkResultReceiver(Handler handler) {
        super(handler);
    }

    public void setWiFiObserver(WiFiObserver observer) {
        this.wiFiObserver = observer;
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if(wiFiObserver != null) {
            List<WiFiData> wiFiDatas = resultData.getParcelableArrayList(PredectConstants.WIFI_SCANNED);
            wiFiObserver.onWiFiObservingEnds(resultCode, wiFiDatas);
        }else{
            Log.e(PredectConstants.LOG_TAG, "---------- COULD NOT SEND MESSAGE TO WIFI OBSERVER, BECAUSE IT'S NULL ----------");
        }
    }
}
