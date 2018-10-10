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
import br.ufc.predetect.ble.domain.Beacon
import br.ufc.predetect.ble.domain.BeaconBundle
import br.ufc.predetect.ble.filters.KalmanFilter
import br.ufc.predetect.ble.managers.BLENetworkManager
import br.ufc.predetect.ble.utils.isValidBeacon
import br.ufc.predetect.ble.utils.mergeBLEData
import br.ufc.predetect.ble.utils.rssiToDistance
import br.ufc.predetect.ble.utils.scanSettings
import br.ufc.quixada.predetect.common.domain.NetworkResultStatus
import br.ufc.quixada.predetect.common.utils.SLEEP_TIME
import br.ufc.quixada.predetect.common.utils.toParcelable
import com.elvishew.xlog.LogLevel
import com.elvishew.xlog.XLog
import com.elvishew.xlog.printer.file.FilePrinter
import java.io.File
import java.lang.System.currentTimeMillis
import java.util.ArrayList
import java.util.HashMap
import kotlin.collections.HashSet

/**
 * @author Gabriel Cesar
 * @since 2018
 */
class BLENetworkObserverService : Service(), Runnable {

    private var sleepTime: Long = 60_000
    private var btManager: BluetoothManager? = null
    private var beaconBundle  : BeaconBundle? = null
    private var networkManager: BLENetworkManager? = null

    private var networkResultReceiver: ResultReceiver? = null

    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate() {
        super.onCreate()
        try {
            val file = File("$LOG_PATH/$LOG_TAG")
            XLog.init(LogLevel.ALL, FilePrinter.Builder(file.path).build())
        } catch (e: Exception) {
            XLog.e(e.message)
            Log.e(LOG_TAG, "BLENetworkObserverService: " + e.message)
        }
    }

    override fun onDestroy() {
        this.releaseBLeLock()
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        this.holdBLeLock()

        networkManager = BLENetworkManager
        btManager = applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

        Log.d(LOG_TAG, "--------- CREATING THE SERVICE ---------")
        XLog.d("--------- CREATING THE SERVICE ---------")

        initBundle(intent)

        return Service.START_REDELIVER_INTENT
    }

    private fun initBundle(intent: Intent) {
        try {
            sleepTime = intent.getLongExtra(SLEEP_TIME, 60_000)

            beaconBundle = toParcelable(intent.getByteArrayExtra(BLE_BUNDLE), BeaconBundle.CREATOR)

            networkResultReceiver = intent.getParcelableExtra(RESULT_RECEIVER)

            if (beaconBundle != null) {

                Thread(this).start()

                Log.d(LOG_TAG, "--------- SERVICE STARTED ---------")
                XLog.d("${currentTimeMillis()} |  --------- SERVICE STARTED ---------")
            } else {
                Log.d(LOG_TAG, "--------- SERVICE START ERROR: WiFi Bundle is NULL ---------")
                XLog.d("${currentTimeMillis()} |  --------- SERVICE START ERROR: WiFi Bundle is NULL ---------")

                networkResultReceiver?.send(NetworkResultStatus.FAIL.value, null)
            }
        } catch (e: Exception) {
            Log.e(LOG_TAG, e.message)
        }

    }

