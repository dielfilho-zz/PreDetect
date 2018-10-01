package br.ufc.quixada.predetect.common.interfaces


/**
 *
 * @author Daniel Filho
 * @since 5/27/16
 *
 * @updated Gabriel Cesar, 2018
 *
 */
interface NetworkListener <T> : NetworkContext {
    fun onChange(list: List<T>)
}