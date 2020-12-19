package danielfilho.ufc.br.com.predetect.managers;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.elvishew.xlog.XLog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import br.ufc.quixada.predetect.common.domain.NetworkResultStatus;
import br.ufc.quixada.predetect.common.interfaces.NetworkReceiver;
import br.ufc.quixada.predetect.common.managers.NetworkResult;
import danielfilho.ufc.br.com.predetect.datas.WiFiData;
import danielfilho.ufc.br.com.predetect.intefaces.WiFiListener;
import danielfilho.ufc.br.com.predetect.intefaces.WiFiObserver;
import danielfilho.ufc.br.com.predetect.receivers.WiFiNetworkResultReceiver;
import danielfilho.ufc.br.com.predetect.services.NetworkObserverService;
import danielfilho.ufc.br.com.predetect.utils.NetworkUtils;

import static br.ufc.quixada.predetect.common.utils.ConstantsKt.SLEEP_TIME;
import static br.ufc.quixada.predetect.common.utils.ConstantsKt.TOKEN_OBSERVER;
import static danielfilho.ufc.br.com.predetect.constants.PreDetectConstants.LOG_TAG;
import static danielfilho.ufc.br.com.predetect.constants.PreDetectConstants.RESULT_RECEIVER;
import static danielfilho.ufc.br.com.predetect.constants.PreDetectConstants.WIFI_BUNDLE;

/**
 *
 * @author Daniel Filho
 * @since 2016
 *
 * Updated by Gabriel Cesar, 2018
 *
 */
public class WifiNetworkManager implements NetworkReceiver {

    private List<WiFiListener> listeners;
    private static WifiNetworkManager instance;

    private WifiManager.WifiLock wifiLock = null;

    public static WifiNetworkManager getInstance(){
        if(instance == null){
            instance = new WifiNetworkManager();
        }
        return instance;
    }

    private WifiNetworkManager() {
        this.listeners = new ArrayList<>();
    }

    private void notifyWiFiListeners(final List<WiFiData> wifiData){
        for(WiFiListener nl : listeners){
            nl.onChange(wifiData);
        }
    }

    public String observeNetwork(WiFiObserver observer, List<String> wifiMACsToObserve, int timeInMinutes, double maxRangeInMeters, int sleepTimeInMinutes){
        String token = "token" + UUID.randomUUID();

        if(wifiMACsToObserve.size() > 0) {

            Log.i(LOG_TAG, "WifiNetworkManager: STARTING TO OBSERVE NETWORK FOR " + sleepTimeInMinutes + " MINUTES");

            Intent serviceIntent = new Intent(observer.getListenerContext(), NetworkObserverService.class);

            final int sleepTimeOneMinute = 60 * 1000;
            serviceIntent.putExtra(SLEEP_TIME, sleepTimeOneMinute * sleepTimeInMinutes);

            serviceIntent.putExtra(TOKEN_OBSERVER, token);

            serviceIntent.putExtra(WIFI_BUNDLE, NetworkUtils.createWiFiBundle(wifiMACsToObserve, timeInMinutes * sleepTimeOneMinute, maxRangeInMeters));

            WiFiNetworkResultReceiver resultReceiver = new WiFiNetworkResultReceiver(observer);

            serviceIntent.putExtra(RESULT_RECEIVER, resultReceiver);

            observer.getListenerContext().startService(serviceIntent);

        } else {
            Log.i(LOG_TAG, "WifiNetworkManager: WIFI LIST IS NULL");

            observer.onObservingEnds(new NetworkResult<>(NetworkResultStatus.UNDEFINED, null, Collections.emptyMap(), token));
        }
        return token;
    }

    public void registerListener(WiFiListener listener){
        this.listeners.add(listener);
        listener.onChange(onWiFiListenerRegistered(listener.getListenerContext()));
    }

    private List<WiFiData> onWiFiListenerRegistered(Context context){
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            throw new NullPointerException("Location is required");
        }

        Log.i(LOG_TAG, "WifiNetworkManager: UPDATE WIFI LISTENERS");

        WifiManager manager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (manager != null) {
            List<ScanResult> resultList = manager.getScanResults() != null ? manager.getScanResults() : new ArrayList<>();

            List<WiFiData> wiFiData = new ArrayList<>();

            if (resultList.size() > 0) {
                for (ScanResult result : resultList) {
                    WiFiData data = new WiFiData(result.BSSID, result.level, NetworkUtils.rssiToDistance(result.level), result.SSID);
                    wiFiData.add(data);
                }
            }
            return wiFiData;
        }
        return new ArrayList<>();
    }

    public void unregisterListener(WiFiListener listener) {
        this.listeners.remove(listener);
    }

    @Override
    public void onNetworkReceive(Context context, Intent intent) {

        if(WifiManager.RSSI_CHANGED_ACTION.equals(intent.getAction())) {
            Log.i(LOG_TAG, "WifiNetworkManager: RSSi CHANGED - UPDATE WIFI LISTENERS");

            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

            List<WiFiData> wifiList = new ArrayList<>();

            if (wifiManager != null) {
                for (ScanResult result : wifiManager.getScanResults()) {
                    WiFiData wifi = new WiFiData(result.BSSID, result.level, NetworkUtils.rssiToDistance(result.level), result.SSID);
                    wifiList.add(wifi);
                }
            }

            notifyWiFiListeners(wifiList);
        }
    }

    public void holdWifiLock(Context context){
        if (wifiLock == null) {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

            if (wifiManager != null) {
                wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, LOG_TAG);
            }
        }

        wifiLock.setReferenceCounted(false);

        if(!wifiLock.isHeld()) wifiLock.acquire();

        XLog.d(System.currentTimeMillis() + " |  -------------- ACQUIRE WIFI LOCK ------------");
        Log.d(LOG_TAG, "WifiNetworkManager: ACQUIRE WIFI LOCK");

    }

    public void releaseWifiLock(){
        if(wifiLock == null){
            Log.d(LOG_TAG, "WifiNetworkManager: WIFI LOCK WAS NOT CREATED");
            XLog.d(System.currentTimeMillis() + "|  -------------- WIFI LOCK WAS NOT CREATED ------------");
        } else {
            if (wifiLock.isHeld()) wifiLock.release();
            Log.d(LOG_TAG, "WifiNetworkManager: RELEASE WIFI LOCK");
            XLog.d(System.currentTimeMillis() + "|  -------------- RELEASE WIFI LOCK ------------");
        }
    }

}
