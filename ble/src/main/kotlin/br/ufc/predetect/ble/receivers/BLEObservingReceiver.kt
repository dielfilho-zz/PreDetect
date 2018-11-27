package br.ufc.predetect.ble.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import br.ufc.predetect.ble.constants.ACTION_START_OBSERVING_SERVICE
import br.ufc.predetect.ble.constants.BLE_BUNDLE
import br.ufc.predetect.ble.constants.LOG_TAG
import br.ufc.predetect.ble.services.BLENetworkObserverService
import br.ufc.predetect.ble.utils.createBeaconBundle
import br.ufc.quixada.predetect.common.utils.TOKEN_OBSERVER
import java.util.*

class BLEObservingReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let { ctx ->
            intent?.action?.run {
                if (this == ACTION_START_OBSERVING_SERVICE) {
                    val serviceIntent = Intent(context, BLENetworkObserverService::class.java)

                    val sleepTimeOneMinute: Long = 60_000
                    val token = "token${UUID.randomUUID()}"

                    serviceIntent.putExtra(TOKEN_OBSERVER, token)

                    Log.d(LOG_TAG, "BLEObservingReceiver: $token")

                    val btMACsToObserve : List<String> = intent.getStringArrayListExtra("btMACsToObserve")
                    val timeInMinutes : Int = intent.getIntExtra("timeInMinutes", 1)
                    val maxRangeInMeters : Double = intent.getDoubleExtra("maxRangeInMeters", 10.0)

                    serviceIntent.putExtra(BLE_BUNDLE, createBeaconBundle(btMACsToObserve, timeInMinutes * sleepTimeOneMinute, maxRangeInMeters))

                    ctx.startService(serviceIntent)
                }
            }
        }
    }

}