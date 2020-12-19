package br.ufc.predetect.ble.data

import android.os.Parcel
import android.os.Parcelable

data class Beacon(
        var name : String? = "",
        var macAddress: String = "",
        var transmissionPower : Int = 0,
        var distance: Double = 0.0,
        var rssi: Int = 0,
        var observeCount: Int = 0,
        var percent: Double = 0.0,
        var iterationExecuted : Int = 0
) : Parcelable, Comparator<Beacon> {

    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString() ?: "",
            parcel.readInt(),
            parcel.readDouble(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readDouble(),
            parcel.readInt())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(macAddress)
        parcel.writeInt(transmissionPower)
        parcel.writeDouble(distance)
        parcel.writeInt(rssi)
        parcel.writeInt(observeCount)
        parcel.writeDouble(percent)
        parcel.writeInt(iterationExecuted)
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

    override fun compare(b1: Beacon?, b2: Beacon?): Int {
        if (b1 == null)
            return 1
        if (b2 == null)
            return -1
        return b1.macAddress.toUpperCase()
                .compareTo(b2.macAddress.toUpperCase())
    }

}
