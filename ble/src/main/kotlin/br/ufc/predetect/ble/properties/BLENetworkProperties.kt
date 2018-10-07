package br.ufc.predetect.ble.properties

/**
 * @author Gabriel Cesar
 * @since 2018
 *
 */
//TODO
object BLENetworkProperties {
    val rssiAtOneMeter : Int = -38
    val signalLoss : Double = 10.times(3.5)
    val signalLossAtOneMeter : Int = -41
    val pathLossInOpenSpace : Double = 2.0
    val pathLossIndoor : Double = 1.7
    val pathLossInOfficeHardPartition : Double = 3.0
}