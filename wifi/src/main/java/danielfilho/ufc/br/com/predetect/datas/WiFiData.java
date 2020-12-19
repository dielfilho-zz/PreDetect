package danielfilho.ufc.br.com.predetect.datas;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Locale;

/**
 *
 * @author Daniel Filho
 * @since 2016
 *
 */
public class WiFiData implements Parcelable{
    private String SSID;
    private String MAC;
    private double distance;
    private int RSSI;

    private int observeCount;
    private double percent;

    public WiFiData(String MAC, int RSSI, double distance, String SSID, int observeCount, double percent) {
        this.SSID = SSID;
        this.MAC = MAC;
        this.distance = distance;
        this.RSSI = RSSI;
        this.observeCount = observeCount;
        this.percent = percent;
    }

    public WiFiData(String MAC, int RSSI, double distance, String SSID) {
        this.MAC = MAC;
        this.RSSI = RSSI;
        this.SSID = SSID;
        this.distance = distance;
        this.observeCount = 0;
        this.percent = 0;
    }

    public WiFiData(String MAC){
        this.MAC = MAC;
    }

    protected WiFiData(Parcel in) {
        MAC = in.readString();
        RSSI = in.readInt();
        SSID = in.readString();
        distance = in.readDouble();
        observeCount = in.readInt();
        percent = in.readDouble();
    }

    public String getSSID() {
        return SSID;
    }

    public void setSSID(String SSID) {
        this.SSID = SSID;
    }

    public int getObserveCount() {
        return observeCount;
    }

    public void setObserveCount(int observeCount) {
        this.observeCount = observeCount;
    }

    public double getPercent() {
        return percent;
    }

    public void setPercent(double percent) {
        this.percent = percent;
    }

    public String getMAC() {
        return MAC;
    }

    public void setMAC(String MAC) {
        this.MAC = MAC;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getRSSI() {
        return RSSI;
    }

    public void setRSSI(int RSSI) {
        this.RSSI = RSSI;
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH,
                "WiFiData{SSID=%s, observeCount=%d, percent=%.2f, MAC=%s, Distance=%.2f}",
                SSID, observeCount, percent, MAC, distance);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(MAC);
        dest.writeInt(RSSI);
        dest.writeString(SSID);
        dest.writeDouble(distance);
        dest.writeInt(observeCount);
        dest.writeDouble(percent);
    }

    public static final Creator<WiFiData> CREATOR = new Creator<WiFiData>() {
        @Override
        public WiFiData createFromParcel(Parcel in) {
            return new WiFiData(in);
        }

        @Override
        public WiFiData[] newArray(int size) {
            return new WiFiData[size];
        }
    };

    @Override
    public int hashCode() {
        return getMAC() != null ? getMAC().hashCode() : 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if(o == this)
            return true;
        else if(!(o instanceof WiFiData))
            return false;

        WiFiData data = (WiFiData) o;
        return (data.getMAC().equalsIgnoreCase(this.getMAC()));

    }

}
