package danielfilho.ufc.br.com.predetect.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

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

}
