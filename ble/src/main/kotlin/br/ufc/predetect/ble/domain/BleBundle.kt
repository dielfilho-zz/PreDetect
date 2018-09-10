package br.ufc.predetect.ble.domain

import android.os.Parcel
import android.os.Parcelable

data class BleBundle(
        val bleData : MutableList<String> = emptyList<String>() as MutableList<String>,
        val observeTime: Int = 0,
        val distanceRange: Double = 0.0
) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readArrayList(String::class.java.classLoader) as MutableList<String>,
            parcel.readInt(),
            parcel.readDouble()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeList(bleData)
        parcel.writeInt(observeTime)
        parcel.writeDouble(distanceRange)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<BleBundle> {
        override fun createFromParcel(parcel: Parcel): BleBundle = BleBundle(parcel)
        override fun newArray(size: Int): Array<BleBundle?> = arrayOfNulls(size)
    }
}