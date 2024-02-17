package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

public class Manager {
    public static String data(String text) {
        // MongoDB Atlas connection string
        String connectionString = "mongodb+srv://scriptsages2209:123456--@cluster0.fzsdnb7.mongodb.net/weather?retryWrites=true&w=majority";

        // Connect to MongoDB Atlas
        try (MongoClient mongoClient = MongoClients.create(connectionString)) {
            // Get the database
            MongoDatabase database = mongoClient.getDatabase("cluster0");

            // Get the collection
            MongoCollection<Document> collection = database.getCollection("weather_data");

            ObjectMapper objectMapper = new ObjectMapper();
            try {
                JsonNode jsonResponse = objectMapper.readTree(text);

                // Extract temperature
                JsonNode main = jsonResponse.get("main");
                double tempC = main.get("temp").asDouble() - 273.15;

                // Extract humidity
                int humidity = main.get("humidity").asInt();

                // Extract wind speed
                JsonNode wind = jsonResponse.get("wind");
                double windSpeed = wind.get("speed").asDouble();

                // Extract air pressure
                double pressure = main.get("pressure").asDouble();

                // Extract location and country
                JsonNode sys = jsonResponse.get("sys");
                String location = jsonResponse.get("name").asText();
                String country = sys.get("country").asText();

                // Prepare the response with all weather information
                String res = "Weather in " + location + ", " + country + ": "
                        + "Temperature: " + String.format("%.2f", tempC) + "°C, "
                        + "Humidity: " + humidity + "%, "
                        + "Wind Speed: " + windSpeed + " m/s, "
                        + "Pressure: " + pressure + " hPa";

                // Store weather data in MongoDB
                Document weatherDocument = new Document("location", location)
                        .append("country", country)
                        .append("temperature", tempC)
                        .append("humidity", humidity)
                        .append("wind_speed", windSpeed)
                        .append("pressure", pressure);

                collection.insertOne(weatherDocument);

                // You can add future temperature fetching logic here for multiple days

                return res;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "Failed to fetch weather information";
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error connecting to MongoDB Atlas");
        }
        return "Failed to fetch weather information";
    }
}