    override fun run() {

        XLog.d("####################")

        observedTime = 0L

        if (beaconBundle == null) {
            Log.e(LOG_TAG, "Bundle is null")
        }

        beaconBundle?.observeTime?.div(sleepTime)?.run {
            timeInSeconds = this
        }

        bleData = hashSetOf()

        beaconBundle?.beaconData?.forEach {
            bleData.add(Beacon(macAddress = it))
        }

        val observerHistory = HashMap<String, MutableList<Beacon>>()

        while (observedTime < timeInSeconds) {

            btManager?.adapter?.bluetoothLeScanner?.run {
                val scanCallback = scanCallback()

                this.startScan(emptyList(), scanSettings(), scanCallback)

                sleep(12_000, false)

                this.stopScan(scanCallback)
            }

            if (bleData.isNotEmpty()) {
                beaconBundle?.distanceRange?.let { distanceRange ->
                    val srs = reduceScanResults(scanResults)

                    bleData = mergeBLEData(srs, bleData)

                    bleData.forEach {
                        if (isValidBeacon(it) && it.distance <= distanceRange) {

                            val newAppear = it.observeCount + 1
                            it.observeCount = newAppear

                            Log.d(LOG_TAG, "BLENetworkObserverService: OBSERVE COUNT = $newAppear")

                            val newPercent = (it.observeCount * 100 / timeInSeconds).toDouble()
                            it.percent = newPercent

                            Log.d(LOG_TAG, "BLENetworkObserverService: PERCENT = $newPercent")

                            Log.d(LOG_TAG, "BLENetworkObserverService: BEACON = $it")
                        }

                        if (!observerHistory.containsKey(it.macAddress)) {
                            observerHistory[it.macAddress] = ArrayList()
                        }

                        observerHistory[it.macAddress]?.add(it)
                    }

                }

                scanResults.clear()

                sleep()

                observedTime++
            } else {
                networkResultReceiver?.send(NetworkResultStatus.UNDEFINED.value, null)

                // If there's no Beacon on ScanResults, stopping service.
                stopSelf()
            }
        }

        XLog.d("${currentTimeMillis()} |  --------- SERVICE ENDS ---------")
        Log.d(LOG_TAG, "--------- SERVICE ENDS ---------")

        // Sending the result for the result receiver telling that network observing end

        val bundle = Bundle()
        bundle.putParcelableArrayList(BLE_SCANNED, ArrayList(bleData))
        networkResultReceiver?.send(NetworkResultStatus.SUCCESS.value, bundle)

        // Send the intent for the broadcasts receivers

        val intent = Intent(ACTION_OBSERVING_ENDS)
        intent.putExtra(BUNDLE_FINISH_OBSERVING, bundle)
        sendBroadcast(intent)

        releaseBLeLock()

        stopSelf()
    }

    private fun scanCallback() : ScanCallback = object : ScanCallback() {
        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            Log.d(LOG_TAG, "BLENetworkObserverService: HAS BATCH ${results?.size}")
            super.onBatchScanResults(results)
        }

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            Log.d(LOG_TAG, "BLENetworkObserverService: HAS RESULT ")

            result?.run {

                val name: String = if (this.device.name.isNullOrBlank()) "UNKNOWN" else this.device.name
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

        // Error necessary disable and enable bt again
        override fun onScanFailed(errorCode: Int) {
            Log.e(LOG_TAG, "BLENetworkObserverService: ERROR IN SCAN $errorCode")
            if (errorCode == 2 && BluetoothAdapter.getDefaultAdapter().disable()) {
                Thread.sleep(12_000)
                BluetoothAdapter.getDefaultAdapter().enable()
            }
            super.onScanFailed(errorCode)
        }
    }

    private fun reduceScanResults(scanResults: List<Beacon>) : List<Beacon> =
            scanResults
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

    private fun sleep(millis : Long = 60_000, sendResultError : Boolean = true) {
        try {
            Thread.sleep(millis)
        } catch (e: InterruptedException) {
            Log.e(LOG_TAG, BLE_NETWORK_SLEEP_ERROR)
            if (sendResultError)
                networkResultReceiver?.send(NetworkResultStatus.FAIL.value, null)
        }
    }

    private fun holdBLeLock() {
        Log.d(LOG_TAG, "NetworkObserverService: HOLD BLE LOCK")

        wakeLock?.run {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager?

            if (powerManager != null) {
                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG)
            } else
                XLog.e("POWER MANAGER NULL")

            this.setReferenceCounted(false)

            if (!this.isHeld) this.acquire(1000)

        }

    }

    private fun releaseBLeLock() {
        Log.d(LOG_TAG, "NetworkObserverService: RELEASE BLE LOCK")

        wakeLock?.run {
            if (this.isHeld)
                this.release()
        }
    }

    // Search by other method of make this
    companion object {
        var timeInSeconds = 0L
        var observedTime = 0L
        var bleData = HashSet<Beacon>()
        var scanResults = mutableListOf<Beacon>()
    }
}

