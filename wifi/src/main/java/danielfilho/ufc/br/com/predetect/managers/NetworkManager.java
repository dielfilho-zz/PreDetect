package danielfilho.ufc.br.com.predetect.managers;


import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.elvishew.xlog.XLog;

import java.util.ArrayList;
import java.util.List;

import br.ufc.quixada.predetect.common.domain.NetworkResultStatus;
import br.ufc.quixada.predetect.common.interfaces.NetworkListener;
import br.ufc.quixada.predetect.common.interfaces.NetworkReceiver;
import br.ufc.quixada.predetect.common.managers.NetworkResult;
import br.ufc.quixada.predetect.common.utils.ParcelableUtilsKt;
import danielfilho.ufc.br.com.predetect.datas.WiFiBundle;
import danielfilho.ufc.br.com.predetect.datas.WiFiData;
import danielfilho.ufc.br.com.predetect.intefaces.WiFiListener;
import danielfilho.ufc.br.com.predetect.intefaces.WiFiObserver;
import danielfilho.ufc.br.com.predetect.receivers.WiFiNetworkResultReceiver;
import danielfilho.ufc.br.com.predetect.services.NetworkObserverService;
import danielfilho.ufc.br.com.predetect.utils.NetworkUtils;

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
public class NetworkManager implements NetworkReceiver {

    private static NetworkManager instance;

    private List<NetworkListener> listeners;

    private WifiManager.WifiLock wifiLock = null;

    public static NetworkManager getInstance(){
        if(instance == null){
            instance = new NetworkManager();
        }
        return instance;
    }

    private NetworkManager() {
        this.listeners = new ArrayList<>();
        this.listeners = new ArrayList<>();
    }

    private void notifyWiFiListeners(List<WiFiData> wifiData){
        for(NetworkListener nl : listeners){
            if(nl instanceof WiFiListener){
                ((WiFiListener) nl).onWiFiChange(wifiData);
            }
        }
    }

    public void observeNetwork(WiFiObserver observer, List<WiFiData> wiFiData, int time, double rangeDistance){
        if(wiFiData.size() > 0) {
            List<String> wifiMACs = new ArrayList<>();
            for (WiFiData wifi : wiFiData) {
                wifiMACs.add(wifi.getMAC());
            }
            WiFiBundle bundle = new WiFiBundle(wifiMACs, time, rangeDistance);
            Intent serviceIntent = new Intent(observer.getListenerContext(), NetworkObserverService.class);

            serviceIntent.putExtra(WIFI_BUNDLE, bundle);

            WiFiNetworkResultReceiver resultReceiver = new WiFiNetworkResultReceiver(observer);

            serviceIntent.putExtra(RESULT_RECEIVER, resultReceiver);

            observer.getListenerContext().startService(serviceIntent);

        } else {
            Log.i(LOG_TAG, "-------- WiFi list is NULL --------");
            observer.onObservingEnds(new NetworkResult<WiFiData>(NetworkResultStatus.UNDEFINED, null));
        }
    }

    public void registerListener(NetworkListener listener){
        this.listeners.add(listener);
        if(listener instanceof WiFiListener){
            ((WiFiListener) listener).onWiFiChange(onWiFiListenerRegistered(listener.getListenerContext()));
        }
    }

    private List<WiFiData> onWiFiListenerRegistered(Context context){
        WifiManager manager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (manager != null) {
            List<ScanResult> resultList = manager.getScanResults() != null ? manager.getScanResults() : new ArrayList<ScanResult>();

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

    public void unregisterListener(NetworkListener listener) {
        this.listeners.remove(listener);
    }

    @Override
    public void onNetworkReceive(Context context, Intent intent) {

        if(WifiManager.RSSI_CHANGED_ACTION.equals(intent.getAction())) {

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

    public byte[] createWiFiBundle(List<String> wifiData, int duration, double distance){
        WiFiBundle wiFiBundle = new WiFiBundle(wifiData, duration, distance);
        return ParcelableUtilsKt.toByteArray(wiFiBundle);
    }

    public void holdWifiLock(Context context){
        if(wifiLock == null){
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wifiManager != null) {
                wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_FULL, LOG_TAG);
            }
        }

        wifiLock.setReferenceCounted(false);

        if(!wifiLock.isHeld())
            wifiLock.acquire();

        XLog.d(System.currentTimeMillis() + "|  -------------- ACQUIRE WIFI LOCK ------------");
        Log.d(LOG_TAG, "-------------- ACQUIRE WIFI LOCK ------------");

    }

    public void releaseWifiLock(){
        if(wifiLock == null){
            Log.d(LOG_TAG, "-------------- WIFI LOCK WAS NOT CREATED ------------");
            XLog.d(System.currentTimeMillis() + "|  -------------- WIFI LOCK WAS NOT CREATED ------------");
        } else{
            if(wifiLock.isHeld())
                wifiLock.release();
            Log.d(LOG_TAG, "-------------- RELEASE WIFI LOCK ------------");
            XLog.d(System.currentTimeMillis() + "|  -------------- RELEASE WIFI LOCK ------------");
        }
    }

}
