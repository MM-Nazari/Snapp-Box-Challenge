package main;
public class DistanceCalculator {

    // Method to calculate the Haversine distance between two lat/lng points
    public static double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth in kilometers

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c; // convert to kilometers

        return distance;
    }

    // Method to calculate speed in km/h between two points
    public static double calculateSpeed(DeliveryPoint p1, DeliveryPoint p2) {
        // Calculate the time difference (in hours)
        double timeDifferenceInHours = (p2.timestamp - p1.timestamp) / 3600.0; // Seconds to hours

        if (timeDifferenceInHours == 0) return 0;

        // Calculate the distance between the two points
        double distance = haversine(p1.lat, p1.lng, p2.lat, p2.lng);

        // Speed in km/h
        return distance / timeDifferenceInHours;
    }
}

