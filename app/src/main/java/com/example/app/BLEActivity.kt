package com.example.app

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import br.ufc.predetect.ble.domain.Beacon
import br.ufc.predetect.ble.interfaces.BeaconListener
import br.ufc.predetect.ble.interfaces.BeaconObserver
import br.ufc.predetect.ble.managers.BLENetworkManager
import br.ufc.quixada.predetect.common.managers.NetworkResult

class BLEActivity : AppCompatActivity(), BeaconObserver, BeaconListener {

    private lateinit var manager: BLENetworkManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        manager = BLENetworkManager

        // REGISTER TO RECEIVE DATA FROM BLE NETWORK
        manager.registerListener(this)

        manager.observeNetwork(
                this,
                listOf("F0:C7:7F:EB:89:5E", "gg:cc:aa:bb:dd:ee"),
                2,
                10.0
        )
    }

    override fun onObservingEnds(networkResult: NetworkResult<Beacon>) {
        networkResult
                .onSuccess {
                    Log.i(LOG_BLE_OBSERVER, "SUCCESS CONTEXT")

                    // BT RESULT LIST
                    it?.apply {
                        Log.i(LOG_BLE_OBSERVER, it.size.toString())
                        Log.i(LOG_BLE_OBSERVER, it.toString())
                    }

                    // Show status for each MAC in each iteration
                    Log.i(LOG_BLE_OBSERVER, networkResult.observedHistory.toString())
                }
                .onFail {
                    Log.i(LOG_BLE_OBSERVER, "FAIL CONTEXT")
                }
                .onUndefinedNetwork {
                    Log.i(LOG_BLE_OBSERVER, "UNDEFINED CONTEXT")
                }
    }

    // BLE LISTENER - RESULTS FOR EACH RSS UPDATE
    override fun onChange(list: List<Beacon>) {
        Log.i(LOG_BLE_LISTENER, "INITIALIZING BLE DATA LISTENER")
        Log.i(LOG_BLE_LISTENER, "BLE SIZE ==> ${list.size}")
        Log.i(LOG_BLE_LISTENER, "BLE DATA ==> $list")
    }

    override fun getListenerContext(): Context = this

    override fun onPause() {
        manager.unregisterListener(this)
        super.onPause()
    }

    override fun onDestroy() {
        manager.unregisterListener(this)
        super.onDestroy()
    }

    override fun onResume() {
        manager.registerListener(this)
        super.onResume()
    }

    companion object {
        const val LOG_BLE_OBSERVER = "BLE_OBSERVER"
        const val LOG_BLE_LISTENER = "BLE_LISTENER"
    }
}