package br.ufc.quixada.predetect.common.managers

import br.ufc.quixada.predetect.common.domain.NetworkResultStatus

open class NetworkResult<T> (
        val resultCode: NetworkResultStatus,
        val dataListener : List<T>
) {

    fun onSuccess(action : () -> Unit) : NetworkResult<T> {
        if (resultCode == NetworkResultStatus.SUCCESS) {
            action()
        }
        return this
    }

    fun onFail(action : () -> Unit) : NetworkResult<T> {
        if (resultCode == NetworkResultStatus.FAIL) {
            action()
        }
        return this
    }

    fun onUndefinedNetwork (action : () -> Unit) : NetworkResult<T> {
        if (resultCode == NetworkResultStatus.UNDEFINED) {
            action()
        }
        return this
    }
}