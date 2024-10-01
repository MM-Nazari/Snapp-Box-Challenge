package main;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeliveryFareEstimation {

    // Method to read CSV and store it in a list of DeliveryPoint
    public static List<DeliveryPoint> readData(String filePath) throws IOException {
        List<DeliveryPoint> data = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        String line;

        // Skip the header if there is one
        br.readLine();

        // Read each line from the CSV
        while ((line = br.readLine()) != null) {
            String[] values = line.split(",");
            int idDelivery = Integer.parseInt(values[0]);
            double lat = Double.parseDouble(values[1]);
            double lng = Double.parseDouble(values[2]);
            long timestamp = Long.parseLong(values[3]);

            // Create a DeliveryPoint object and add it to the list
            data.add(new DeliveryPoint(idDelivery, lat, lng, timestamp));
        }

        br.close();
        return data;
    }

    // Method to filter invalid points where speed > 100 km/h
    public static List<DeliveryPoint> filterInvalidPoints(List<DeliveryPoint> points) {
        List<DeliveryPoint> validPoints = new ArrayList<>();

        // Add the first point to the valid list, as it's always valid
        validPoints.add(points.get(0));

        for (int i = 1; i < points.size(); i++) {
            DeliveryPoint p1 = points.get(i - 1);
            DeliveryPoint p2 = points.get(i);

            // Calculate the speed between p1 and p2
            double speed = DistanceCalculator.calculateSpeed(p1, p2);

            // If the speed is less than or equal to 100 km/h, we keep p2
            if (speed <= 100) {
                validPoints.add(p2);
            }
        }

        return validPoints;
    }

    // Method to calculate the fare for a single delivery
    public static double calculateFare(List<DeliveryPoint> filteredPoints) {
        double fare = 1.30; // Start with the flag amount of 1.30 units

        for (int i = 1; i < filteredPoints.size(); i++) {
            DeliveryPoint p1 = filteredPoints.get(i - 1);
            DeliveryPoint p2 = filteredPoints.get(i);

            // Calculate speed between p1 and p2
            double speed = DistanceCalculator.calculateSpeed(p1, p2);
            // Calculate distance between p1 and p2
            double distance = DistanceCalculator.haversine(p1.lat, p1.lng, p2.lat, p2.lng);
            // Calculate the time difference (in hours)
            double timeDifferenceInHours = (p2.timestamp - p1.timestamp) / 3600.0; // Seconds to hours

            // Extract the hour of the day from the timestamp (assuming Unix timestamp in seconds)
            int hourOfDay = (int) ((p1.timestamp % 86400) / 3600); // Seconds to hours in a day

            // Apply pricing rules based on speed and time of the day
            if (speed > 10) {
                if (hourOfDay >= 5 && hourOfDay <= 24) {
                    // Rule 1: Day time rate (0.74 units per km)
                    fare += distance * 0.74;
                } else {
                    // Rule 2: Night time rate (1.3 units per km)
                    fare += distance * 1.3;
                }
            } else {
                // Rule 3: Low speed fare (11.9 units per hour)
                fare += timeDifferenceInHours * 11.9;
            }
        }

        // Ensure the minimum fare is 3.47 units
        if (fare < 3.47) {
            fare = 3.47;
        }

        return fare;
    }

    // Method to write the output to a CSV file
    public static void writeOutputToCSV(Map<Integer, Double> fareEstimates, String outputPath) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {
            writer.write("id_delivery,fare_estimate\n");
            for (Map.Entry<Integer, Double> entry : fareEstimates.entrySet()) {
                writer.write(entry.getKey() + "," + String.format("%.2f", entry.getValue()) + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String filePath = "src/sample_data.csv";
        String outputPath = "output.csv";// Path to the CSV file

        try {

            // Read the CSV data into a list of DeliveryPoint objects
            List<DeliveryPoint> deliveryPoints = readData(filePath);

            // List to store deleted records with their speeds
            //List<String> deletedRecords = new ArrayList<>();

            // Group the points by id_delivery
            Map<Integer, List<DeliveryPoint>> deliveries = new HashMap<>();
            for (DeliveryPoint point : deliveryPoints) {
                deliveries.computeIfAbsent(point.idDelivery, k -> new ArrayList<>()).add(point);
            }

            // Print the grouped deliveries
            for (Map.Entry<Integer, List<DeliveryPoint>> entry : deliveries.entrySet()) {
                int idDelivery = entry.getKey();
                List<DeliveryPoint> points = entry.getValue();
                System.out.println("id_delivery: " + idDelivery);
                for (DeliveryPoint deliveryPoint : points) {
                    System.out.println("    " + deliveryPoint);
                }
            }

            // Map to store fare estimates for each delivery
            Map<Integer, Double> fareEstimates = new HashMap<>();

            // Process each delivery and calculate the fare
            for (Map.Entry<Integer, List<DeliveryPoint>> entry : deliveries.entrySet()) {
                int idDelivery = entry.getKey();
                List<DeliveryPoint> points = entry.getValue();

                // Filter out invalid points where speed > 100 km/h for the current delivery only
                List<DeliveryPoint> filteredPoints = filterInvalidPoints(points);  // Fix: Pass 'points' instead of 'deliveryPoints'

                // Calculate the fare for the filtered points of the current delivery
                double fare = calculateFare(filteredPoints);

                // Store the fare estimate for this delivery
                fareEstimates.put(idDelivery, fare);
            }

//            // Process each delivery and calculate the fare
//            for (Map.Entry<Integer, List<DeliveryPoint>> entry : deliveries.entrySet()) {
//                int idDelivery = entry.getKey();
//                List<DeliveryPoint> points = entry.getValue();
//
//                // Filter out invalid points where speed > 100 km/h
//                //List<DeliveryPoint> filteredPoints = filterInvalidPoints(points, deletedRecords);
//                List<DeliveryPoint> filteredPoints = filterInvalidPoints(deliveryPoints);
//
//                // Calculate the fare for the filtered points
//                double fare = calculateFare(filteredPoints);
//
//                // Store the fare estimate for this delivery
//                fareEstimates.put(idDelivery, fare);
//            }

            // Write the fare estimates to the output CSV file
            writeOutputToCSV(fareEstimates, outputPath);

            System.out.println("Fare estimates have been written to: " + outputPath);
//            // Read the CSV data into a list of DeliveryPoint objects
//            List<DeliveryPoint> deliveryPoints = readData(filePath);
//
//            // Filter out invalid points where speed > 100 km/h
//            List<DeliveryPoint> filteredPoints = filterInvalidPoints(deliveryPoints);
//
//            // Calculate the fare for the filtered points
//            double totalFare = calculateFare(filteredPoints);
//
//            // Print the delivery points to check the output
//            for (DeliveryPoint point : deliveryPoints) {
//                System.out.println(point);
//            }
//
//            System.out.println("///////////////////////");
//
//            // Print the valid delivery points after filtering
//            for (DeliveryPoint point : filteredPoints) {
//                System.out.println(point);
//            }
//
//            System.out.println("///////////////////////");
//
//            // Print the total calculated fare
//            System.out.println("\nTotal Fare: " + totalFare);
//
//
//            // Group the points by id_delivery
//            Map<Integer, List<DeliveryPoint>> deliveries = new HashMap<>();
//            for (DeliveryPoint point : deliveryPoints) {
//                deliveries.computeIfAbsent(point.idDelivery, k -> new ArrayList<>()).add(point);
//            }
//
//            // Map to store fare estimates for each delivery
//            Map<Integer, Double> fareEstimates = new HashMap<>();
//
//            // Process each delivery and calculate the fare
//            for (Map.Entry<Integer, List<DeliveryPoint>> entry : deliveries.entrySet()) {
//                int idDelivery = entry.getKey();
//                List<DeliveryPoint> points = entry.getValue();
//
//                // Filter out invalid points where speed > 100 km/h
//                //List<DeliveryPoint> filteredPoints = filterInvalidPoints(points, deletedRecords);
//
//                // Calculate the fare for the filtered points
//                double fare = calculateFare(filteredPoints);
//
//                // Store the fare estimate for this delivery
//                fareEstimates.put(idDelivery, fare);
//            }
//
//            // Write the fare estimates to the output CSV file
//            writeOutputToCSV(fareEstimates, outputPath);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
