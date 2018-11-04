package br.ufc.predetect.ble.utils

import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanSettings
import android.util.Log
import br.ufc.predetect.ble.constants.LOG_TAG
import br.ufc.predetect.ble.data.Beacon
import br.ufc.predetect.ble.data.BeaconBundle
import br.ufc.predetect.ble.filters.KalmanFilter
import br.ufc.quixada.predetect.common.utils.toByteArray


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

fun reduceScanResults(scanResults: List<Beacon>) : List<Beacon> = scanResults
        .groupBy { it.macAddress }
        .map { it ->
            val first = it.value.first()
            val rssFiltered = KalmanFilter().filter(it.value.map { it.rssi }).toInt()
            val distanceFiltered = rssiToDistance(rssFiltered)

            Log.d(LOG_TAG, "DISTANCE FILTERED: $distanceFiltered")
            Log.d(LOG_TAG, "RSS FILTERED: $rssFiltered")

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