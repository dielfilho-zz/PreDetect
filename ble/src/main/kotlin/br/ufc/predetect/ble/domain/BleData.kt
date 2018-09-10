package br.ufc.predetect.ble.domain

import android.os.Parcel
import android.os.Parcelable

data class BleData (
        val ssId: String? = "",
        val MAC: String? = "",
        var distance: Double = 0.0,
        var rssI: Int = 0,
        var observeCount: Int = 0,
        var percent: Double = 0.0
) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readDouble(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readDouble())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(ssId)
        parcel.writeString(MAC)
        parcel.writeDouble(distance)
        parcel.writeInt(rssI)
        parcel.writeInt(observeCount)
        parcel.writeDouble(percent)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<BleData> {
        override fun createFromParcel(parcel: Parcel): BleData = BleData(parcel)
        override fun newArray(size: Int): Array<BleData?> = arrayOfNulls(size)
    }

}