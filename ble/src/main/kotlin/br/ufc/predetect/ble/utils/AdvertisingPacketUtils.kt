package br.ufc.predetect.ble.utils

import br.ufc.predetect.ble.domain.AdvertisingPacket

/**
 * Returns an ArrayList of AdvertisingPackets that have been received in the specified time range.
 * If no packets match, an empty list will be returned. Expects the specified AdvertisingPackets
 * to be sorted by timestamp in ascending order (i.e. the oldest timestamp first)!
 *
 * @param advertisingPackets the packets to filter (sorted by timestamp ascending)
 * @param startTimestamp     minimum timestamp, inclusive
 * @param endTimestamp       maximum timestamp, exclusive
 */
fun getAdvertisingPacketsBetween(advertisingPackets: MutableList<AdvertisingPacket>, startTimestamp: Long, endTimestamp: Long): List<AdvertisingPacket> {
    if (advertisingPackets.isEmpty()) return emptyList()

    val size = advertisingPackets.size

    val oldestAdvertisingPacket = advertisingPackets[0]
    val latestAdvertisingPacket = advertisingPackets[size - 1]

    if (endTimestamp <= oldestAdvertisingPacket.timestamp ||
            startTimestamp > latestAdvertisingPacket.timestamp)
        return emptyList()

    // find the index of the first advertising packet with a timestamp
    // larger than or equal to the specified startTimestamp
    val startIndex = advertisingPackets.binarySearch(0, size, comparison = {
        when {
            it.timestamp < startTimestamp -> -1
            it.timestamp > startTimestamp -> 1
            else -> 0
        }
    })

    // find the index of the last advertising packet with a timestamp
    // smaller than the specified endTimestamp
    val endIndex = advertisingPackets.binarySearch(0, size, comparison = {
        when {
            it.timestamp < endTimestamp -> -1
            it.timestamp > endTimestamp -> 1
            else -> 0
        }
    })

    return advertisingPackets.subList(startIndex, endIndex + 1)
}