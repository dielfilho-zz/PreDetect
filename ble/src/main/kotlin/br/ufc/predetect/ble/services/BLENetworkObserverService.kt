package br.ufc.predetect.ble.services

import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.os.PowerManager
import android.os.ResultReceiver
import android.util.Log
import br.ufc.predetect.ble.constants.*
import br.ufc.predetect.ble.data.Beacon
import br.ufc.predetect.ble.data.BeaconBundle
import br.ufc.predetect.ble.managers.BLENetworkManager
import br.ufc.predetect.ble.utils.*
import br.ufc.quixada.predetect.common.domain.NetworkResultStatus
import br.ufc.quixada.predetect.common.utils.OBSERVED_HISTORY
import br.ufc.quixada.predetect.common.utils.SLEEP_TIME
import br.ufc.quixada.predetect.common.utils.TOKEN_OBSERVER
import br.ufc.quixada.predetect.common.utils.toParcelable
import com.elvishew.xlog.XLog
import java.util.ArrayList
import java.util.HashMap
import kotlin.collections.HashSet

/**
 * @author Gabriel Cesar
 * @since 2018
 */
class BLENetworkObserverService : Service(), Runnable {
    private var observerToken : String? = null
    private var sleepTime: Long = 60_000
    private var btManager: BluetoothManager? = null
    private var beaconBundle  : BeaconBundle? = null
    private var networkManager: BLENetworkManager? = null

    private var networkResultReceiver: ResultReceiver? = null

    private var wakeLock: PowerManager.WakeLock? = null

    init {
        startXLogger()
    }

    companion object {
        var timeToObserve = 0L
        var observedTime = 0L
        var bleData = HashSet<Beacon>()
        var scanResults = mutableListOf<Beacon>()
    }

    override fun run() {
        val bundle = Bundle()
        bundle.putString(TOKEN_OBSERVER, observerToken)

        if (beaconBundle == null) { Log.e(LOG_TAG, "BUNDLE IS NULL") }

        beaconBundle?.observeTime?.div(sleepTime)?.run { timeToObserve = this }

        bleData.addAll(beaconBundle?.beaconData?.map { Beacon(macAddress = it) }?.toHashSet() ?: hashSetOf())

        val observerHistory = HashMap<String, MutableList<Beacon>>()

        while (observedTime < timeToObserve) {

            btManager?.adapter?.bluetoothLeScanner?.run {
                val scanCallback = scanCallback()

                Log.d(LOG_TAG, "BLENetworkObserverService: START SCAN")
                this.startScan(emptyList(), scanSettings(), scanCallback)

                sleepThread(12)

                Log.d(LOG_TAG, "BLENetworkObserverService: STOP SCAN")
                this.stopScan(scanCallback)

                sleepThread(2)

                val scansResult = reduceScanResults(scanResults)
                Log.d(LOG_TAG, "BLENetworkObserverService: SCANS RESULTS ${scansResult.size}")

                scanResults.clear()

                beaconBundle?.distanceRange?.let { distanceRange ->

                    bleData.forEach { beacon ->
                        scansResult.forEach { scan ->
                            if ( scan.macAddress == beacon.macAddress && isValidBeacon(scan) && scan.distance <= distanceRange) {
                                beacon.name = beacon.name.orElse(scan.name)
                                beacon.distance = scan.distance
                                beacon.rssi = scan.rssi
                                beacon.observeCount = beacon.observeCount + 1
                                beacon.percent = (beacon.observeCount * 100) / timeToObserve.toDouble()
                            }
                        }

                        if (!observerHistory.containsKey(beacon.macAddress)) {
                            observerHistory[beacon.macAddress] = ArrayList()
                        }

                        observerHistory[beacon.macAddress]?.add(beacon)
                        Log.d(LOG_TAG, "BLENetworkObserverService: BEACON = $beacon")
                    }

                }

                sleepThread (sleepTime) {
                    bundle.putParcelableArrayList(BLE_SCANNED, ArrayList(bleData))
                    bundle.putSerializable(OBSERVED_HISTORY, observerHistory)

                    networkResultReceiver?.send(NetworkResultStatus.FAIL.value, bundle)
                    XLog.d("${getActualDateString()} | SERVICE OBSERVER ENDS | STATUS FAIL")
                }

                observedTime++
            }

            if (bleData.isEmpty()) {
                networkResultReceiver?.send(NetworkResultStatus.UNDEFINED.value, null)
                XLog.d("${getActualDateString()} | SERVICE OBSERVER ENDS | STATUS UNDEFINED")

                // If there's no Beacon on ScanResults, stopping service.
                stopSelf()
            }
        }

        XLog.d("${getActualDateString()} | SERVICE OBSERVER ENDS | STATUS SUCCESS")
        Log.d(LOG_TAG, "SERVICE OBSERVER ENDS")

        // Sending the result for the result receiver telling that network observing end

        bundle.putParcelableArrayList(BLE_SCANNED, ArrayList(bleData))
        bundle.putSerializable(OBSERVED_HISTORY, observerHistory)

        networkResultReceiver?.send(NetworkResultStatus.SUCCESS.value, bundle)

        // Send the intent for the broadcasts receivers

        val intent = Intent(ACTION_OBSERVING_ENDS)
        intent.putExtra(BUNDLE_FINISH_OBSERVING, bundle)
        sendBroadcast(intent)

        stopSelf()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        this.holdBLeLock()

        networkManager = BLENetworkManager
        btManager = applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

        Log.d(LOG_TAG, "CREATING THE SERVICE")
        XLog.d("${getActualDateString()} | CREATING THE SERVICE")

        initBundle(intent)

        return Service.START_REDELIVER_INTENT
    }

