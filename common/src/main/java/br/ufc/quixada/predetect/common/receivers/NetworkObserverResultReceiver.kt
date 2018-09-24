package br.ufc.quixada.predetect.common.receivers

import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.os.ResultReceiver
import android.util.Log
import br.ufc.quixada.predetect.common.domain.NetworkResultStatus
import br.ufc.quixada.predetect.common.interfaces.NetworkObserver
import br.ufc.quixada.predetect.common.managers.NetworkResult

abstract class NetworkObserverResultReceiver<D : Parcelable>(
        private val keyScanner : String,
        private val observer: NetworkObserver<D>? = null,
        handler: Handler? = null) : ResultReceiver(handler) {

    private fun onResult(resultStatus: NetworkResultStatus, resultData : List<D>?) {
        observer?.onObservingEnds(NetworkResult(resultStatus, resultData))
    }

    private fun onFail() {
        Log.e("PRE_DETECT", "---------- COULD NOT SEND MESSAGE TO $keyScanner OBSERVER, BECAUSE IT'S NULL ----------")
    }

    override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
        if (observer == null) {
            return this.onFail()
        }

        val values : List<D>? = resultData?.getParcelableArrayList(keyScanner)
        this.onResult(NetworkResultStatus.fromParcelable(resultCode), values)
    }
}