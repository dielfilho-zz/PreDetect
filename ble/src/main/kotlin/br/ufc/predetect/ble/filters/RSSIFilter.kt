package br.ufc.predetect.ble.filters

interface RSSIFilter {

    fun filter(advertisingPackets: List<Int>) : Double

}