package br.ufc.quixada.predetect.common.utils

import br.ufc.quixada.predetect.common.properties.rssiAtOneMeter
import br.ufc.quixada.predetect.common.properties.signalLoss
import java.lang.Double.parseDouble
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

/**
 * This formula is based in path loss model function
 *
 * @param rssi Received Signal Strength Intensity
 * @see <link to function>
 *
 * */
fun rssiToDistance(rssi: Int): Double {

    val decimalFormat = DecimalFormat(".#")
    val dfs = DecimalFormatSymbols()
    dfs.decimalSeparator = '.'
    decimalFormat.decimalFormatSymbols = dfs

    val distance = Math.pow(10.0, (rssiAtOneMeter - rssi) / signalLoss)
    return parseDouble(decimalFormat.format(distance))
}
