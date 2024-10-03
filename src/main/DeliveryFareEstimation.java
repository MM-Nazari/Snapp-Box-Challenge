package main;

import java.io.*;
import java.time.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class DeliveryFareEstimation {



    // Executor service to manage threads
    private static final ExecutorService executor = Executors.newFixedThreadPool(4);  // Adjust pool size based on system

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

            System.out.println(timeDifferenceInHours);

            // Extract hour of day from both timestamps
            //int hourOfDayStart = (int) ((p1.timestamp % 86400) / 3600);
            // Extract the hour of day
            // Convert timestamps to LocalDateTime in the specified timezone

            // Assuming you are working with Iran Time (IRDT/IRST)
//            ZoneId zoneId = ZoneId.of("Asia/Tehran");
//
//            LocalDateTime startDateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(p1.timestamp), zoneId);
//            LocalDateTime endDateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(p2.timestamp), zoneId);
//
//            int hourOfDayStart = startDateTime.getHour();
//            int hourOfDayEnd = endDateTime.getHour();
//            System.out.println(hourOfDayStart);
//              int hourOfDayEnd = (int) ((p2.timestamp % 86400) / 3600);
//              int hourOfDayStart = (int) ((p1.timestamp % 86400) / 3600);
//            System.out.println(hourOfDayEnd);

//// Assuming you are working with Iran Time (IRDT/IRST)
//            ZoneId zoneId = ZoneId.of("Asia/Tehran");
//
//// Convert timestamps to LocalDateTime in the specified timezone
//            LocalDateTime startDateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(p1.timestamp), zoneId);
//            LocalDateTime endDateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(p2.timestamp), zoneId);

            // Convert timestamps to LocalDateTime in the specified timezone (Tehran)
            ZoneId zoneId = ZoneId.of("Asia/Tehran");
            LocalDateTime startTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(p1.timestamp), zoneId);
            LocalDateTime endTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(p2.timestamp), zoneId);

            // Calculate total time difference in hours
            double totalDurationInHours = Duration.between(startTime, endTime).toMinutes() / 60.0;

            // Calculate distance between the points
            //double distance = DistanceCalculator.haversine(p1.lat, p1.lng, p2.lat, p2.lng);

            // Define the fare periods (Night: 00:00 to 5:00, Day: 5:00 to 24:00)
            LocalTime nightEnd = LocalTime.of(5, 0);
            LocalTime dayStart = LocalTime.of(5, 0);
            LocalTime midnight = LocalTime.of(0, 0);

            // Check if the trip crosses from day to night or vice versa
            if (startTime.toLocalTime().isBefore(nightEnd) && endTime.toLocalTime().isAfter(dayStart)) {
                // Trip spans both night and day

                // Calculate time spent at night (from start to 5:00 AM)
                double nighttimeDuration = Duration.between(startTime.toLocalTime(), nightEnd).toMinutes() / 60.0;
                double daytimeDuration = totalDurationInHours - nighttimeDuration; // The rest is daytime

                fare += nighttimeDuration * distance * 1.3; // Apply night rate
                fare += daytimeDuration * distance * 0.74; // Apply day rate

            } else if (startTime.toLocalTime().isAfter(dayStart) && endTime.toLocalTime().isBefore(midnight)) {
                // Entire trip during the day
                fare += totalDurationInHours * distance * 0.74;

            } else if (startTime.toLocalTime().isBefore(dayStart) && endTime.toLocalTime().isBefore(dayStart)) {
                // Entire trip during the night
                fare += totalDurationInHours * distance * 1.3;

            } else {
                // Trip crosses midnight (for example, from 11:30 PM to 12:30 AM)

                // Calculate time before midnight
                double preMidnightDuration = Duration.between(startTime.toLocalTime(), midnight).toMinutes() / 60.0;
                double postMidnightDuration = totalDurationInHours - preMidnightDuration;

                fare += preMidnightDuration * distance * 0.74; // Apply night rate for time before midnight
                fare += postMidnightDuration * distance * 1.3; // Apply day rate for time after midnight
            }
        }

        // Ensure the minimum fare is 3.47 units
        if (fare < 3.47) {
            fare = 3.47;
        }

        return fare;
    }

