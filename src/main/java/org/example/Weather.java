package org.example;

import static spark.Spark.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

public class Weather {
    public static void main(String[] args) {
        // MongoDB Atlas connection string
        String connectionString = "mongodb+srv://scriptsages2209:123456--@cluster0.fzsdnb7.mongodb.net/weather?retryWrites=true&w=majority";

        // Connect to MongoDB Atlas
        try (MongoClient mongoClient = MongoClients.create(connectionString)) {
            // Get the database
            MongoDatabase database = mongoClient.getDatabase("Cluster0");

            // Get the collection
            MongoCollection<Document> collection = database.getCollection("weather_data");

            port(8080); // Port used by the Spark framework

            // Define a route to render the form
            get("/", (req, res) -> {
                return "<form action='/' method='post'>" +
                        "Enter the city name: <input type='text' name='username'>" +
                        "<input type='submit' value='Submit'>" +
                        "</form>";
            });

            // Define a route to handle form submission
            post("/", (req, res) -> {
                String city = req.queryParams("username");
                String apiKey = "d94a1a5aa3c285a26a3d45c0e357d771";
                String apiUrl = "http://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + apiKey;

                try {
                    String jsonText = ""; // This will contain the JSON response obtained from the OpenWeather API call
                    URL url = new URL(apiUrl);
                    InputStream is = url.openStream();
                    BufferedReader bufferReader = new BufferedReader(new InputStreamReader(is));
                    String line;

                    while ((line = bufferReader.readLine()) != null) {
                        jsonText += line;
                    }
                    is.close();
                    bufferReader.close();

                    // Parse JSON response to extract weather information
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode jsonResponse = objectMapper.readTree(jsonText);

                    // Extract temperature
                    JsonNode main = jsonResponse.get("main");
                    double temperature = main.get("temp").asDouble() - 273.15;

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
                    String result = "Weather in " + location + ", " + country + ": "
                            + "Temperature: " + String.format("%.2f", temperature) + "°C, "
                            + "Humidity: " + humidity + "%, "
                            + "Wind Speed: " + windSpeed + " m/s, "
                            + "Pressure: " + pressure + " hPa";

                    // Store weather data in MongoDB
                    Document weatherDocument = new Document("location", location)
                            .append("country", country)
                            .append("temperature", temperature)
                            .append("humidity", humidity)
                            .append("wind_speed", windSpeed)
                            .append("pressure", pressure);

                    collection.insertOne(weatherDocument);

                    // You can add future temperature fetching logic here for multiple days

                    return result;

                } catch (IOException e) {
                    e.printStackTrace();
                    return "Error fetching weather information";
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error connecting to MongoDB Atlas");
        }
    }
}
