package br.ufc.quixada.predetect.common.receivers

import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.os.ResultReceiver
import android.util.Log
import br.ufc.quixada.predetect.common.domain.NetworkResultStatus
import br.ufc.quixada.predetect.common.interfaces.NetworkObserver
import br.ufc.quixada.predetect.common.managers.NetworkResult
import br.ufc.quixada.predetect.common.utils.LOG_PRE_DETECT
import br.ufc.quixada.predetect.common.utils.OBSERVED_HISTORY
import br.ufc.quixada.predetect.common.utils.TOKEN_OBSERVER

abstract class NetworkObserverResultReceiver<D : Parcelable>(
        private val keyScanner : String,
        private val observer: NetworkObserver<D>? = null,
        handler: Handler? = null) : ResultReceiver(handler) {

    private fun onResult(resultStatus: NetworkResultStatus, resultData : List<D>?, history : HashMap<String, List<D>>?, token : String?) {
        Log.i(LOG_PRE_DETECT, "NetworkObserverResultReceiver: SEND MESSAGE TO $keyScanner OBSERVER")
        observer?.onObservingEnds(NetworkResult(resultStatus, resultData, history ?: emptyMap(), token))
    }

    private fun onFail() {
        Log.e(LOG_PRE_DETECT, "NetworkObserverResultReceiver: COULD NOT SEND MESSAGE TO $keyScanner OBSERVER, BECAUSE IT'S NULL")
    }

    override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
        if (observer == null) {
            return this.onFail()
        }

        val values : List<D>? = resultData?.getParcelableArrayList(keyScanner)

        @Suppress("unchecked_cast")
        val history : HashMap<String, List<D>>? = resultData?.getSerializable(OBSERVED_HISTORY) as HashMap<String, List<D>>?

        val token = resultData?.getString(TOKEN_OBSERVER)

        this.onResult(NetworkResultStatus.fromParcelable(resultCode), values, history, token)
    }
}