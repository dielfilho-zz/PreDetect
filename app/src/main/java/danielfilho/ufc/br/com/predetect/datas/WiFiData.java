package danielfilho.ufc.br.com.predetect.datas;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Daniel Filho on 5/27/16.
 */
public class WiFiData extends NetworkData implements Parcelable{
    private String SSID;

    private int observeCount;
    private double percent;

    public WiFiData(String MAC, int RSSI, double distance, String SSID) {
        super(MAC, RSSI, distance);
        this.SSID = SSID;
        this.observeCount = 0;
        this.percent = 0;
    }

    public WiFiData(String SSID, String MAC, int RSSI) {
        super(MAC, RSSI);
        this.SSID = SSID;
        this.observeCount = 0;
        this.percent = 0;
    }

    public WiFiData(String MAC){
        super(MAC, 0, 0);
    }

    protected WiFiData(Parcel in) {
        SSID = in.readString();
        MAC = in.readString();
        observeCount = in.readInt();
        percent = in.readDouble();
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

    @Override
    public boolean equals(Object o) {
        if(o == this)
            return true;
        else if(!(o instanceof WiFiData))
            return false;

        WiFiData data = (WiFiData) o;
        return (data.getMAC().equals(this.getMAC()));

    }

    @Override
    public int hashCode() {
        return getMAC() != null ? getMAC().hashCode() : 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return "WiFiData{" +
                "SSID='" + SSID + '\'' +
                ", observeCount=" + observeCount +
                ", percent=" + percent +
                ", MAC="+ MAC+
                '}';
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(SSID);
        dest.writeString(MAC);
        dest.writeInt(observeCount);
        dest.writeDouble(percent);
    }
}
