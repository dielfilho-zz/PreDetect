//package com.example.app
//
//import android.content.Context
//import android.os.AsyncTask
//import android.os.Bundle
//import android.support.v7.app.AppCompatActivity
//import android.util.Log
//import br.ufc.quixada.predetect.common.managers.NetworkResult
//import danielfilho.ufc.br.com.predetect.datas.WiFiData
//import danielfilho.ufc.br.com.predetect.intefaces.WiFiListener
//import danielfilho.ufc.br.com.predetect.intefaces.WiFiObserver
//import danielfilho.ufc.br.com.predetect.managers.WifiNetworkManager
//
//class WifiActivity : AppCompatActivity(), WiFiListener, WiFiObserver {
//
//    private lateinit var manager: WifiNetworkManager
//    private lateinit var tokenFromObserverNetwork : String
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        manager = WifiNetworkManager.getInstance()
//
//        // REGISTER TO RECEIVE DATA FROM WIFI NETWORK
//        manager.registerListener(this)
//
//        // WIFI OBSERVER - MAC ADDRESS
//        AsyncTask.execute {
//            tokenFromObserverNetwork = manager.observeNetwork(
//                    this,
//                    listOf("cc:50:0a:55:48:50", "gg:cc:aa:bb:dd:ee"),
//                    2,
//                    10.0,
//                    1
//            )
//        }
//    }
//
//    // WIFI OBSERVER - RESULT
//    override fun onObservingEnds(networkResult: NetworkResult<WiFiData>) {
//        networkResult
//                .onSuccess {
//                    Log.i(LOG_WIFI_OBSERVER, "SUCCESS CONTEXT")
//
//                    // WIFI RESULT LIST
//                    it?.apply {
//                        Log.i(LOG_WIFI_OBSERVER, it.size.toString())
//                        Log.i(LOG_WIFI_OBSERVER, it.toString())
//                    }
//
//                    // SHOW STATUS FOR EACH MAC IN EACH ITERATION
//                    Log.i(LOG_WIFI_OBSERVER, networkResult.observedHistory.toString())
//
//                    // TOKEN FROM OBSERVER
//                    Log.i(LOG_WIFI_OBSERVER, networkResult.token)
//                }
//                .onFail {
//                    Log.i(LOG_WIFI_OBSERVER, "FAIL CONTEXT")
//                }
//                .onUndefinedNetwork {
//                    Log.i(LOG_WIFI_OBSERVER, "UNDEFINED CONTEXT")
//                }
//
//    }
//
//    // WIFI LISTENER - RESULTS FOR EACH RSS UPDATE
//    override fun onChange(list: List<WiFiData>) {
//        Log.i(LOG_WIFI_LISTENER, "INITIALIZING WIFI DATA LISTENER")
//        Log.i(LOG_WIFI_LISTENER, "WIFI DATA ==> ${list.size}")
//    }
//
//    override fun getListenerContext(): Context = this
//
//    override fun onPause() {
//        manager.unregisterListener(this)
//        super.onPause()
//    }
//
//    override fun onDestroy() {
//        manager.unregisterListener(this)
//        super.onDestroy()
//    }
//
//    override fun onResume() {
//        manager.registerListener(this)
//        super.onResume()
//    }
//
//    companion object {
//        const val LOG_WIFI_OBSERVER = "PRE_DETECT__APP__WIFI_OBSERVER"
//        const val LOG_WIFI_LISTENER = "PRE_DETECT__APP__WIFI_LISTENER"
//    }
//}