//            // For London
//            ZoneId londonZoneId = ZoneId.of("Europe/London");
//
//// For Paris
//            ZoneId parisZoneId = ZoneId.of("Europe/Paris");
//
//// Convert timestamps to LocalDateTime
//            LocalDateTime startDateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(p1.timestamp), londonZoneId);
//            LocalDateTime endDateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(p2.timestamp), londonZoneId);
// Extract hours and minutes
//            int hourOfDayStart = startDateTime.getHour();
//            int hourOfDayEnd = endDateTime.getHour();
//            System.out.println(hourOfDayStart);
//            System.out.println(hourOfDayEnd);
//
//
//// Calculate the total duration in hours
//            double totalDuration = (double) Duration.between(startDateTime, endDateTime).toMinutes() / 60.0;
//            System.out.println(totalDuration);
//// Apply pricing rules based on speed and time of the day
//            if (speed > 10) {
//                // Daytime rate is from 5 AM to 12 AM (midnight)
//                if (hourOfDayStart < 5 && hourOfDayEnd > 5) {
//                    // Trip starts at night and ends during the day
//                    double nighttimeDuration = 5 - hourOfDayStart; // Duration at night
//                    double daytimeDuration = totalDuration - nighttimeDuration; // Duration during the day
//                    fare += nighttimeDuration * distance * 1.3; // Nighttime rate
//                    fare += daytimeDuration * distance * 0.74; // Daytime rate
//                } else if (hourOfDayStart >= 5 && hourOfDayEnd <= 24) {
//                    // Entire trip during the day
//                    fare += totalDuration * distance * 0.74;
//                } else {
//                    // Entire trip at night
//                    fare += totalDuration * distance * 1.3;
//                }
//            } else {
//                // Rule 3: Low speed fare (11.9 units per hour)
//                fare += totalDuration * 11.9;
//            }
//
//// Ensure the minimum fare is 3.47 units
//            if (fare < 3.47) {
//                fare = 3.47;
//            }
//
////            // Extract the hour of the day from the timestamp (assuming Unix timestamp in seconds)
////             int hourOfDay = (int) ((p1.timestamp % 86400) / 3600); // Seconds to hours in a day
////           // int hourOfDay = (int) (((p1.timestamp + (3 * 3600 + 30 * 60)) % 86400) / 3600);
////
////
////            // Apply pricing rules based on speed and time of the day
////            if (speed > 10) {
////                if (hourOfDay > 5 && hourOfDay <= 24) {
////                    // Rule 1: Day time rate (0.74 units per km)
////                    fare += distance * 0.74;
////                } else {
////                    // Rule 2: Night time rate (1.3 units per km)
////                    fare += distance * 1.3;
////                }
////            } else {
////                // Rule 3: Low speed fare (11.9 units per hour)
////                fare += timeDifferenceInHours * 11.9;
////            }
//        }
//
//        // Ensure the minimum fare is 3.47 units
//        if (fare < 3.47) {
//            fare = 3.47;
//        }
//
//        return fare;
//    }

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

    // Task to process a batch of deliveries in parallel
    public static class DeliveryTask implements Callable<Map<Integer, Double>> {
        private final Map<Integer, List<DeliveryPoint>> deliveries;

        public DeliveryTask(Map<Integer, List<DeliveryPoint>> deliveries) {
            this.deliveries = deliveries;
        }

        @Override
        public Map<Integer, Double> call() {
            Map<Integer, Double> fareEstimates = new HashMap<>();

            for (Map.Entry<Integer, List<DeliveryPoint>> entry : deliveries.entrySet()) {
                int idDelivery = entry.getKey();
                List<DeliveryPoint> points = entry.getValue();

                // Filter out invalid points where speed > 100 km/h
                List<DeliveryPoint> filteredPoints = filterInvalidPoints(points);

                // Calculate the fare for the filtered points
                double fare = calculateFare(filteredPoints);

                // Store the fare estimate for this delivery
                fareEstimates.put(idDelivery, fare);
            }

            return fareEstimates;
        }
    }


    public static void main(String[] args) {
        String filePath = "src/expanded_delivery_data.csv";
        String outputPath = "output.csv";// Path to the CSV file

        try {
//             Measure time without cache
            long startTime = System.nanoTime();

            // Read the CSV data into a list of DeliveryPoint objects
            List<DeliveryPoint> deliveryPoints = readData(filePath);

            // Group the points by id_delivery
            Map<Integer, List<DeliveryPoint>> deliveries = new HashMap<>();
            for (DeliveryPoint point : deliveryPoints) {
                deliveries.computeIfAbsent(point.idDelivery, k -> new ArrayList<>()).add(point);
            }

            // Split the deliveries into batches for multithreading
            List<Map<Integer, List<DeliveryPoint>>> batches = new ArrayList<>();
            int batchSize = deliveries.size() / 4;  // Divide the deliveries for each thread (4 threads in this case)
            Map<Integer, List<DeliveryPoint>> currentBatch = new HashMap<>();
            int count = 0;

            for (Map.Entry<Integer, List<DeliveryPoint>> entry : deliveries.entrySet()) {
                currentBatch.put(entry.getKey(), entry.getValue());
                count++;
                if (count % batchSize == 0) {
                    batches.add(new HashMap<>(currentBatch));
                    currentBatch.clear();
                }
            }
            if (!currentBatch.isEmpty()) {
                batches.add(new HashMap<>(currentBatch)); // Add any remaining deliveries
            }

            // Submit tasks to the thread pool
            List<Future<Map<Integer, Double>>> futures = new ArrayList<>();
            for (Map<Integer, List<DeliveryPoint>> batch : batches) {
                futures.add(executor.submit(new DeliveryTask(batch)));
            }

            // Collect the results
            Map<Integer, Double> finalFareEstimates = new HashMap<>();
            for (Future<Map<Integer, Double>> future : futures) {
                finalFareEstimates.putAll(future.get()); // Merging all fare estimates from each thread
            }

            // Write the fare estimates to the output CSV file
            writeOutputToCSV(finalFareEstimates, outputPath);

            System.out.println("Fare estimates have been written to: " + outputPath);

            long endTime = System.nanoTime();
            long totalTime = endTime - startTime;
            System.out.println("Execution time: " + totalTime / 1_000_000 + " ms");

        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }

//        try {
//
//            // Measure time without cache
//            long startTime = System.nanoTime();
//
//            // Read the CSV data into a list of DeliveryPoint objects
//            List<DeliveryPoint> deliveryPoints = readData(filePath);
//
//            // List to store deleted records with their speeds
//            //List<String> deletedRecords = new ArrayList<>();
//
//            // Group the points by id_delivery
//            Map<Integer, List<DeliveryPoint>> deliveries = new HashMap<>();
//            for (DeliveryPoint point : deliveryPoints) {
//                deliveries.computeIfAbsent(point.idDelivery, k -> new ArrayList<>()).add(point);
//            }
//
//            // Print the grouped deliveries
////            for (Map.Entry<Integer, List<DeliveryPoint>> entry : deliveries.entrySet()) {
////                int idDelivery = entry.getKey();
////                List<DeliveryPoint> points = entry.getValue();
////                System.out.println("id_delivery: " + idDelivery);
////                for (DeliveryPoint deliveryPoint : points) {
////                    System.out.println("    " + deliveryPoint);
////                }
////            }
//
//            // Map to store fare estimates for each delivery
//            Map<Integer, Double> fareEstimates = new HashMap<>();
//
//            // Process each delivery and calculate the fare
//            for (Map.Entry<Integer, List<DeliveryPoint>> entry : deliveries.entrySet()) {
//                int idDelivery = entry.getKey();
//                List<DeliveryPoint> points = entry.getValue();
//
//                // Filter out invalid points where speed > 100 km/h for the current delivery only
//                List<DeliveryPoint> filteredPoints = filterInvalidPoints(points);  // Fix: Pass 'points' instead of 'deliveryPoints'
//
//                // Calculate the fare for the filtered points of the current delivery
//                double fare = calculateFare(filteredPoints);
//
//                // Store the fare estimate for this delivery
//                fareEstimates.put(idDelivery, fare);
//            }
//
//
//            // Write the fare estimates to the output CSV file
//            writeOutputToCSV(fareEstimates, outputPath);
//
//
//            System.out.println("Fare estimates have been written to: " + outputPath);
//
//            long endTime = System.nanoTime();
//            long totalTime = endTime - startTime;
//            System.out.println("Execution time: " + totalTime / 1_000_000 + " ms");
//
//
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}
