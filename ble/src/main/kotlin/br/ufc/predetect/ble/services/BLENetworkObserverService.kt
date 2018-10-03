package br.ufc.predetect.ble.services

import android.app.Service
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.os.Parcelable
import android.os.ResultReceiver
import android.util.Log
import br.ufc.predetect.ble.constants.*
import br.ufc.predetect.ble.domain.Beacon
import br.ufc.predetect.ble.domain.BeaconBundle
import br.ufc.predetect.ble.filters.KalmanFilter
import br.ufc.predetect.ble.managers.BLENetworkManager
import br.ufc.predetect.ble.utils.getAdvertisingRange
import br.ufc.predetect.ble.utils.rssiToDistance
import br.ufc.predetect.ble.utils.scanSettings
import br.ufc.quixada.predetect.common.domain.NetworkResultStatus
import br.ufc.quixada.predetect.common.utils.toParcelable
import com.elvishew.xlog.LogLevel
import com.elvishew.xlog.XLog
import com.elvishew.xlog.printer.file.FilePrinter
import java.lang.System.currentTimeMillis
import java.util.*

/**
 *
 * @author Gabriel Cesar
 * @since 2018
 *
 */
class BLENetworkObserverService : Service(), Runnable {

    private var btManager: BluetoothManager? = null
    private var beaconBundle  : BeaconBundle? = null
    private var networkManager: BLENetworkManager? = null

    private var networkResultReceiver: ResultReceiver? = null

    override fun onCreate() {
        super.onCreate()
        XLog.d("-------- SERVICE ON CREATE -------")
        Log.d(LOG_TAG, "-------- SERVICE ON CREATE -------")
    }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        try {
            XLog.init(LogLevel.ALL, FilePrinter.Builder(LOG_PATH + "pre_detect").build())
        } catch (e: Exception) {
            XLog.e(e.message)
        }

        networkManager = BLENetworkManager
        btManager = applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

        try {
            XLog.init(LogLevel.ALL, FilePrinter.Builder(LOG_PATH).build())
        } catch (e: Exception) {
            XLog.e(e.message)
        }

        Log.d(LOG_TAG, "--------- CREATING THE SERVICE ---------")
        XLog.d("--------- CREATING THE SERVICE ---------")

        initBundle(intent)

        return Service.START_REDELIVER_INTENT
    }

    private fun initBundle(intent: Intent) {
        try {
            beaconBundle = toParcelable(intent.getByteArrayExtra(BLE_BUNDLE), BeaconBundle.CREATOR)

            networkResultReceiver = intent.getParcelableExtra(RESULT_RECEIVER)

            if (beaconBundle != null) {
                Thread(this)
                        .start()

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

        observedTime = 0

        if (beaconBundle == null) {
            Log.e(LOG_TAG, "Bundle is null")
        }

        timeInSeconds = 0

        beaconBundle?.observeTime?.div(60000)?.run {
            timeInSeconds = this
        }

        bleData = emptyList<Beacon>().toMutableList()

        beaconBundle?.beaconData?.forEach {
            bleData.add(Beacon(macAddress = it))
        }

        while (observedTime < timeInSeconds) {

            btManager?.adapter?.bluetoothLeScanner?.run {
                this.startScan(emptyList(), scanSettings(), scanCallback(beaconBundle?.distanceRange))
                this.stopScan(scanCallback(beaconBundle?.distanceRange))
                //TODO
            }

        }

        XLog.d("${currentTimeMillis()} |  --------- SERVICE ENDS ---------")
        Log.d(LOG_TAG, "--------- SERVICE ENDS ---------")

        // Sending the result for the result receiver telling that network observing end

        val bundle = Bundle()
        bundle.putParcelableArrayList(BLE_SCANNED, bleData as ArrayList<out Parcelable>)
        networkResultReceiver?.send(NetworkResultStatus.SUCCESS.value, bundle)

        // Send the intent for the broadcasts receivers

        val intent = Intent(ACTION_OBSERVING_ENDS)
        intent.putExtra(BUNDLE_FINISH_OBSERVING, bundle)
        sendBroadcast(intent)

        stopSelf()
    }

    private fun scanCallback(distanceRange : Double?) : ScanCallback = object : ScanCallback() {
        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            if (results != null && results.size > 0) {
                distanceRange?.run {
                    getBeaconsFromScanResult(results).forEach { beaconResult ->
                        bleData.forEach { beacon ->
                            if (beacon.macAddress == beaconResult.macAddress &&
                                    distanceRange >= beaconResult.distance) {

                                beacon.percent = beacon.observeCount.plus(100).div(timeInSeconds).toDouble()

                                beacon.observeCount += 1

                                Log.d(LOG_TAG, "PERCENT: ${beacon.observeCount}")
                            }
                        }
                    }
                }

                sleep()

                observedTime++

            } else {

                networkResultReceiver?.send(NetworkResultStatus.UNDEFINED.value, null)

                // If there's no Beacon on ScanResults, stopping service.
                stopSelf()
            }

        }

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            Log.i(LOG_TAG, "HAS RESULTS ${result?.device?.name}")
        }
    }

    fun sleep() {
        try {
            Thread.sleep(60000)
        } catch (e: InterruptedException) {
            Log.e(LOG_TAG, BLE_NETWORK_SLEEP_ERROR)
            networkResultReceiver?.send(NetworkResultStatus.FAIL.value, null)
        }
    }

    fun getBeaconsFromScanResult(scanResults : MutableList<ScanResult>) : List<Beacon> = scanResults
            .asSequence()
            .groupBy { it.device.address }
            .map { entry: Map.Entry<String, List<ScanResult>> ->

                val name: String = entry.value.first().device.name

                val rss = KalmanFilter().filter(entry.value.map { it.rssi }).toInt()
                val txPower = entry.value.asSequence().map { it.txPower }.average().toInt()

                val maxRange = getAdvertisingRange(txPower)
                val distance = rssiToDistance(rss)

                Log.i(LOG_TAG, "MAX RANGE $maxRange")
                Log.i(LOG_TAG, "DISTANCE  $distance")

                Beacon(
                        name = name,
                        macAddress = entry.key,
                        transmissionPower = txPower,
                        rssi = rss,
                        distance = distance
                )

            }
            .toList()

    companion object {
        var timeInSeconds = 0
        var observedTime = 0
        var bleData = emptyList<Beacon>().toMutableList()
    }
}

