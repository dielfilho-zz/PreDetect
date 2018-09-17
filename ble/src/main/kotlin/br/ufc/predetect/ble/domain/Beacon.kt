package br.ufc.predetect.ble.domain

import android.os.Parcel
import android.os.Parcelable
import br.ufc.predetect.ble.utils.getAdvertisingPacketsBetween

data class Beacon(
        val SSId: String? = "",
        val MAC: String? = "",
        val transmissionPower : Int = 0,
        var distance: Double = 0.0,
        var distanceEstimated : Double = 0.0,
        var RSSi: Int = 0,
        var RSSiEstimated : Int = 0,
        var observeCount: Int = 0,
        var percent: Double = 0.0,
        var advertisingPackets: MutableList<AdvertisingPacket> = emptyList<AdvertisingPacket>() as MutableList<AdvertisingPacket>
) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readInt(),
            parcel.readDouble(),
            parcel.readDouble(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readDouble(),
            parcel.readArrayList(AdvertisingPacket::class.java.classLoader) as MutableList<AdvertisingPacket>)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(SSId)
        parcel.writeString(MAC)
        parcel.writeInt(transmissionPower)
        parcel.writeDouble(distance)
        parcel.writeInt(RSSi)
        parcel.writeInt(RSSiEstimated)
        parcel.writeInt(observeCount)
        parcel.writeDouble(percent)
        parcel.writeList(advertisingPackets)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Beacon> {
        override fun createFromParcel(parcel: Parcel): Beacon = Beacon(parcel)
        override fun newArray(size: Int): Array<Beacon?> = arrayOfNulls(size)
    }

    /**
     *
     * Returns an list of AdvertisingPackets that have been received in the specified time range.
     * If no packets match, an empty list will be returned.
     *
     * @param startTimestamp minimum timestamp, inclusive
     * @param endTimestamp   maximum timestamp, exclusive
     *
     */
    fun getAdvertisingPacketsBetween(startTimestamp: Long, endTimestamp: Long): List<AdvertisingPacket> {
        return getAdvertisingPacketsBetween(advertisingPackets, startTimestamp, endTimestamp)
    }
}
