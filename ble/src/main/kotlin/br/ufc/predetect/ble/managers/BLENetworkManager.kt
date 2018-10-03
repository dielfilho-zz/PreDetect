package br.ufc.predetect.ble.managers

import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.util.Log
import br.ufc.predetect.ble.domain.Beacon
import br.ufc.predetect.ble.filters.KalmanFilter
import br.ufc.predetect.ble.interfaces.BeaconListener
import br.ufc.predetect.ble.constants.LOG_TAG
import br.ufc.predetect.ble.utils.getAdvertisingRange
import br.ufc.predetect.ble.utils.rssiToDistance
import br.ufc.predetect.ble.utils.scanSettings
import br.ufc.quixada.predetect.common.interfaces.NetworkListener
import br.ufc.quixada.predetect.common.interfaces.NetworkReceiver

/**
 * @author Gabriel Cesar
 * @since 2018
 *
 */
object BLENetworkManager : NetworkReceiver {
    private lateinit var btManager: BluetoothManager
    private val listeners: MutableList<NetworkListener<Beacon>>? = emptyList<NetworkListener<Beacon>>().toMutableList()

    private fun notifyWiFiListeners(wifiData: List<Beacon>) = listeners?.forEach {
        if (it is BeaconListener) {
            it.onChange(wifiData)
        }
    }

    fun registerListener(listener: NetworkListener<Beacon>) = listener.apply {
        listeners?.add(this)
        if (this is BeaconListener) {
            onListenerRegistered(this.getListenerContext())
        }
    }

    fun unregisterListener(listener: NetworkListener<Beacon>) = listeners?.remove(listener)

    /**
     * #scanCallback already do it
     * */
    override fun onNetworkReceive(context: Context?, intent: Intent?) = Unit
    // Maybe will not necessary in BLE

    @Throws(NullPointerException::class)
    private fun onListenerRegistered(context: Context) {
        btManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        btManager.adapter?.bluetoothLeScanner?.startScan(emptyList(), scanSettings(), scanCallback())
    }

    private fun scanCallback() : ScanCallback = object : ScanCallback() {
        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            results?.run {
                notifyWiFiListeners(results.asSequence()
                        .groupBy { it.device.address }
                        .map { entry: Map.Entry<String, List<ScanResult>> ->

                            val name : String = entry.value.first().device.name

                            val rss= KalmanFilter().filter(entry.value.map { it.rssi }).toInt()
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

                        }.toList()
                )
            }
        }

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            Log.i(LOG_TAG, "HAS RESULTS ${result?.device?.name}")
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(LOG_TAG, "ERROR IN SCAN $errorCode")
            super.onScanFailed(errorCode)
        }
    }

}