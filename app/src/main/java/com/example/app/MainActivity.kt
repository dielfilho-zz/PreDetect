package com.example.app

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import danielfilho.ufc.br.com.predetect.datas.WiFiData
import danielfilho.ufc.br.com.predetect.intefaces.WiFiListener
import danielfilho.ufc.br.com.predetect.managers.NetworkManager

class MainActivity : AppCompatActivity(), WiFiListener {

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

    }

    override fun onChange(list: List<WiFiData>) {
        Log.i("APP", "INITIALIZING WIFI DATA LISTENER")
        Log.i("APP", "WIFI DATA ==> ${list.size}")
        Log.i("APP", "WIFI DATA ==> $list")
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