    private fun initBundle(intent: Intent) {
        try {
            observerToken = intent.getStringExtra(TOKEN_OBSERVER)

            sleepTime = intent.getLongExtra(SLEEP_TIME, 60_000)

            beaconBundle = toParcelable(intent.getByteArrayExtra(BLE_BUNDLE), BeaconBundle.CREATOR)

            networkResultReceiver = intent.getParcelableExtra(RESULT_RECEIVER)

            if (beaconBundle != null) {

                Thread(this).start()

                val message = "SERVICE STARTED | OBSERVER TOKEN=$observerToken | SLEEP TIME=$sleepTime | DURATION=${beaconBundle?.observeTime}"
                Log.d(LOG_TAG, message)
                XLog.d("${getActualDateString()} | $message")
            } else {
                Log.d(LOG_TAG, "SERVICE START ERROR: WiFi Bundle is NULL")
                XLog.d("${getActualDateString()} | SERVICE START ERROR: WiFi Bundle is NULL")

                networkResultReceiver?.send(NetworkResultStatus.FAIL.value, null)
            }
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Error to init bundle | ${e.message}")
        }

    }

    override fun onDestroy() {
        this.releaseBLeLock()
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? = null

    private fun scanCallback() : ScanCallback = object : ScanCallback() {
        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            Log.d(LOG_TAG, "BLENetworkObserverService: HAS BATCH ${results?.size}")
            super.onBatchScanResults(results)
        }

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            Log.d(LOG_TAG, "BLENetworkObserverService: HAS RESULT ")

            result?.run {

                val name: String = this.device.name.orElse("UNKNOWN")
                val macAddress : String = this.device.address
                val rss = this.rssi
                val distance = rssiToDistance(rss)

                val beacon = Beacon(
                        name = name,
                        macAddress = macAddress,
                        rssi = rss,
                        distance = distance
                )

                scanResults.add(beacon)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(LOG_TAG, getMessageByErrorCodeInScanResult(errorCode))

            Log.d(LOG_TAG, "DISABLING BLUETOOTH AND WAIT 12 SECONDS TO ENABLED AGAIN")

            BluetoothAdapter.getDefaultAdapter().run {
                if (this.disable()) {
                    sleepThread(12)
                    this.enable()
                }
            }
        }
    }

    // WAKE LOCK

    private fun holdBLeLock() {
        Log.d(LOG_TAG, "NetworkObserverService: HOLD BLE LOCK")

        wakeLock?.run {
            (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG).apply {
                    setReferenceCounted(false)
                    if (!isHeld) acquire(1000)
                }
            }
        }
    }

    private fun releaseBLeLock() {
        Log.d(LOG_TAG, "NetworkObserverService: RELEASE BLE LOCK")
        wakeLock?.run { if (isHeld) release() }
    }
}

fun<String> String?.orElse(alt : String) : String = if (this == null || this == "") alt else this