package org.skarvate;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Scanner;

// retrieve weather data from API - this backend logic will fetch the latest number
// data from the external API and return it. The GUI will
// display this data to the user
public class WeatherApp {
    // fetch weather data
    public static JSONObject getWeatherData(String locationName) {
        // get location coordinates using the geolocation API
        JSONArray locationData = getLocationData(locationName);

        // extract latitude and longitude data
        JSONObject location = (JSONObject) Objects.requireNonNull(locationData).getFirst();
        double latitude = (Double) location.get("latitude");
        double longitude = (Double) location.get("longitude");

        // build API request URL with location coordinates
        String urlString = "https://api.open-meteo.com/v1/forecast?" +
                "latitude=" + latitude + "&longitude=" + longitude +
                "&hourly=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m&timezone=America%2FSao_Paulo";

        try {
            // call api and get response
            HttpURLConnection conn = fetchApiResponse(urlString);

            //check for response status
            // 200 - means that the connection was a success
            if(Objects.requireNonNull(conn).getResponseCode() != 200) {
                System.out.println("Error: Could not connect to API");
                return null;
            }

            // store resulting json data
            StringBuilder resultJson = new StringBuilder();
            Scanner scanner = new Scanner(conn.getInputStream());
            while(scanner.hasNextLine()) {
                // read and store into the string builder
                resultJson.append(scanner.nextLine());
            }

            // close scanner
            scanner.close();

            // close url connection
            conn.disconnect();

            // parse through our data
            JSONParser parser = new JSONParser();
            JSONObject resultJsonObject = (JSONObject) parser.parse(String.valueOf(resultJson));

            // retrieve hourly data
            JSONObject hourly = (JSONObject) resultJsonObject.get("hourly");

            // we wont to get the current hour's data
            // so we need to get the index of our current hour
            JSONArray time = (JSONArray) hourly.get("time");
            int index = findIndexOfCurrentTime(time);

            // get temperature
            JSONArray temperatureData = (JSONArray) hourly.get("temperature_2m");
            double temperature = (Double) temperatureData.get(index);

            // get weather code
            JSONArray weatherCode = (JSONArray) hourly.get("weather_code");
            String weatherCondition = convertWeatherCode((long) weatherCode.get(index));

            // get humidity
            JSONArray relativeHumidity = (JSONArray) hourly.get("relative_humidity_2m");
            long humidity = (Long) relativeHumidity.get(index);

            // get windspeed
            JSONArray windSpeedData = (JSONArray) hourly.get("wind_speed_10m");
            double windSpeed = (Double) windSpeedData.get(index);

            // build the weather json data object that we are going to acess in our frontend
            JSONObject weatherData = new JSONObject();
            weatherData.put("temperature", temperature);
            weatherData.put("weather_condition", weatherCondition);
            weatherData.put("humidity", humidity);
            weatherData.put("windspeed", windSpeed);

            return weatherData;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static JSONArray getLocationData(String locationName) {
        // replace any whitespace in location name to + to adhere to API's request format
        locationName = locationName.replaceAll(" ", "+");

        // build API url with location parameter
        String urlString = "https://geocoding-api.open-meteo.com/v1/search?name=" +
                locationName + "&count=10&language=en&format=json";

        try {
            // call api and get a response
            HttpURLConnection conn = fetchApiResponse(urlString);

            // check response status
            // 200 means successful  connection
            if(conn.getResponseCode() != 200) {
                System.out.println("Error: Could not connect to API");
            } else {
                // store the API results
                StringBuilder resultJson = new StringBuilder();
                Scanner scanner = new Scanner(conn.getInputStream());

                // read and store the resulting json data into our string builder
                while (scanner.hasNextLine()) {
                    resultJson.append(scanner.nextLine());
                }

                // close scanner
                scanner.close();

                // close url connection
                conn.disconnect();

                // parse the JSON string into a JSON obj
                JSONParser parser = new JSONParser();
                JSONObject resultJsonObj = (JSONObject) parser.parse(String.valueOf(resultJson));

                // get the list of location data the API generated from the location name
                JSONArray locationData = (JSONArray) resultJsonObj.get("results");
                return locationData;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // couldn't find location
        return null;
    }

    private static HttpURLConnection fetchApiResponse (String urlString){
        try {
            // attempt to create connection
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // set request method to get
            conn.setRequestMethod("GET");

            // connect to our API
            conn.connect();
            return conn;
        } catch (IOException e) {
            e.printStackTrace();
        }

        // could not make connection
        return null;
    }

    private static int findIndexOfCurrentTime(JSONArray timeList) {
        String currentTime = getCurrentTime();


        // iterate through the time list and see which one matches our current time
        for(int i = 0; i < timeList.size(); i++) {
            String time = (String) timeList.get(i);
            if (time.equalsIgnoreCase(currentTime)) {
                // return index
                return i;
            }
        }
        return 0;
    }

    public static String getCurrentTime() {
        // get current date and time
        LocalDateTime currentDataTime = LocalDateTime.now();

        // format date to be 2024-12-18T00:00 (this is how is read in the API)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH':00'");

        // format and print the current date and time
        String formattedDateTime = currentDataTime.format(formatter);

        return formattedDateTime;
    }

    // convert the weather code to
    private static String convertWeatherCode(long weatherCode) {
        String weatherCondition = "";
        if(weatherCode == 0L) {
            weatherCondition = "Clear";
        } else if(weatherCode <= 3L && weatherCode > 0L) {
            weatherCondition = "Cloudy";
        } else if((weatherCode >= 51L && weatherCode <= 67L) || (weatherCode >= 80L && weatherCode <= 99L)) {
            // rain

            weatherCondition = "Rain";
        } else if (weatherCode >= 71L && weatherCode <= 77L) {
            weatherCondition = "Snow";
        }

        return weatherCondition;
    }
}
