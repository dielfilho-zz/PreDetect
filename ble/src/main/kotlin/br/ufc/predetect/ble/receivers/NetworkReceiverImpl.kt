package br.ufc.predetect.ble.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import br.ufc.predetect.ble.managers.BLENetworkManager
import br.ufc.quixada.predetect.common.interfaces.NetworkReceiver

/**
 * @author Gabriel Cesar
 * @since 2018
 *
 */
class NetworkReceiverImpl(private val networkManager: NetworkReceiver = BLENetworkManager) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) = networkManager.onNetworkReceive(context, intent)

}
