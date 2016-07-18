package danielfilho.ufc.br.com.predetect.datas;

/**
 * Created by daniel filho on 5/27/16.
 */
public abstract class NetworkData {
    private int RSSI;
    protected String MAC;
    private double distance;


    public NetworkData(String MAC, int RSSI, double distance) {
        this.MAC = MAC;
        this.RSSI = RSSI;
        this.distance = distance;
    }

    public NetworkData(String MAC, int RSSI) {
        this.RSSI = RSSI;
        this.MAC = MAC;
    }

    public NetworkData() {
        this.RSSI = 0;
        this.MAC = "none";
        this.distance = 0;
    }

    public int getRSSI() {
        return RSSI;
    }

    public void setRSSI(int RSSI) {
        this.RSSI = RSSI;
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


}
