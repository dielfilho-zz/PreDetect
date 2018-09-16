package br.ufc.predetect.ble.domain

import android.os.Parcel
import android.os.Parcelable

data class Beacon(
        val SSID: String? = "",
        val MAC: String? = "",
        var distance: Double = 0.0,
        var distanceEstimated : Double = 0.0,
        var RSSI: Int = 0,
        var RSSIEstimated : Int = 0,
        var observeCount: Int = 0,
        var percent: Double = 0.0
) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readDouble(),
            parcel.readDouble(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readDouble())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(SSID)
        parcel.writeString(MAC)
        parcel.writeDouble(distance)
        parcel.writeInt(RSSI)
        parcel.writeInt(observeCount)
        parcel.writeDouble(percent)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Beacon> {
        override fun createFromParcel(parcel: Parcel): Beacon = Beacon(parcel)
        override fun newArray(size: Int): Array<Beacon?> = arrayOfNulls(size)
    }

}
