package danielfilho.ufc.br.com.predetect.constants;

/**
 * Created by whoami on 5/31/16.
 */
public class PredectConstants {

    //Log tag
    public static final String LOG_TAG = "LOG_PREDECT";

    //Error messages
    public static final String SLEEP_ERROR = "Error at Thread Sleep in NetworkObserverService";

    //Wifi Bundle
    public static final String WIFI_BUNDLE = "WIFI_BUNDLE";

    //Wifi scanned
    public static final String WIFI_SCANNED = "WIFI_SCANNED";

    //Constant used to get result receiver object at NetworkObserverService class
    public static final String RESULT_RECEIVER = "RESULT_RECEIVER";

    //Action for start the NetworkObserverService
    public static final String ACTION_SERVICE = "danielfilho.ufc.br.com.predetect.networkservice";

    //Action when NetworkObserverService ends to observing a network
    public static final String ACTION_OBSERVING_ENDS = "danielfilho.ufc.br.com.predetect.OBSERVING_ENDS";

}
