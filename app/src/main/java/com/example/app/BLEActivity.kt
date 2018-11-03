package com.example.app

import android.Manifest
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import br.ufc.predetect.ble.data.Beacon
import br.ufc.predetect.ble.interfaces.BeaconListener
import br.ufc.predetect.ble.interfaces.BeaconObserver
import br.ufc.predetect.ble.managers.BLENetworkManager
import br.ufc.quixada.predetect.common.managers.NetworkResult

class BLEActivity : AppCompatActivity()
        , BeaconObserver
        , BeaconListener
{

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // REGISTER TO RECEIVE DATA FROM BLE NETWORK
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, listOf(Manifest.permission.ACCESS_FINE_LOCATION).toTypedArray(), 1)
        }

        BLENetworkManager.registerListener(this)

        BLENetworkManager.observeNetwork(
                this,
                listOf("F0:C7:7F:EB:89:5E", "gg:cc:aa:bb:dd:ee"),
                5,
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
        Log.i(LOG_BLE_LISTENER, "INITIALIZING DATA LISTENER CHANGE")
        Log.i(LOG_BLE_LISTENER, "BLE SIZE ==> ${list.size}")
        Log.i(LOG_BLE_LISTENER, "BLE DATA ==> $list")
    }

    override fun getListenerContext(): Context = this

    override fun onPause() {
        BLENetworkManager.unregisterListener(this)
        super.onPause()
    }

    override fun onDestroy() {
        BLENetworkManager.unregisterListener(this)
        super.onDestroy()
    }

    override fun onResume() {
        // REGISTER TO RECEIVE DATA FROM BLE NETWORK
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, listOf(Manifest.permission.ACCESS_FINE_LOCATION).toTypedArray(), 1)
        }

        BLENetworkManager.registerListener(this)
        super.onResume()
    }

    companion object {
        const val LOG_BLE_OBSERVER = "PRE_DETECT__APP__BLE_OBSERVER"
        const val LOG_BLE_LISTENER = "PRE_DETECT__APP__BLE_LISTENER"
    }
}