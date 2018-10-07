package br.ufc.predetect.ble.domain

import android.os.Parcel
import android.os.Parcelable

data class BeaconBundle(
        val beaconData : MutableList<String> = emptyList<String>() as MutableList<String>,
        val observeTime: Int = 0,
        val distanceRange: Double = 0.0
) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readArrayList(String::class.java.classLoader) as MutableList<String>,
            parcel.readInt(),
            parcel.readDouble()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeList(beaconData)
        parcel.writeInt(observeTime)
        parcel.writeDouble(distanceRange)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<BeaconBundle> {
        override fun createFromParcel(parcel: Parcel): BeaconBundle = BeaconBundle(parcel)
        override fun newArray(size: Int): Array<BeaconBundle?> = arrayOfNulls(size)
    }
}