package br.ufc.quixada.predetect.common.interfaces

import br.ufc.quixada.predetect.common.managers.NetworkResult

/**
 *
 * @author Daniel Filho
 * @since 5/27/16
 *
 * @updated Gabriel Cesar, 2018
 *
 */
interface NetworkObserver<T>: NetworkListener {
    fun onObservingEnds(networkResult: NetworkResult<T>)
}
