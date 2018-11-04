package danielfilho.ufc.br.com.predetect.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.util.Log;

import com.elvishew.xlog.LogLevel;
import com.elvishew.xlog.XLog;
import com.elvishew.xlog.printer.file.FilePrinter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import br.ufc.quixada.predetect.common.domain.NetworkResultStatus;
import br.ufc.quixada.predetect.common.utils.ParcelableUtilsKt;
import danielfilho.ufc.br.com.predetect.datas.WiFiBundle;
import danielfilho.ufc.br.com.predetect.datas.WiFiData;
import danielfilho.ufc.br.com.predetect.managers.WifiNetworkManager;
import danielfilho.ufc.br.com.predetect.receivers.ObservingReceiver;
import danielfilho.ufc.br.com.predetect.utils.NetworkUtils;

import static br.ufc.quixada.predetect.common.utils.ConstantsKt.OBSERVED_HISTORY;
import static br.ufc.quixada.predetect.common.utils.ConstantsKt.SLEEP_TIME;
import static br.ufc.quixada.predetect.common.utils.ConstantsKt.TOKEN_OBSERVER;
import static danielfilho.ufc.br.com.predetect.constants.PreDetectConstants.ACTION_OBSERVING_ENDS;
import static danielfilho.ufc.br.com.predetect.constants.PreDetectConstants.BUNDLE_FINISH_OBSERVING;
import static danielfilho.ufc.br.com.predetect.constants.PreDetectConstants.LOG_PATH;
import static danielfilho.ufc.br.com.predetect.constants.PreDetectConstants.LOG_TAG;
import static danielfilho.ufc.br.com.predetect.constants.PreDetectConstants.RESULT_RECEIVER;
import static danielfilho.ufc.br.com.predetect.constants.PreDetectConstants.WAKE_LOCK_TAG;
import static danielfilho.ufc.br.com.predetect.constants.PreDetectConstants.WIFI_BUNDLE;
import static danielfilho.ufc.br.com.predetect.constants.PreDetectConstants.WIFI_SCANNED;

/**
 *
 * @author Daniel Filho
 * @since 2016
 *
 * Updated by Gabriel Cesar, 2018
 *
 */
public class NetworkObserverService extends Service implements Runnable {
    private Integer sleepTime;
    private String observerToken;
    private Intent wakefulIntent;
    private WiFiBundle wiFiBundle;
    private WifiManager wifiManager;
    private WifiNetworkManager wifiNetworkManager;
    private ResultReceiver networkResultReceiver;

    private PowerManager.WakeLock wakeLock;

    @Override
    public void run() {
        Bundle bundle = new Bundle();
        bundle.putString(TOKEN_OBSERVER, observerToken);

        XLog.d("####################");

        int observedTime = 0;

        int timeInSeconds = wiFiBundle.getObserveTime() / sleepTime;

        HashSet<WiFiData> wiFiDataSet = new HashSet<>();

        for (String mac : wiFiBundle.getWifiData()) {
            wiFiDataSet.add(new WiFiData(mac));
        }

        Log.i(LOG_TAG, "NetworkObserverService: OBSERVER " + wiFiDataSet.size() + "WIFI's");

        HashMap<String, List<WiFiData>> observerHistory = new HashMap<>();

        while (observedTime < timeInSeconds) {

            List<ScanResult> scanResults = wifiManager.getScanResults();

            wiFiDataSet = NetworkUtils.mergeWifiData(scanResults, wiFiDataSet);

            if (!wiFiDataSet.isEmpty()){
                for (WiFiData wifiScan : wiFiDataSet) {

                    if (NetworkUtils.isValidWifiData(wifiScan) && wifiScan.getDistance() <= wiFiBundle.getDistanceRange()) {

                        int newAppear = wifiScan.getObserveCount() + 1;
                        wifiScan.setObserveCount(newAppear);

                        Log.d(LOG_TAG, "NetworkObserverService: OBSERVE COUNT = " + newAppear);

                        double percent = (wifiScan.getObserveCount() * 100) / timeInSeconds;
                        wifiScan.setPercent(percent);

                        Log.d(LOG_TAG, "NetworkObserverService: PERCENT = " + percent);
                    }

                    if (!observerHistory.containsKey(wifiScan.getMAC())) {
                        observerHistory.put(wifiScan.getMAC(), new ArrayList<>());
                    }

                    observerHistory
                            .get(wifiScan.getMAC())
                            .add(wifiScan);
                }

                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    Log.e(LOG_TAG, "NetworkObserverService: Error at Thread Sleep | " + e.getMessage());

                    if (networkResultReceiver != null) {
                        bundle.putParcelableArrayList(WIFI_SCANNED, new ArrayList<>(wiFiDataSet));
                        bundle.putSerializable(OBSERVED_HISTORY, observerHistory);
                        networkResultReceiver.send(NetworkResultStatus.FAIL.getValue(), bundle);
                        Log.d(LOG_TAG, "NetworkObserverService: FAIL NETWORK");
                    }
                }

                observedTime++;

            } else {
                if (networkResultReceiver != null)
                    networkResultReceiver.send(NetworkResultStatus.UNDEFINED.getValue(), null);

                Log.e(LOG_TAG, "NetworkObserverService: UNDEFINED NETWORK");
                // If there's no WIFI on ScanResults, stopping service.
                stopSelf();
            }

        }

