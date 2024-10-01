package main;
public class DeliveryPoint {
    public int idDelivery;
    public double lat;
    public double lng;
    public long timestamp;

    // Constructor
    public DeliveryPoint(int idDelivery, double lat, double lng, long timestamp) {
        this.idDelivery = idDelivery;
        this.lat = lat;
        this.lng = lng;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "DeliveryPoint{" +
                "idDelivery=" + idDelivery +
                ", lat=" + lat +
                ", lng=" + lng +
                ", timestamp=" + timestamp +
                '}';
    }
}

