package br.ufc.predetect.ble.managers

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import br.ufc.predetect.ble.domain.Beacon
import br.ufc.predetect.ble.interfaces.BeaconListener
import br.ufc.quixada.predetect.common.interfaces.NetworkListener
import br.ufc.quixada.predetect.common.interfaces.NetworkReceiver
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.scan.ScanFilter
import com.polidea.rxandroidble2.scan.ScanSettings
import io.reactivex.android.schedulers.AndroidSchedulers


object NetworkManager : NetworkReceiver {

    lateinit var rxBleClient: RxBleClient
    lateinit var btAdapter: BluetoothAdapter
    lateinit var btManager : BluetoothManager

    private val listeners: MutableList<NetworkListener<Beacon>>? = emptyList<NetworkListener<Beacon>>().toMutableList()

    fun notifyWiFiListeners(wifiData: List<Beacon>) {
        listeners?.forEach {
            if (it is BeaconListener) {
                it.onChange(wifiData)
            }
        }
    }

    fun registerListener(listener: NetworkListener<Beacon>) {

        listeners?.add(listener)

        if (listener is BeaconListener) {
            listener.onChange(onListenerRegistered(listener.getListenerContext()))
        }
    }

    fun unregisterListener(listener: NetworkListener<Beacon>) {
        listeners?.remove(listener)
    }

    @Throws(NullPointerException::class)
    private fun onListenerRegistered(context: Context): List<Beacon> {
        btManager = context.applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        rxBleClient = RxBleClient.create(context)
        btAdapter = btManager.adapter

        TODO("research about blocking next in this context")

//        return rxBleClient.scanBleDevices(scanSettings(), scanFilter())
//                .observeOn(AndroidSchedulers.mainThread())
//                .blockingNext().map {
//                    Beacon()
//                }

//                val data = WiFiData(result.BSSID, result.level, NetworkUtils.rssiToDistance(result.level), result.SSID)

    }

    override fun onNetworkReceive(context: Context?, intent: Intent?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun scanSettings() : ScanSettings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
            .build()

    private fun scanFilter() : ScanFilter = ScanFilter.empty()

}