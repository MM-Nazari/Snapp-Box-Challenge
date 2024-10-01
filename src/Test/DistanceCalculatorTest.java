package Test;

import main.DeliveryPoint;
import org.junit.Test;
import static org.junit.Assert.*;

import main.DeliveryFareEstimation;
import main.DistanceCalculator;

public class DistanceCalculatorTest {

    // Unit test for the Haversine distance method
    @Test
    public void testHaversine() {
        // Test with known values (London -> Paris)
        double londonLat = 51.5074;
        double londonLon = -0.1278;
        double parisLat = 48.8566;
        double parisLon = 2.3522;

        // The actual distance between London and Paris is about 343 km
        double distance = DistanceCalculator.haversine(londonLat, londonLon, parisLat, parisLon);

        // Assert that the calculated distance is within a reasonable range (±5 km tolerance)
        assertEquals(343, distance, 5);
    }

    // Unit test for the calculateSpeed method
    @Test
    public void testCalculateSpeed() {
        // Mock two DeliveryPoint objects
        DeliveryPoint point1 = new DeliveryPoint(1, 51.5074, -0.1278, 1609459200); // London at 01-Jan-2021 00:00:00
        DeliveryPoint point2 = new DeliveryPoint(1, 48.8566, 2.3522, 1609462800);  // Paris at 01-Jan-2021 01:00:00

        // Calculate speed between London and Paris over 1 hour
        double speed = DistanceCalculator.calculateSpeed(point1, point2);

        // The distance is about 343 km, so the speed should be around 343 km/h
        assertEquals(343, speed, 5);
    }

    // Additional test to check zero time difference (to prevent division by zero errors)
    @Test
    public void testCalculateSpeedWithZeroTimeDifference() {
        // Mock two points with the same timestamp
        DeliveryPoint point1 = new DeliveryPoint(1, 51.5074, -0.1278, 1609459200);
        DeliveryPoint point2 = new DeliveryPoint(1, 51.5074, -0.1278, 1609459200); // Same point and time

        // Calculate speed with zero time difference (should return 0)
        double speed = DistanceCalculator.calculateSpeed(point1, point2);

        // The speed should be 0 because the time difference is 0
        assertEquals(0, speed, 0);
    }
}