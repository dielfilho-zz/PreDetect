package br.ufc.quixada.predetect.common.managers

import br.ufc.quixada.predetect.common.domain.NetworkResultStatus

open class NetworkResult<T> (
        private val resultCode: NetworkResultStatus,
        private val dataListener : List<T>?
) {

    fun onSuccess(action : (List<T>?) -> Unit) : NetworkResult<T> {
        if (resultCode == NetworkResultStatus.SUCCESS) {
            action(dataListener)
        }
        return this
    }

    fun onFail(action : (List<T>?) -> Unit) : NetworkResult<T> {
        if (resultCode == NetworkResultStatus.FAIL) {
            action(dataListener)
        }
        return this
    }

    fun onUndefinedNetwork (action : (List<T>?) -> Unit) : NetworkResult<T> {
        if (resultCode == NetworkResultStatus.UNDEFINED) {
            action(dataListener)
        }
        return this
    }
}