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
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * @author Gabriel Cesar
 * @since 2018
 */
class BLENetworkObserverService : Service(), Runnable {
    private val startTime = System.currentTimeMillis()

    private var observerToken : String? = null
    private var sleepTime: Long = 1800_000 // Default 30 minutes
    private var btManager: BluetoothManager? = null
    private var beaconBundle  : BeaconBundle? = null
    private var networkManager: BLENetworkManager? = null

    private var networkResultReceiver: ResultReceiver? = null

    private var wakeLock: PowerManager.WakeLock? = null

    init {
        startXLogger()
    }

    companion object {
        var bleData = ConcurrentLinkedQueue<Beacon>()
        var scanResults = ConcurrentLinkedQueue<Beacon>()

        private const val TWELVE_SECONDS = 12_000L
        private const val TWO_SECONDS = 2_000L
    }

    override fun run() {
        var timeToObserve = 0L
        var observedTime = 0L

        val bundle = Bundle()
        bundle.putString(TOKEN_OBSERVER, observerToken)

        if (beaconBundle == null) { Log.e(LOG_TAG, "BLENetworkObserverService: BUNDLE IS NULL") }

        beaconBundle?.observeTime?.div(sleepTime)?.run { timeToObserve = this }

        bleData.addAll(beaconBundle?.beaconData?.map { Beacon(macAddress = it) }?.toHashSet() ?: hashSetOf())

        val observerHistory = HashMap<String, MutableList<Beacon>>()

        if (bleData.isEmpty()) {
            networkResultReceiver?.send(NetworkResultStatus.UNDEFINED.value, null)
            XLog.d("BLENetworkObserverService: ${getActualDateString()} | SERVICE OBSERVER ENDS | STATUS UNDEFINED")

            // If there's no Beacon on ScanResults, stopping service.
            stopSelf()
        }

        while (observedTime < timeToObserve) {

            adapterUpdateAfterEveryOneHour()

            try {
                bleData.forEach { it.iterationExecuted = observedTime.toString().toInt().plus(1) }
            } catch (e : Exception) {
                Log.e(LOG_TAG, "BLENetworkObserverService: Error to increment iteration for each data in BleData. ${e.message}")
            }

            btManager?.adapter?.run {
                if (state == BluetoothAdapter.STATE_TURNING_ON) {
                    Log.d(LOG_TAG, "BLENetworkObserverService: WAIT 12 SECONDS TO ENABLE BLUETOOTH")
                    sleepThread(TWELVE_SECONDS)
                }

                if (state == BluetoothAdapter.STATE_ON) {
                    Log.d(LOG_TAG, "BLENetworkObserverService: BLUETOOTH ENABLED")

                    bluetoothLeScanner?.run {

                        val scanCallback = scanCallback()

                        Log.d(LOG_TAG, "BLENetworkObserverService: START SCAN")
                        this.startScan(emptyList(), scanSettings(), scanCallback)

                        sleepThread(TWELVE_SECONDS)

                        Log.d(LOG_TAG, "BLENetworkObserverService: STOP SCAN")
                        this.stopScan(scanCallback)

                        sleepThread(TWO_SECONDS)

                        var scansResult = emptyList<Beacon>()

                        try {
                            scansResult = reduceScanResults(scanResults)
                            Log.d(LOG_TAG, "BLENetworkObserverService: SCANS RESULTS ${scansResult.size}")

                            scanResults.clear()
                        } catch (e : Exception) {
                            Log.e(LOG_TAG, "BLENetworkObserverService: Error to reduce data in ScanResults. ${e.message}")
                        }

                        try {

                            beaconBundle?.distanceRange?.let { distanceRange ->

                                bleData.forEach { beacon ->
                                    scansResult.forEach { scan ->
                                        if (scan.macAddress == beacon.macAddress && isValidBeacon(scan) && scan.distance <= distanceRange) {
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
                                    Log.d(LOG_TAG, "BLENetworkObserverService: TOKEN $observerToken | ITERATION $observedTime | BEACON = $beacon")
                                }
                            }
                        } catch (e : Exception) {
                            Log.e(LOG_TAG, "BLENetworkObserverService: Error to map ScanResults. Ignore results. ${e.message}")
                        }
                    }
                }
            }

            Log.d(LOG_TAG, "BLENetworkObserverService: TOKEN $observerToken | ITERATION $observedTime")

            sleepThread(sleepTime) {
                bundle.putParcelableArrayList(BLE_SCANNED, ArrayList(bleData))
                bundle.putSerializable(OBSERVED_HISTORY, observerHistory)

                networkResultReceiver?.send(NetworkResultStatus.FAIL.value, bundle)
                XLog.d("BLENetworkObserverService: ${getActualDateString()} | SERVICE OBSERVER ENDS | STATUS FAIL")
            }

            observedTime++

        }

        XLog.d("BLENetworkObserverService: ${getActualDateString()} | SERVICE OBSERVER ENDS | STATUS SUCCESS")
        Log.d(LOG_TAG, "BLENetworkObserverService: SERVICE OBSERVER ENDS")

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

        Log.d(LOG_TAG, "OnStartCommand: CREATING THE SERVICE")
        XLog.d("OnStartCommand: ${getActualDateString()} | CREATING THE SERVICE")

        initBundle(intent)

        return Service.START_REDELIVER_INTENT
    }

    private fun initBundle(intent: Intent) {
        try {
            observerToken = intent.getStringExtra(TOKEN_OBSERVER)

            sleepTime = intent.getLongExtra(SLEEP_TIME,1800_000) // Default 30 minutes

            beaconBundle = toParcelable(intent.getByteArrayExtra(BLE_BUNDLE), BeaconBundle.CREATOR)

            networkResultReceiver = intent.getParcelableExtra(RESULT_RECEIVER)

            if (beaconBundle != null) {

                Thread(this).start()

                val message = "SERVICE STARTED | OBSERVER TOKEN=$observerToken | SLEEP TIME=$sleepTime | DURATION=${beaconBundle?.observeTime}"
                Log.d(LOG_TAG, "InitBundle: $message")
                XLog.d("InitBundle: ${getActualDateString()} | $message")
            } else {
                Log.d(LOG_TAG, "InitBundle: SERVICE START ERROR: WiFi Bundle is NULL")
                XLog.d("InitBundle: ${getActualDateString()} | SERVICE START ERROR: WiFi Bundle is NULL")

                networkResultReceiver?.send(NetworkResultStatus.FAIL.value, null)
            }
        } catch (e: Exception) {
            Log.e(LOG_TAG, "InitBundle: Error to init bundle | ${e.message}")
        }

    }

    override fun onDestroy() {
        this.releaseBLeLock()
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? = null

    private fun scanCallback() : ScanCallback = object : ScanCallback() {

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
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
            Log.e(LOG_TAG, "ScanCallback: " + getMessageByErrorCodeInScanResult(errorCode))

            Log.d(LOG_TAG, "ScanCallback: DISABLING BLUETOOTH AND WAIT 12 SECONDS TO ENABLED AGAIN")

            try {
                BluetoothAdapter.getDefaultAdapter().run {
                    if (disable()) {
                        sleepThread(TWELVE_SECONDS)
                        enable()
                    }
                }
            } catch (e : Exception) {
                Log.e(LOG_TAG, "ScanCallback: Error to restart Bluetooth.")
            }
        }
    }

    // UPDATE BLE
    private fun adapterUpdateAfterEveryOneHour() {
        try {
            if (((System.currentTimeMillis() - startTime) % 3_360_000) == 0L) {
                // make something to back to work after 55 minutes

                BluetoothAdapter.getDefaultAdapter().run {
                    if (disable()) {
                        sleepThread(TWELVE_SECONDS)
                        enable()
                        sleepThread(TWELVE_SECONDS)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(LOG_TAG, "BLENetworkObserverService: Error to restart Bluetooth.")
        }
    }

    // WAKE LOCK

    private fun holdBLeLock() {
        Log.d(LOG_TAG, "BLELock: HOLD BLE LOCK")

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
        Log.d(LOG_TAG, "BLELock: RELEASE BLE LOCK")
        wakeLock?.run { if (isHeld) release() }
    }
}

fun<String> String?.orElse(alt : String) : String = if (this == null || this == "") alt else this