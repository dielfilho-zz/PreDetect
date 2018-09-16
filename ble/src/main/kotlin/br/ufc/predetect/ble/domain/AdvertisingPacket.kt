package br.ufc.predetect.ble.domain

class AdvertisingPacket (
        var data: ByteArray,
        var RSSI: Int = 0,
        var timestamp: Long
) {
    init {
        timestamp = System.currentTimeMillis()
    }
}