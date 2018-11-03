package br.ufc.predetect.ble.utils

import android.bluetooth.le.ScanSettings
import br.ufc.predetect.ble.data.Beacon
import br.ufc.predetect.ble.data.BeaconBundle
import br.ufc.predetect.ble.properties.BLENetworkProperties.pathLoss
import br.ufc.predetect.ble.properties.BLENetworkProperties.signalLossAtOneMeter
import br.ufc.quixada.predetect.common.utils.calculateDistance
import br.ufc.quixada.predetect.common.utils.toByteArray
import com.elvishew.xlog.XLog
import java.lang.Double.parseDouble
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*


/**
 * This formula is based in path loss model function
 *
 * @param rssi Received Signal Strength Intensity
 * @return distance based in rssi
 *
 */
fun rssiToDistance(rssi: Int): Double {

    val decimalFormat = DecimalFormat(".##")
    val dfs = DecimalFormatSymbols()
    dfs.decimalSeparator = '.'
    decimalFormat.decimalFormatSymbols = dfs

    val distance = calculateDistance(rssi.toDouble(), signalLossAtOneMeter, pathLoss)
    return parseDouble(decimalFormat.format(distance))
}
