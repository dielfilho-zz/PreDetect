package br.ufc.predetect.ble.utils

import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanSettings
import android.util.Log
import br.ufc.predetect.ble.constants.LOG_TAG
import br.ufc.predetect.ble.data.Beacon
import br.ufc.predetect.ble.data.BeaconBundle
import br.ufc.predetect.ble.filters.KalmanFilter
import br.ufc.quixada.predetect.common.utils.toByteArray
import com.elvishew.xlog.XLog
import java.util.*


fun filterBeacon(advertisingPackets : List<Beacon>) : Beacon {
    val first = advertisingPackets.first()
    val rssFiltered = KalmanFilter().filter(advertisingPackets.map { it.rssi }).toInt()
    val distanceFiltered = rssiToDistance(rssFiltered)

    Log.d(LOG_TAG, "DISTANCE FILTERED: $distanceFiltered")
    Log.d(LOG_TAG, "RSS FILTERED: $rssFiltered")

    return Beacon(
            macAddress = first.macAddress,
            name = first.name,
            rssi = rssFiltered,
            distance = distanceFiltered
    )
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

        btCollection.add(oldData.copy(name = if (oldData.name.isNullOrBlank()) "UNKNOWN" else oldData.name))
    }

    return btCollection
}

fun reduceScanResults(scanResults: List<Beacon>) : List<Beacon> = scanResults
        .groupBy { it.macAddress }
        .map { it ->
            val first = it.value.first()
            val rssFiltered = KalmanFilter().filter(it.value.map { it.rssi }).toInt()
            val distanceFiltered = rssiToDistance(rssFiltered)

            Log.d(LOG_TAG, "BLENetworkObserverService: DISTANCE FILTERED: $distanceFiltered")
            Log.d(LOG_TAG, "BLENetworkObserverService: RSS FILTERED: $rssFiltered")

            Beacon(
                    macAddress = it.key,
                    name = first.name,
                    rssi = rssFiltered,
                    distance = distanceFiltered
            )
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

fun getMessageByErrorCodeInScanResult (errorCode : Int) : String = when (errorCode) {
    ScanCallback.SCAN_FAILED_ALREADY_STARTED -> "ScanCallback: Fails to start scan as BLE scan with the same settings is already started by the app"
    ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED -> "ScanCallback: Fails to start scan as app cannot be registered."
    ScanCallback.SCAN_FAILED_FEATURE_UNSUPPORTED -> "ScanCallback: Fails to start power optimized scan as this feature is not supported."
    ScanCallback.SCAN_FAILED_INTERNAL_ERROR -> "ScanCallback: Fails to start scan due an internal error."
    else -> "ScanCallback: Unknown error."
}