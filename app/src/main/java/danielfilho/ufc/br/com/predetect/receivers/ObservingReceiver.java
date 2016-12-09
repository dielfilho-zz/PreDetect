package danielfilho.ufc.br.com.predetect.receivers;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.elvishew.xlog.LogLevel;
import com.elvishew.xlog.XLog;
import com.elvishew.xlog.printer.Printer;
import com.elvishew.xlog.printer.file.FilePrinter;


import danielfilho.ufc.br.com.predetect.constants.PredectConstants;
import danielfilho.ufc.br.com.predetect.services.NetworkObserverService;

/**
 * Created by Daniel Filho on 11/17/16.
 */

public class ObservingReceiver extends WakefulBroadcastReceiver {



    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(PredectConstants.ACTION_SERVICE_WAKEFUL_BROADCAST)) {
            try {
                XLog.init(LogLevel.ALL, new FilePrinter.Builder(PredectConstants.LOG_PATH).build());
            } catch (Exception e) {
            }

            if (intent.getByteArrayExtra(PredectConstants.WIFI_BUNDLE) != null) {
                XLog.d(System.currentTimeMillis()+"|  -------------- PREDECT : ON BROADCAST  -----------");
                Log.d(PredectConstants.LOG_TAG, "-------------- PREDECT : ON BROADCAST  -----------");
                Intent observingIntent = new Intent(context, NetworkObserverService.class);
                observingIntent.putExtra(PredectConstants.WIFI_BUNDLE, intent.getByteArrayExtra(PredectConstants.WIFI_BUNDLE));
                startWakefulService(context, observingIntent);
            } else {
                Log.e(PredectConstants.LOG_TAG, "-------------- THE BUNDLE ON WIFI OBSERVING IS NULL -----------");
                XLog.e(System.currentTimeMillis()+"|  -------------- THE BUNDLE ON WIFI OBSERVING IS NULL -----------");
            }
        }
    }
}
