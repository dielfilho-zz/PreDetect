package br.ufc.predetect.ble.managers

import android.Manifest
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.support.annotation.RequiresPermission
import android.support.v4.content.ContextCompat
import android.util.Log
import br.ufc.predetect.ble.constants.BLE_BUNDLE
import br.ufc.predetect.ble.constants.LOG_TAG
import br.ufc.predetect.ble.constants.RESULT_RECEIVER
import br.ufc.predetect.ble.data.Beacon
import br.ufc.predetect.ble.interfaces.BeaconListener
import br.ufc.predetect.ble.interfaces.BeaconObserver
import br.ufc.predetect.ble.managers.BeaconRepository.beaconsBatch
import br.ufc.predetect.ble.receivers.BLENetworkResultReceiver
import br.ufc.predetect.ble.services.BLENetworkObserverService
import br.ufc.predetect.ble.utils.*
import br.ufc.quixada.predetect.common.domain.NetworkResultStatus
import br.ufc.quixada.predetect.common.interfaces.NetworkReceiver
import br.ufc.quixada.predetect.common.managers.NetworkResult
import br.ufc.quixada.predetect.common.utils.SLEEP_TIME
import com.elvishew.xlog.XLog

/**
 * @author Gabriel Cesar
 * @since 2018
 */

object BLENetworkManager : NetworkReceiver {
    private val scanCallback = scanCallback()
    private val listeners: MutableList<BeaconListener>? = mutableListOf()

    init {
        startXLogger()
    }

    fun unregisterListener(listener: BeaconListener) = listeners?.remove(listener)

    @RequiresPermission(ACCESS_FINE_LOCATION)
    fun registerListener(listener: BeaconListener): Boolean = listener.let {
        if (ContextCompat.checkSelfPermission(it.getListenerContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            XLog.e("${getActualDateString()} | Doesn't has permission to access location.")
            return false
        }

        listeners?.add(it)
        onListenerRegistered(it.getListenerContext())
        return true
    }

    override fun onNetworkReceive(context: Context?, intent: Intent?) {
        intent?.run {
            if (this.action == BluetoothAdapter.ACTION_STATE_CHANGED && listeners?.isNotEmpty() == true) {
                Log.d(LOG_TAG, "STARTING SCAN AGAIN")

                val btManager = context?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager?

                btManager?.adapter?.bluetoothLeScanner?.run {
                    Log.d(LOG_TAG, "STOP PREVIOUS SCAN CALLBACK")
                    this.stopScan(scanCallback)

                    Log.d(LOG_TAG, "START SCAN CALLBACK")
                    this.startScan(emptyList(), scanSettings(), scanCallback)
                }
            }
        }
    }

    private fun onListenerRegistered(context: Context) {

        Log.i(LOG_TAG, "REGISTER LISTENER")

        val btManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager?

        if (btManager == null) {
            Log.e(LOG_TAG, "No Bluetooth LE found.")
        }

        btManager?.adapter?.bluetoothLeScanner?.run {
            Log.d(LOG_TAG, "STOP PREVIOUS SCAN CALLBACK")
            this.stopScan(scanCallback)

            Log.d(LOG_TAG, "START SCAN CALLBACK")
            this.startScan(emptyList(), scanSettings(), scanCallback)
        }
    }

    fun observeNetwork(observer: BeaconObserver, btMACsToObserve: List<String>, timeInMinutes: Int, maxRangeInMeters: Double, intervalTimeInMinutes: Int = 1) {
        if (btMACsToObserve.isNotEmpty()) {

            Log.i(LOG_TAG, "BLENetworkManager: STARTING TO OBSERVE NETWORK FOR $intervalTimeInMinutes MINUTES")

            val serviceIntent = Intent(observer.getListenerContext(), BLENetworkObserverService::class.java)

            val sleepTimeOneMinute: Long = 60 * 1000
            serviceIntent.putExtra(SLEEP_TIME, sleepTimeOneMinute * intervalTimeInMinutes)

            serviceIntent.putExtra(BLE_BUNDLE, createBeaconBundle(btMACsToObserve, timeInMinutes * sleepTimeOneMinute, maxRangeInMeters))

            val resultReceiver = BLENetworkResultReceiver(observer)

            serviceIntent.putExtra(RESULT_RECEIVER, resultReceiver)

            observer.getListenerContext().startService(serviceIntent)

        }

        else  {
            Log.i(LOG_TAG, "BLENetworkManager: WIFI LIST IS NULL")
            observer.onObservingEnds(NetworkResult(NetworkResultStatus.UNDEFINED, null, emptyMap()))
        }

    }

    private fun scanCallback(): ScanCallback = object : ScanCallback() {

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            Log.d(LOG_TAG, "BLENetworkManager: HAS BATCH ${results?.size}")
            super.onBatchScanResults(results)
        }

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            Log.d(LOG_TAG, "BLENetworkManager: HAS RESULT")

            result?.run {

                val name: String = this.device.name ?: "UNKNOWN"
                val macAddress: String = this.device.address
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

                XLog.i("${getActualDateString()} | HAS RESULT ON SCAN | $beacon")

                updateListeners()
            }

        }

        override fun onScanFailed(errorCode: Int) {

            Log.e(LOG_TAG, when (errorCode) {
                SCAN_FAILED_ALREADY_STARTED -> "ScanCallback: Fails to start scan as BLE scan with the same settings is already started by the app"
                SCAN_FAILED_APPLICATION_REGISTRATION_FAILED -> "ScanCallback: Fails to start scan as app cannot be registered."
                SCAN_FAILED_FEATURE_UNSUPPORTED -> "ScanCallback: Fails to start power optimized scan as this feature is not supported."
                SCAN_FAILED_INTERNAL_ERROR -> "ScanCallback: Fails to start scan due an internal error."
                else -> "ScanCallback: Unknown error."
            })

            Log.d(LOG_TAG, "DISABLING BLUETOOTH AND WAIT 12 SECONDS TO ENABLED AGAIN")

            if (BluetoothAdapter.getDefaultAdapter().disable()) {
                sleepThread(12)
                BluetoothAdapter.getDefaultAdapter().enable()
            }

        }

    }

    private fun updateListeners(timeToWaitInSeconds: Long = 12) {
        var updateListeners = false

        // Search in batch and if has 100 or more values in some list so update listeners
        val maximum = beaconsBatch.values.map { it.size }.max() ?: 0

        if (maximum >= 100)
            updateListeners = true

        if (updateListeners) {
            Log.d(LOG_TAG, "NOTIFY LISTENERS AND WAIT $timeToWaitInSeconds SECONDS")

            notifyWiFiListeners(beaconsBatch.values.map { filterBeacon(it) }.toList())

            beaconsBatch.clear()

            sleepThread(timeToWaitInSeconds)
        }
    }

    private fun notifyWiFiListeners(wifiData: List<Beacon>) = listeners?.forEach { it.onChange(wifiData) }
}