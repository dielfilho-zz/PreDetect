package br.ufc.quixada.predetect.common.utils


/**
 * Calculate the distance
 *
 * @param rssi              the currently measured RSSI
 * @param rssiAtOneMeter    the RSSI measured at 1m distance
 * @param pathLossParameter the path-loss adjustment parameter
 *
 * @see [log-distance path loss model](https://en.wikipedia.org/wiki/Log-distance_path_loss_model)
 */
fun calculateDistance(rssi: Double, rssiAtOneMeter: Double, pathLossParameter: Double): Double {
    return Math.pow(10.0, (rssiAtOneMeter - rssi) / (10 * pathLossParameter))
}