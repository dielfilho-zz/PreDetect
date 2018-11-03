package br.ufc.predetect.ble.data

import android.os.Parcel
import android.os.Parcelable

data class BeaconBundle(
        val beaconData : List<String> = emptyList(),
        val observeTime: Long = 0,
        val distanceRange: Double = 0.0
) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readArrayList(String::class.java.classLoader) as List<String>,
            parcel.readLong(),
            parcel.readDouble()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeList(beaconData)
        parcel.writeLong(observeTime)
        parcel.writeDouble(distanceRange)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<BeaconBundle> {
        override fun createFromParcel(parcel: Parcel): BeaconBundle = BeaconBundle(parcel)
        override fun newArray(size: Int): Array<BeaconBundle?> = arrayOfNulls(size)
    }
}