        XLog.d("--------- SERVICE ENDS " + System.currentTimeMillis() + " ---------");
        Log.d(LOG_TAG, "NetworkObserverService: SERVICE ENDS");

        // Sending the result for the result receiver telling that network observing ends

        bundle.putParcelableArrayList(WIFI_SCANNED, new ArrayList<>(wiFiDataSet));
        bundle.putSerializable(OBSERVED_HISTORY, observerHistory);

        if(networkResultReceiver != null) {
            networkResultReceiver.send(NetworkResultStatus.SUCCESS.getValue(), bundle);
            Log.d(LOG_TAG, "NetworkObserverService: SUCCESS NETWORK");
        }

        // Send the intent for the broadcasts receivers
        Intent intent = new Intent(ACTION_OBSERVING_ENDS);
        intent.putExtra(BUNDLE_FINISH_OBSERVING, bundle);
        sendBroadcast(intent);

        ObservingReceiver.completeWakefulIntent(wakefulIntent);

        stopSelf();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            File file = new File(LOG_PATH + "/" + LOG_TAG);
            XLog.init(LogLevel.ALL, new FilePrinter.Builder(file.getPath()).build());
        } catch (Exception e){
            XLog.e(e.getMessage());
            Log.e(LOG_TAG,"NetworkObserverService: " + e.getMessage());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.holdWifiLock();

        this.wifiNetworkManager = WifiNetworkManager.getInstance();
        this.wifiNetworkManager.holdWifiLock(this);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        initBundle(intent);

        this.wakefulIntent = intent;

        return START_REDELIVER_INTENT;

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.wifiNetworkManager.releaseWifiLock();
        this.releaseWifiLock();
    }

    private void initBundle(Intent intent){
        try {
            observerToken = intent.getStringExtra(TOKEN_OBSERVER);

            sleepTime = intent.getIntExtra(SLEEP_TIME, 60000);

            wiFiBundle = ParcelableUtilsKt.toParcelable(intent.getByteArrayExtra(WIFI_BUNDLE), WiFiBundle.CREATOR);
            networkResultReceiver = intent.getParcelableExtra(RESULT_RECEIVER);

            if(wiFiBundle != null) {
                new Thread(this).start();
                Log.d(LOG_TAG, "--------- SERVICE STARTED ---------");
                XLog.d(System.currentTimeMillis()+"|  --------- SERVICE STARTED ---------");
            } else {
                Log.d(LOG_TAG, "--------- SERVICE START ERROR: WiFi Bundle is NULL ---------");
                XLog.d(System.currentTimeMillis()+"|  --------- SERVICE START ERROR: WiFi Bundle is NULL ---------");

                if (networkResultReceiver != null) {
                    networkResultReceiver.send(NetworkResultStatus.FAIL.getValue(), null);
                    Log.d(LOG_TAG, "NetworkObserverService: FAIL NETWORK");
                }
            }
        }catch (Exception e){
            Log.e(LOG_TAG, "NetworkObserverService: " + e.getMessage());
        }
    }

    private void holdWifiLock() {
        Log.d(LOG_TAG, "NetworkObserverService: HOLD WIFI LOCK");
        if(wakeLock == null){
            final PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            if (powerManager != null) {
                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG);
            } else XLog.e("POWER MANAGER NULL");
        }

        wakeLock.setReferenceCounted(false);

        if(!wakeLock.isHeld()) wakeLock.acquire(1000);

    }

    public void releaseWifiLock(){
        Log.d(LOG_TAG, "NetworkObserverService: RELEASE WIFI LOCK");
        if(wakeLock != null && wakeLock.isHeld()) wakeLock.release();
    }
}
