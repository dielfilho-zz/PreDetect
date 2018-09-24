package br.ufc.quixada.predetect.common.domain

enum class NetworkResultStatus(val value : Int) {
    SUCCESS(1), FAIL(2), UNDEFINED(3);

    companion object {
        fun fromParcelable(resultCode: Int): NetworkResultStatus = when (resultCode) {
            1 -> SUCCESS
            2 -> FAIL
            else -> UNDEFINED
        }
    }
}