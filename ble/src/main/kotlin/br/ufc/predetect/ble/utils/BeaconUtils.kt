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
 *
 */

package br.ufc.predetect.ble.utils

/**
 *
 * Estimates a maximum distance at which advertising packages sent
 * using the specified transmission power can be received.
 *
 * @param transmissionPower the tx power (in dBm) of the beacon
 * @return estimated range in meters
 * @see <https://support.kontakt.io/hc/en-gb/articles/201621521-Transmission-power-Range-and-RSSI>
 *     Kontakt.io Knowledge Base
 *
 */
fun getAdvertisingRange(transmissionPower : Int) : Double {
    return when {
        transmissionPower < -30 -> 1.0
        transmissionPower < -25 -> getAdvertisingRange(transmissionPower, -30, 2)
        transmissionPower < -18 -> getAdvertisingRange(transmissionPower, -20, 4)
        transmissionPower < -14 -> getAdvertisingRange(transmissionPower, -16, 16)
        transmissionPower < -10 -> getAdvertisingRange(transmissionPower, -12, 20)
        transmissionPower < -6 -> getAdvertisingRange(transmissionPower, -8, 30)
        transmissionPower < -2 -> getAdvertisingRange(transmissionPower, -4, 40)
        transmissionPower < 2 -> getAdvertisingRange(transmissionPower, 0, 60)
        else -> getAdvertisingRange(transmissionPower, 4, 70)
    }
}

/**
 * Uses a simple rule of three equation. Transmission power values will be incremented by 100 to
 * compensate for negative values.
 */
fun getAdvertisingRange(transmissionPower: Int, calibratedTransmissionPower: Int, calibratedRange: Int): Double {
    return calibratedRange.times(transmissionPower.plus(100).div(calibratedTransmissionPower.plus(100).toDouble()))
}