package br.ufc.predetect.ble.filters

import br.ufc.predetect.ble.domain.AdvertisingPacket

interface RSSIFilter {

    fun filter(advertisingPackets : List<AdvertisingPacket>) : Double

}