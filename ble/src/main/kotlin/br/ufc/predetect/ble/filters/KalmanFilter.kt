package br.ufc.predetect.ble.filters

import br.ufc.predetect.ble.utils.calculateVariance

/**
 *
 * Kalman filtering, also known as linear quadratic estimation (LQE), is an algorithm that uses a
 * series of measurements observed over time, containing statistical noise and other inaccuracies,
 * and produces estimates of unknown variables that tend to be more accurate than those based on a
 * single measurement alone, by using Bayesian inference and estimating a joint probability
 * distribution over the variables for each time frame.
 *
 *
 * Since RSSi signals are largely influenced by signal noise, taking samples from the signal seems
 * likely to be beneficial. Evaluate Unscented Kalman filter
 *
 * @see <https://en.wikipedia.org/wiki/Kalman_filter> Kalman Filter
 * @see <https://www.wouterbulten.nl/blog/tech/kalman-filters-explained-removing-noise-from-rssi-signals/> Kalman Explained
 * @see <https://github.com/fgroch/beacon-rssi-resolver/blob/master/src/main/java/tools/blocks/filter/KalmanFilter.java> Example Implementation
 *
 */

class KalmanFilter (private val processNoise: Double = 0.008) : RSSIFilter {

    override fun filter(advertisingPackets : List<Int>): Int {
        val averageRSSi = advertisingPackets.average()
        val measurementNoise : Double = calculateVariance(advertisingPackets, averageRSSi)
        return calculateKalman(advertisingPackets, processNoise, measurementNoise, averageRSSi).toInt()
    }

    private fun calculateKalman(advertisingPackets: List<Int>,
                                    processNoise: Double,
                                    measurementNoise: Double,
                                    averageRSSi: Double): Double
    {
        var errorCovariance : Double
        var lastErrorCovariance = 1.0
        var estimated : Double = averageRSSi

        advertisingPackets.forEach {
            val gain = lastErrorCovariance.div(lastErrorCovariance.plus(measurementNoise))
            estimated += gain.times(it.minus(estimated))
            errorCovariance = 1.minus(gain).times(lastErrorCovariance)
            lastErrorCovariance = errorCovariance.plus(processNoise)
        }

        return estimated
    }
}