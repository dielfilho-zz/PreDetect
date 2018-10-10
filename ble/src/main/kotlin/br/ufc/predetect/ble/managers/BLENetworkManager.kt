package br.ufc.predetect.ble.managers

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.support.annotation.RequiresPermission
import android.util.Log
import br.ufc.predetect.ble.constants.BLE_BUNDLE
import br.ufc.predetect.ble.constants.LOG_TAG
import br.ufc.predetect.ble.constants.RESULT_RECEIVER
import br.ufc.predetect.ble.domain.Beacon
import br.ufc.predetect.ble.filters.KalmanFilter
import br.ufc.predetect.ble.interfaces.BeaconListener
import br.ufc.predetect.ble.interfaces.BeaconObserver
import br.ufc.predetect.ble.managers.BeaconRepository.beaconsBatch
import br.ufc.predetect.ble.receivers.BLENetworkResultReceiver
import br.ufc.predetect.ble.services.BLENetworkObserverService
import br.ufc.predetect.ble.utils.createBeaconBundle
import br.ufc.predetect.ble.utils.rssiToDistance
import br.ufc.predetect.ble.utils.scanSettings
import br.ufc.quixada.predetect.common.domain.NetworkResultStatus
import br.ufc.quixada.predetect.common.interfaces.NetworkReceiver
import br.ufc.quixada.predetect.common.managers.NetworkResult
import br.ufc.quixada.predetect.common.utils.SLEEP_TIME

/**
 * @author Gabriel Cesar
 * @since 2018
 */
object BLENetworkManager : NetworkReceiver {
    private val scanCallback = scanCallback()
    private lateinit var btManager: BluetoothManager
    private val listeners: MutableList<BeaconListener>? = mutableListOf()

    private fun notifyWiFiListeners(wifiData: List<Beacon>) = listeners?.forEach { it.onChange(wifiData) }

    fun registerListener(listener: BeaconListener) = listener.apply {
        listeners?.add(this)
        onListenerRegistered(this.getListenerContext())
    }

    fun unregisterListener(listener: BeaconListener) = listeners?.remove(listener)

    override fun onNetworkReceive(context: Context?, intent: Intent?) {
        intent?.run {
            if (this.action == BluetoothAdapter.ACTION_STATE_CHANGED && listeners?.isNotEmpty() == true) {
                Log.d(LOG_TAG, "BLENetworkManager: STARTING SCAN AGAIN")
                btManager.adapter?.bluetoothLeScanner?.startScan(emptyList(), scanSettings(), scanCallback)
            }
        }
    }

    @Throws(NullPointerException::class)
    @RequiresPermission(android.Manifest.permission.BLUETOOTH_ADMIN)
    private fun onListenerRegistered(context: Context) {
        btManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        btManager.adapter?.bluetoothLeScanner?.startScan(emptyList(), scanSettings(), scanCallback)
    }

    fun observeNetwork(observer: BeaconObserver, btMACsToObserve: List<String>, timeInMinutes: Int, maxRangeInMeters: Double, intervalTimeInMinutes: Int = 1) {
        if (btMACsToObserve.isNotEmpty()) {

            Log.i(LOG_TAG, "BLENetworkManager: STARTING TO OBSERVE NETWORK FOR $intervalTimeInMinutes MINUTES")

            val serviceIntent = Intent(observer.getListenerContext(), BLENetworkObserverService::class.java)

            val sleepTimeOneMinute : Long = 60 * 1000
            serviceIntent.putExtra(SLEEP_TIME, sleepTimeOneMinute * intervalTimeInMinutes)

            serviceIntent.putExtra(BLE_BUNDLE, createBeaconBundle(btMACsToObserve, timeInMinutes * sleepTimeOneMinute, maxRangeInMeters))

            val resultReceiver = BLENetworkResultReceiver(observer)

            serviceIntent.putExtra(RESULT_RECEIVER, resultReceiver)

            observer.getListenerContext().startService(serviceIntent)

        } else {
            Log.i(LOG_TAG, "BLENetworkManager: WIFI LIST IS NULL")
            observer.onObservingEnds(NetworkResult(NetworkResultStatus.UNDEFINED, null, emptyMap()))
        }
    }

    private fun scanCallback() : ScanCallback = object : ScanCallback() {

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            Log.i(LOG_TAG, "BLENetworkManager: HAS BATCH ${results?.size}")
            super.onBatchScanResults(results)
        }

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            Log.i(LOG_TAG, "BLENetworkManager: HAS RESULT ")

            result?.run {

                val name: String = this.device.name ?: "UNKNOWN"
                val macAddress : String = this.device.address
                val rss = this.rssi
                val distance = rssiToDistance(rss)

                val beacon = Beacon(
                        name = name,
                        macAddress = macAddress,
                        rssi = rss,
                        distance = distance
                )

                if (!beaconsBatch.contains(macAddress)) {
                    beaconsBatch[macAddress] = mutableListOf()
                }

                beaconsBatch[macAddress]?.add(beacon)

                updateListeners()
            }

            super.onScanResult(callbackType, result)
        }

        // Error necessary disable and enable bt again
        override fun onScanFailed(errorCode: Int) {
            Log.e(LOG_TAG, "BLENetworkManager: ERROR IN SCAN $errorCode")
            if (errorCode == 2 && BluetoothAdapter.getDefaultAdapter().disable()) {
                Thread.sleep(12_000)
                BluetoothAdapter.getDefaultAdapter().enable()
            }
            super.onScanFailed(errorCode)
        }
    }

    private fun updateListeners() {
        var updateListeners = false

        // Search in batch and if has 100 or more values in some list so update listeners
        val maximum = beaconsBatch.values.map { it.size }.max() ?: 0

        if (maximum >= 100)
            updateListeners = true

        if (updateListeners) {
            notifyWiFiListeners(beaconsBatch.values.map { filterBeacon(it) }.toList())

            beaconsBatch.clear()

            Thread.sleep(12_000)
        }
    }

    private fun filterBeacon(advertisingPackets : List<Beacon>) : Beacon {
        val first = advertisingPackets.first()
        val rssFiltered = KalmanFilter().filter(advertisingPackets.map { it.rssi }).toInt()
        val distanceFiltered = rssiToDistance(rssFiltered)

        Log.d(LOG_TAG, "BLENetworkManager: DISTANCE FILTERED: $distanceFiltered")
        Log.d(LOG_TAG, "BLENetworkManager: RSS FILTERED: $rssFiltered")

        return Beacon(
                macAddress = first.macAddress,
                name = first.name,
                rssi = rssFiltered,
                distance = distanceFiltered
        )
    }
}