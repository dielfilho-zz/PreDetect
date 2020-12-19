package danielfilho.ufc.br.com.predetect.constants;

import android.os.Environment;

/**
 *
 * @author Daniel Filho
 * @since 2016
 *
 */
public class PreDetectConstants{

    //Log tag
    public static final String LOG_TAG = "PRE_DETECT__WIFI";

    //Log path
    public static final String LOG_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();

    //Wifi Bundle
    public static final String WIFI_BUNDLE = "WIFI_BUNDLE";

    //Wifi scanned
    public static final String WIFI_SCANNED = "WIFI_SCANNED";

    //Constant used to get result receiver object at NetworkObserverService class
    public static final String RESULT_RECEIVER = "RESULT_RECEIVER";

    //Action for WakefulBroadcast
    public static final String ACTION_SERVICE_WAKEFUL_BROADCAST = "danielfilho.ufc.br.com.predetect.wakefulbroadcast";

    //Action for start NetworkObserverService
    public static final String ACTION_START_OBSERVING_SERVICE = "danielfilho.ufc.br.com.predetect.networkservice";

    //Action when NetworkObserverService ends to observing a network
    public static final String ACTION_OBSERVING_ENDS = "danielfilho.ufc.br.com.predetect.OBSERVING_ENDS";

    public static final String BUNDLE_FINISH_OBSERVING = "BUNDLE_FINISH_OBSERVING";

    public static final String WAKE_LOCK_TAG = "PRE_DETECT_WIFI:OBSERVING_WAKE_LOCK";
}
