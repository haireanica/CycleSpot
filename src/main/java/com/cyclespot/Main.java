package com.cyclespot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashSet;
import java.util.Set;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Main {

    public static void main(String[] args) {

        // Creates an HTTP client
        HttpClient client = HttpClient.newHttpClient();

        // Creates a request for Citi Bike station information
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://gbfs.citibikenyc.com/gbfs/en/station_information.json"))
                .GET()
                .build();

        try {

            // Sends the request to Citi Bike
            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            // Checks if the API request was successful
            if (response.statusCode() != 200) {
                System.out.println("API request failed: " + response.statusCode());
                return;
            }

            // Creates a Jackson ObjectMapper
            ObjectMapper mapper = new ObjectMapper();

            // Converts the JSON response into Java-readable data
            JsonNode data = mapper.readTree(response.body());

            // Gets the list of stations
            JsonNode stations = data.get("data").get("stations");

            // Stores the station IDs for Manhattan stations
            Set<String> manhattanStationIds = new HashSet<>();

            // Finds the Manhattan stations
            for (JsonNode station : stations) {

                double latitude = station.get("lat").asDouble();
                double longitude = station.get("lon").asDouble();

                // Checks if the station is inside our Manhattan area
                if (latitude >= 40.70 && latitude <= 40.88
                        && longitude >= -74.02 && longitude <= -73.90) {

                    String stationId = station.get("station_id").asText();

                    // Adds the station ID to our set
                    manhattanStationIds.add(stationId);
                }
            }

            System.out.println("Total Citi Bike stations: " + stations.size());
            System.out.println("Manhattan stations: " + manhattanStationIds.size());
            System.out.println();

            // Creates a request for Citi Bike station status
            HttpRequest statusRequest = HttpRequest.newBuilder()
                    .uri(URI.create("https://gbfs.citibikenyc.com/gbfs/en/station_status.json"))
                    .GET()
                    .build();

            // Sends the request
            HttpResponse<String> statusResponse =
                    client.send(statusRequest, HttpResponse.BodyHandlers.ofString());

            // Checks if the status request was successful
            if (statusResponse.statusCode() != 200) {
                System.out.println("Station status request failed: "
                        + statusResponse.statusCode());
                return;
            }

            // Converts the status JSON into Java-readable data
            JsonNode statusData = mapper.readTree(statusResponse.body());

            // Gets the list of station statuses
            JsonNode statusStations = statusData.get("data").get("stations");
            // Gets the timestamp for when the station status data was updated
            long timestamp = statusData.get("last_updated").asLong();
            Instant instant = Instant.ofEpochSecond(timestamp);
            ZonedDateTime dateTime = instant
                    .atZone(ZoneId.of("America/New_York"));
            DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedTime = dateTime.format(formatter);

            System.out.println("Last updated: " + formattedTime);
            System.out.println();

            // Finds the status information for Manhattan stations
            for (JsonNode station : statusStations) {

                String stationId = station.get("station_id").asText();

                // Checks if this station is in Manhattan
                if (manhattanStationIds.contains(stationId)) {

                    int bikes = station.get("num_bikes_available").asInt();
                    int docks = station.get("num_docks_available").asInt();

                    System.out.println("Station ID: " + stationId);
                    System.out.println("Available bikes: " + bikes);
                    System.out.println("Available docks: " + docks);
                    System.out.println("-------------------------");
                }
            }

        } catch (Exception e) {

            // Prints an error if something goes wrong
            e.printStackTrace();
        }
    }
}