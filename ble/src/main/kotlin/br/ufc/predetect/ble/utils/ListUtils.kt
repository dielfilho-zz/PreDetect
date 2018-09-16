package br.ufc.predetect.ble.utils

fun calculateVariance (values : List<Int>, average: Double) : Double = values.let {
    values
            .map { v -> Math.pow(v - average, 2.0) }
            .reduce { acc, v -> acc + v }
            .div(it.realSize)
}

val <T> List<T>.realSize: Int
    get() = (size - 1).coerceAtLeast(1)