package br.ufc.predetect.ble.utils

import br.ufc.predetect.ble.properties.NetworkPropertiesUtils.rssiAtOneMeter
import br.ufc.predetect.ble.properties.NetworkPropertiesUtils.signalLoss
import br.ufc.quixada.predetect.common.utils.calculateDistance
import java.lang.Double.parseDouble
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols


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

    val distance = calculateDistance(rssi.toDouble(), rssiAtOneMeter.toDouble(), signalLoss)
    return parseDouble(decimalFormat.format(distance))
}
