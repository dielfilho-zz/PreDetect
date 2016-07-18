package danielfilho.ufc.br.com.predetect.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import danielfilho.ufc.br.com.predetect.constants.PredectConstants;
import danielfilho.ufc.br.com.predetect.datas.WiFiBundle;
import danielfilho.ufc.br.com.predetect.datas.WiFiData;
import danielfilho.ufc.br.com.predetect.intefaces.WiFiListener;
import danielfilho.ufc.br.com.predetect.managers.NetworkManager;
import danielfilho.ufc.br.com.predetect.receivers.NetworkResultReceiver;

/**
 * Created by Daniel Filho on 5/31/16.
 */
public class NetworkObserverService extends Service implements Runnable{

    public static final int SERVICE_SUCCESS = 1;
    public static final int SERVICE_FAIL = 2;
    public static final int SERVICE_NO_WIFI = 3;


    private WifiManager wifiManager;
    private WiFiBundle wiFiBundle;

    private ResultReceiver networkResultReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        initBundle(intent);

        return START_REDELIVER_INTENT;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void initBundle(Intent intent){
        wiFiBundle = intent.getParcelableExtra(PredectConstants.WIFI_BUNDLE);
        networkResultReceiver = intent.getParcelableExtra(PredectConstants.RESULT_RECEIVER);
        if(wiFiBundle != null && networkResultReceiver != null) {
            new Thread(this).start();
            Log.i(PredectConstants.LOG_TAG, "--------- SERVICE STARTED ---------");
        }else{
            Log.e(PredectConstants.LOG_TAG, "--------- SERVICE START ERROR: WiFi Bundle is NULL ---------");
            if(networkResultReceiver != null){
                networkResultReceiver.send(SERVICE_FAIL, null);
            }
        }
    }

    private HashSet<WiFiData> removeReplicatedWifi(List<ScanResult> scanResults){
        HashSet<WiFiData> hashWifi = new HashSet<>();
        for(ScanResult sr : scanResults){
            WiFiData temp = new WiFiData(sr.BSSID);
            if(!hashWifi.contains(temp)) {
                double resultDistance = NetworkManager.rssiToDistance(sr.level);
                WiFiData data = new WiFiData(sr.BSSID, sr.level, resultDistance, sr.SSID);
                hashWifi.add(data);
            }else{
                Log.i(PredectConstants.LOG_TAG, "--------- There's one WiFi duplicated on WiFiManager ScanResults ---------");
            }
        }
        return hashWifi;
    }

    @Override
    public void run() {

        int observedTime = 0;

        int timeInSeconds = wiFiBundle.getObserveTime() / 1000;

        List<WiFiData> wiFiDatas = new ArrayList<>();

        for (String MAC : wiFiBundle.getWifiData()) {
            wiFiDatas.add(new WiFiData(MAC));
        }

        while(observedTime < timeInSeconds) {
            /*
                If there's a AP with 2 antennas
                there will be 2 networks with same MAC on scanResults list.

                Removing replicated MACs
             */
            HashSet<WiFiData> wiFiDataCollection = removeReplicatedWifi(wifiManager.getScanResults());

            if(wiFiDataCollection.size() > 0){
                Iterator<WiFiData> iterator = wiFiDataCollection.iterator();
                while(iterator.hasNext()){
                    WiFiData wifiScan = iterator.next();

                    for (WiFiData wiFiData : wiFiDatas) {
                        if (wiFiData.getMAC().equals(wifiScan.getMAC()) && wiFiBundle.getDistanceRange() >= wifiScan.getDistance()) {

                            int newAppear = wiFiData.getObserveCount();
                            wiFiData.setObserveCount(++newAppear);

                            double percent = (wiFiData.getObserveCount() * 100) / timeInSeconds;
                            wiFiData.setPercent(percent);
                        }
                    }

                }

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Log.e(PredectConstants.LOG_TAG, PredectConstants.SLEEP_ERROR);
                    networkResultReceiver.send(SERVICE_FAIL, null);
                }

                observedTime++;

            }else{

                networkResultReceiver.send(SERVICE_NO_WIFI, null);
                //If there's no WIFI on ScanResults, stopping service.
                stopSelf();
            }

        }
        Log.i(PredectConstants.LOG_TAG, "--------- SERVICE ENDS ---------");

        /*
            Seding an intent for all broadcasts receivers
            telling that network observing ends
         */
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(PredectConstants.WIFI_SCANNED, (ArrayList<? extends Parcelable>) wiFiDatas);
        networkResultReceiver.send(SERVICE_SUCCESS, bundle);
        stopSelf();
    }


}
