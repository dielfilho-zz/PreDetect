package com.example.app

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import br.ufc.quixada.predetect.common.managers.NetworkResult
import danielfilho.ufc.br.com.predetect.datas.WiFiData
import danielfilho.ufc.br.com.predetect.intefaces.WiFiListener
import danielfilho.ufc.br.com.predetect.intefaces.WiFiObserver
import danielfilho.ufc.br.com.predetect.managers.NetworkManager

class MainActivity : AppCompatActivity(), WiFiListener, WiFiObserver {

    private lateinit var manager: NetworkManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.i("APP", "INITIALIZING MANAGER INSTANCE")
        manager = NetworkManager.getInstance()

        Log.i("APP", "REGISTER LISTENER")

        // Register to receive data from wifi network
        manager.registerListener(this)

        Log.i("APP", "REGISTERED LISTENER")

        Log.i("APP_END", "INITIALIZING OBSERVER")

        manager.observeNetwork(
                this,
                listOf(WiFiData("d0:04:92:08:56:48")),
                System.currentTimeMillis().toInt(),
                10.0
        )
    }

    override fun onObservingEnds(networkResult: NetworkResult<WiFiData>) {
        networkResult
                .onSuccess {
                    Log.i("APP_END", "SUCCESS CONTEXT")

                    it?.run {
                        Log.i("APP_END", it.size.toString())
                        Log.i("APP_END", it.toString())
                    }
                }
                .onFail {
                    Log.i("APP_END", "FAIL CONTEXT")
                }
                .onUndefinedNetwork {
                    Log.i("APP_END", "UNDEFINED CONTEXT")
                }
    }

    override fun onChange(list: List<WiFiData>) {
        Log.i("APP", "INITIALIZING WIFI DATA LISTENER")
        Log.i("APP", "WIFI DATA ==> ${list.size}")
        Log.i("APP_", list.filter { it.ssid == "brisa-266168" }.toString())
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
}
