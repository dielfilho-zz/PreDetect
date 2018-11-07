package br.ufc.predetect.ble.managers

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothAdapter.STATE_ON
import android.bluetooth.BluetoothAdapter.STATE_TURNING_ON
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
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
import br.ufc.quixada.predetect.common.utils.TOKEN_OBSERVER
import com.elvishew.xlog.XLog
import java.util.*


/**
 * @author Gabriel Cesar
 * @since 2018
 */

object BLENetworkManager : NetworkReceiver {
    private const val TWELVE_SECONDS = 12_000L

    private val scanCallback = scanCallback()
    private val listeners: MutableList<BeaconListener>? = mutableListOf()

    init {
        startXLogger()
    }

    fun unregisterListener(listener: BeaconListener) = listeners?.remove(listener)

    fun registerListener(listener: BeaconListener) = listener.let {
        listeners?.add(it)
        onListenerRegistered(it.getListenerContext())
    }

    override fun onNetworkReceive(context: Context?, intent: Intent?) {
        intent?.run {
            if (this.action == BluetoothAdapter.ACTION_STATE_CHANGED && listeners?.isNotEmpty() == true) {
                Log.d(LOG_TAG, "STARTING SCAN AGAIN")

                val btManager = context?.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager?

                btManager?.adapter?.run {
                    if (state == STATE_TURNING_ON) {
                        sleepThread(TWELVE_SECONDS)
                    }
                    if (state == STATE_ON) {
                        bluetoothLeScanner?.run {
                            Log.d(LOG_TAG, "STOP PREVIOUS SCAN CALLBACK")
                            this.stopScan(scanCallback)

                            Log.d(LOG_TAG, "START SCAN CALLBACK")
                            this.startScan(emptyList(), scanSettings(), scanCallback)
                        }
                    }
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

        if (btManager?.adapter == null) {
            Log.e(LOG_TAG, "Bluetooth Adapter is off.")
        }

        btManager?.adapter?.run {
            if (state == STATE_TURNING_ON) {
                Log.d(LOG_TAG, "WAIT 12 SECONDS TO ENABLE BLUETOOTH")
                sleepThread(TWELVE_SECONDS)
            }
            if (state == STATE_ON) {
                Log.d(LOG_TAG, "BLUETOOTH ENABLED")

                bluetoothLeScanner?.run {
                    Log.d(LOG_TAG, "STOP PREVIOUS SCAN CALLBACK")
                    this.stopScan(scanCallback)

                    Log.d(LOG_TAG, "START SCAN CALLBACK")
                    this.startScan(emptyList(), scanSettings(), scanCallback)
                }
            }
        }

    }

    fun observeNetwork(observer: BeaconObserver, btMACsToObserve: List<String>, timeInMinutes: Int, maxRangeInMeters: Double, intervalTimeInMinutes: Int) : String {
        val token = "token${UUID.randomUUID()}"

        if (btMACsToObserve.isNotEmpty()) {

            Log.i(LOG_TAG, "BLENetworkManager: STARTING TO OBSERVE NETWORK FOR EACH $intervalTimeInMinutes MINUTES")

            val serviceIntent = Intent(observer.getListenerContext(), BLENetworkObserverService::class.java)

            val sleepTimeOneMinute: Long = 60_000

            serviceIntent.putExtra(SLEEP_TIME, intervalTimeInMinutes * sleepTimeOneMinute)

            serviceIntent.putExtra(TOKEN_OBSERVER, token)

            serviceIntent.putExtra(BLE_BUNDLE, createBeaconBundle(btMACsToObserve, timeInMinutes * sleepTimeOneMinute, maxRangeInMeters))

            val resultReceiver = BLENetworkResultReceiver(observer)

            serviceIntent.putExtra(RESULT_RECEIVER, resultReceiver)

            observer.getListenerContext().startService(serviceIntent)

        }

        else  {
            Log.i(LOG_TAG, "BLENetworkManager: WIFI LIST IS NULL")
            observer.onObservingEnds(NetworkResult(NetworkResultStatus.UNDEFINED, null, emptyMap(), token))
        }

        return token
    }

    private fun scanCallback(): ScanCallback = object : ScanCallback() {

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            Log.d(LOG_TAG, "BLENetworkManager: HAS BATCH ${results?.size}")
            super.onBatchScanResults(results)
        }

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
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
                Log.d(LOG_TAG, "BLENetworkManager: ${getActualDateString()} | HAS RESULT ON SCAN | $beacon")

                updateListeners()
            }

        }

        override fun onScanFailed(errorCode: Int) {

            Log.e(LOG_TAG, getMessageByErrorCodeInScanResult(errorCode))

            Log.d(LOG_TAG, "DISABLING BLUETOOTH AND WAIT 12 SECONDS TO ENABLED AGAIN")

            if (BluetoothAdapter.getDefaultAdapter().disable()) {
                sleepThread(TWELVE_SECONDS)
                BluetoothAdapter.getDefaultAdapter().enable()
            }

        }

    }

    private fun updateListeners(timeToWaitInMillis: Long = TWELVE_SECONDS) {
        var updateListeners = false

        // Search in batch and if has 100 or more values in some list so update listeners
        val maximum = beaconsBatch.values.map { it.size }.max() ?: 0

        if (maximum >= 100)
            updateListeners = true

        if (updateListeners) {
            Log.d(LOG_TAG, "NOTIFY LISTENERS AND WAIT $timeToWaitInMillis MILLISECONDS")

            notifyWiFiListeners(beaconsBatch.values.map { filterBeacon(it) }.toList())

            beaconsBatch.clear()

            sleepThread(timeToWaitInMillis)
        }
    }

    private fun notifyWiFiListeners(wifiData: List<Beacon>) = listeners?.forEach { it.onChange(wifiData) }
}