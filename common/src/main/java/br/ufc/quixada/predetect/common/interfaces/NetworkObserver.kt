package br.ufc.quixada.predetect.common.interfaces

interface NetworkObserver<T>: NetworkListener {
    fun onObservingEnds(resultCode: Int, list: List<T>)
}
