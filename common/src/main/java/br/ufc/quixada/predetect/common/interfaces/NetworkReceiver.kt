package br.ufc.quixada.predetect.common.interfaces

import android.content.Context
import android.content.Intent

/**
 *
 * @author Daniel Filho
 * @since 5/27/16
 *
 * @updated Gabriel Cesar, 2018
 *
 */
interface NetworkReceiver {
    fun onNetworkReceive(context: Context?, intent: Intent?)
}
