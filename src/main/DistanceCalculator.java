package main;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class DistanceCalculator {


    // A cache to store calculated distances between two points
    private static final Map<String, Double> distanceCache = new HashMap<>();
    private static final String CACHE_FILE_PATH = "distanceCache.ser"; // Path to save the cache

    // Method to generate a unique key for the cache based on the coordinates
    private static String generateCacheKey(double lat1, double lon1, double lat2, double lon2) {
        return lat1 + "," + lon1 + "," + lat2 + "," + lon2;
    }

    // Method to print the contents of the cache
    public static void printCache() {
        if (distanceCache.isEmpty()) {
            System.out.println("Cache is empty.");
        } else {
            System.out.println("Cached distances:");
            for (Map.Entry<String, Double> entry : distanceCache.entrySet()) {
                System.out.println("Route: " + entry.getKey() + " | Distance: " + entry.getValue() + " km");
            }
        }
    }

    // Method to clear the cache (optional)
    public static void clearCache() {
        distanceCache.clear();
        System.out.println("Cache cleared.");
    }

    // Save the cache to a file before program terminates
    public static void saveCacheToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(CACHE_FILE_PATH))) {
            oos.writeObject(distanceCache);
            System.out.println("Cache saved to file.");
        } catch (IOException e) {
            System.err.println("Error saving cache to file: " + e.getMessage());
        }
    }

    // Load the cache from the file when the program starts
    public static void loadCacheFromFile() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(CACHE_FILE_PATH))) {
            Map<String, Double> loadedCache = (Map<String, Double>) ois.readObject();
            distanceCache.putAll(loadedCache);
            System.out.println("Cache loaded from file.");
        } catch (FileNotFoundException e) {
            System.out.println("No previous cache file found.");
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading cache from file: " + e.getMessage());
        }
    }

    // Method to calculate the Haversine distance between two lat/lng points
    public static double haversine(double lat1, double lon1, double lat2, double lon2) {

        String cacheKey = generateCacheKey(lat1, lon1, lat2, lon2);

        // Check if the distance is already cached
        //loadCacheFromFile();
        if (distanceCache.containsKey(cacheKey)) {
            return distanceCache.get(cacheKey);
        }
        //printCache();

        final int R = 6371; // Radius of the earth in kilometers

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c; // convert to kilometers

        // Store the result in the cache
        distanceCache.put(cacheKey, distance);
        //saveCacheToFile();
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

