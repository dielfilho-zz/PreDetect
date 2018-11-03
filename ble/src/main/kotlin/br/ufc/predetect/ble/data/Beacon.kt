package br.ufc.predetect.ble.data

import android.os.Parcel
import android.os.Parcelable

data class Beacon(
        val name : String? = "",
        val macAddress: String = "",
        val transmissionPower : Int = 0,
        var distance: Double = 0.0,
        var rssi: Int = 0,
        var observeCount: Int = 0,
        var percent: Double = 0.0
) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString() ?: "",
            parcel.readInt(),
            parcel.readDouble(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readDouble())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(macAddress)
        parcel.writeInt(transmissionPower)
        parcel.writeDouble(distance)
        parcel.writeInt(rssi)
        parcel.writeInt(observeCount)
        parcel.writeDouble(percent)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Beacon> {
        override fun createFromParcel(parcel: Parcel): Beacon = Beacon(parcel)
        override fun newArray(size: Int): Array<Beacon?> = arrayOfNulls(size)
    }

    override fun hashCode(): Int {
        return macAddress.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other === this)
            return true
        else if (other !is Beacon)
            return false

        val data: Beacon = other
        return data.macAddress.equals(macAddress, ignoreCase = true)
    }

}
