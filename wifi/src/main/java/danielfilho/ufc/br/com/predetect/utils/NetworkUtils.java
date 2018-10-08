package danielfilho.ufc.br.com.predetect.utils;

import android.net.wifi.ScanResult;

import com.elvishew.xlog.XLog;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import br.ufc.quixada.predetect.common.utils.ParcelableUtilsKt;
import danielfilho.ufc.br.com.predetect.datas.WiFiBundle;
import danielfilho.ufc.br.com.predetect.datas.WiFiData;

import static br.ufc.quixada.predetect.common.utils.NetworkUtilsKt.calculateDistance;
import static danielfilho.ufc.br.com.predetect.properties.NetworkProperties.rssiAtOneMeter;
import static danielfilho.ufc.br.com.predetect.properties.NetworkProperties.signalLoss;
import static java.lang.Double.parseDouble;

public abstract class NetworkUtils {

    /**
     * This formula is based in path loss model function
     *
     * @param rssi Received Signal Strength Intensity
     * @return distance based in rssi
     *
     * */
    public static Double rssiToDistance(Integer rssi) {

        DecimalFormat decimalFormat = new DecimalFormat(".##");
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setDecimalSeparator('.');
        decimalFormat.setDecimalFormatSymbols(dfs);

        Double distance = calculateDistance(rssi, rssiAtOneMeter, signalLoss);
        return parseDouble(decimalFormat.format(distance));
    }

    /**
     *  If there's an AP with 2 antennas there will be 2 networks with same MAC on scanResults list.
     * */
    public static HashSet<WiFiData> mergeWifiData(List<ScanResult> scanResults, HashSet<WiFiData> wiFiDataSet){
        HashSet<WiFiData> hashWifi = new HashSet<>();
        for(ScanResult sr : scanResults){
            WiFiData temp = new WiFiData(sr.BSSID);

            if (!hashWifi.contains(temp) && wiFiDataSet.contains(temp)) {
                double resultDistance = NetworkUtils.rssiToDistance(sr.level);
                WiFiData data = new WiFiData(sr.BSSID, sr.level, resultDistance, sr.SSID);


                for (WiFiData oldData: wiFiDataSet) {
                    if (oldData.equals(data)) {
                        data.setObserveCount(oldData.getObserveCount());
                        data.setPercent(oldData.getPercent());
                        break;
                    }
                }

                XLog.d(String.format(Locale.ENGLISH, "%s,%s,%d,%f", sr.BSSID, sr.SSID, sr.level, resultDistance));

                hashWifi.add(data);
            }
        }
        return hashWifi;
    }

    public static byte[] createWiFiBundle(List<String> wifiData, int duration, double distance){
        WiFiBundle wiFiBundle = new WiFiBundle(wifiData, duration, distance);
        return ParcelableUtilsKt.toByteArray(wiFiBundle);
    }

}
