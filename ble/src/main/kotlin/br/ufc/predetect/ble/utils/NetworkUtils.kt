package br.ufc.predetect.ble.utils

import android.bluetooth.le.ScanSettings
import br.ufc.predetect.ble.domain.Beacon
import br.ufc.predetect.ble.domain.BeaconBundle
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

/**
 * Remove duplicated and merge with previous results
 */
fun mergeBLEData(scanResults: List<Beacon>, wiFiDataSet: HashSet<Beacon>): HashSet<Beacon> {
    val btCollection = HashSet<Beacon>()

    for (oldData in wiFiDataSet) {
        for (sr in scanResults) {

            if (oldData.macAddress == sr.macAddress) {

                val data = sr.copy(
                        observeCount = oldData.observeCount,
                        percent = oldData.percent
                )

                btCollection.add(data)

                XLog.d(String.format(Locale.ENGLISH, "%s,%s,%d,%f", sr.macAddress, sr.name, sr.rssi, sr.distance))

                break
            }
        }

        btCollection.add(oldData)
    }

    return btCollection
}

fun createBeaconBundle(data: List<String>, duration: Long, distance: Double): ByteArray =
        toByteArray(BeaconBundle(data, duration, distance))

fun scanSettings() : ScanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
        .build()

fun isValidBeacon(beacon: Beacon): Boolean {
    return beacon.name != null &&
            beacon.distance > 0.001
}