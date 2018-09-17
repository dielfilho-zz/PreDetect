/*
 * Copyright 2018 neXenio
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 * http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package br.ufc.predetect.ble.filters

import br.ufc.predetect.ble.domain.AdvertisingPacket
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
 *
 */

class KalmanFilter (val duration : Long, private val processNoise: Double = 0.008) : RSSIFilter {

    override fun filter(advertisingPackets : List<AdvertisingPacket>): Double {
        val advertisingPacketRSSi = advertisingPackets.map { advertisingPacket -> advertisingPacket.RSSI }
        val averageRSSi = advertisingPacketRSSi.average()
        val measurementNoise : Double = calculateVariance(advertisingPacketRSSi, averageRSSi)
        return calculateKalmanRSSi(advertisingPackets, processNoise, measurementNoise, averageRSSi)
    }

    private fun calculateKalmanRSSi(advertisingPackets: List<AdvertisingPacket>,
                                    processNoise: Double,
                                    measurementNoise: Double,
                                    averageRSSi: Double): Double
    {
        var errorCovariance : Double
        var lastErrorCovariance = 1.0
        var estimated : Double = averageRSSi

        advertisingPackets.forEach {
            val gain = lastErrorCovariance.div(lastErrorCovariance.plus(measurementNoise))
            estimated += gain.times(it.RSSI.minus(estimated))
            errorCovariance = 1.minus(gain).times(lastErrorCovariance)
            lastErrorCovariance = errorCovariance.plus(processNoise)
        }

        return estimated
    }
}