// Class to represent a Delivery Point
public class DeliveryPoint {
    int idDelivery;
    double lat;
    double lng;
    long timestamp;

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

