package Test;

import main.DeliveryFareEstimation;
import main.DeliveryPoint;
import main.DeliveryPoint.*;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import java.io.*;
import java.util.*;

public class DeliveryFareEstimationTest {

    private List<DeliveryPoint> deliveryPoints;

    @Before
    public void setUp() {
        // Initialize test data
        deliveryPoints = Arrays.asList(
                new DeliveryPoint(1, 51.5074, -0.1278, 1609459200), // London, 1st Jan 2021 00:00:00
                new DeliveryPoint(1, 48.8566, 2.3522, 1609462800), // Paris, 1st Jan 2021 01:00:00
                new DeliveryPoint(2, 52.5200, 13.4050, 1609466400), // Berlin, 1st Jan 2021 02:00:00
                new DeliveryPoint(2, 40.7128, -74.0060, 1609470000)  // New York, 1st Jan 2021 03:00:00
        );
    }

    @Test
    public void testReadData() throws IOException {
        // Create a temporary CSV file
        String csvData = "id_delivery,lat,lng,timestamp\n" +
                "1,51.5074,-0.1278,1609459200\n" +
                "1,48.8566,2.3522,1609462800\n";

        File tempFile = File.createTempFile("testData", ".csv");
        BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
        writer.write(csvData);
        writer.close();

        // Call the readData method and verify the output
        List<DeliveryPoint> points = DeliveryFareEstimation.readData(tempFile.getPath());
        assertEquals(2, points.size());
        assertEquals(1, points.get(0).idDelivery);
        assertEquals(51.5074, points.get(0).lat, 0.0001);
        assertEquals(-0.1278, points.get(0).lng, 0.0001);
        assertEquals(1609459200, points.get(0).timestamp);

        // Clean up temporary file
        tempFile.delete();
    }

    @Test
    public void testFilterInvalidPoints() {
        // Test with two delivery points, where the speed exceeds 100 km/h between them
        List<DeliveryPoint> points = Arrays.asList(
                new DeliveryPoint(1, 51.5074, -0.1278, 1609459200), // London
                new DeliveryPoint(1, 48.8566, 2.3522, 1609459500)  // Paris (too fast!)
        );

        List<DeliveryPoint> validPoints = DeliveryFareEstimation.filterInvalidPoints(points);
        assertEquals(1, validPoints.size());  // Only the first point should remain
    }

    @Test
    public void testCalculateFare() {
        // Test fare calculation with valid filtered points
        List<DeliveryPoint> filteredPoints = Arrays.asList(
                new DeliveryPoint(1, 51.5074, -0.1278, 1609459200), // London
                new DeliveryPoint(1, 48.8566, 2.3522, 1609462800)  // Paris
        );

        double fare = DeliveryFareEstimation.calculateFare(filteredPoints);
        assertTrue(fare >= 3.47);  // Minimum fare check
        assertEquals(1.30 + (0.74 * 343), fare, 5);  // Check based on day time rate and distance
    }

    @Test
    public void testWriteOutputToCSV() throws IOException {
        // Prepare fare estimates
        Map<Integer, Double> fareEstimates = new HashMap<>();
        fareEstimates.put(1, 5.50);
        fareEstimates.put(2, 8.75);

        // Create a temporary file for output
        File tempFile = File.createTempFile("testOutput", ".csv");

        // Call the writeOutputToCSV method
        DeliveryFareEstimation.writeOutputToCSV(fareEstimates, tempFile.getPath());

        // Read the output file and verify the content
        BufferedReader reader = new BufferedReader(new FileReader(tempFile));
        String header = reader.readLine();  // Read header
        assertEquals("id_delivery,fare_estimate", header);

        String line1 = reader.readLine();
        assertEquals("1,5.50", line1);

        String line2 = reader.readLine();
        assertEquals("2,8.75", line2);

        reader.close();
        tempFile.delete();  // Clean up
    }
}

