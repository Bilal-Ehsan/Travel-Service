package com.example.travel;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api/v1")
public class TravelOrchestrator {

    public static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @GetMapping("/id")
    public JSONObject getId() throws InterruptedException, ExecutionException, TimeoutException {
        HttpRequest req = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://www.randomnumberapi.com/api/v1.0/random?min=5&max=500"))
                .setHeader("User-Agent", "Java HttpClient")
                .build();

        // Async task
        CompletableFuture<HttpResponse<String>> res =
                httpClient.sendAsync(req, HttpResponse.BodyHandlers.ofString());

        String result = res.thenApply(HttpResponse::body).get(5, TimeUnit.SECONDS);
        String id = result.replace("]", "").replace("[", "");

        JSONObject json = new JSONObject();
        json.put("id", id);

        return json;
    }

    @GetMapping("/forecast")
    public JSONObject getWeather(@RequestParam(name = "location") String location,
                                 @RequestParam(name = "date") String date)
            throws InterruptedException, ExecutionException, TimeoutException, ParseException {
        String weatherUri = "https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/timeline/" +
                location + "?unitGroup=uk&include=days&key=6PT9RBX559A3YY6H6EK6XARE9&contentType=json";

        HttpRequest req = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(weatherUri))
                .setHeader("User-Agent", "Java HttpClient")
                .build();

        CompletableFuture<HttpResponse<String>> res =
                httpClient.sendAsync(req, HttpResponse.BodyHandlers.ofString());

        String result = res.thenApply(HttpResponse::body).get(5, TimeUnit.SECONDS);

        // Parse wanted data from original response
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = (JSONObject) parser.parse(result);

        JSONArray days = (JSONArray) jsonObject.get("days");
        JSONObject json = new JSONObject(); // Will contain the response payload

        for (Object day : days) {
            JSONObject data = (JSONObject) day;
            String locationDate = (String) data.get("datetime");

            if (locationDate.equals(date)) {
                String description = (String) data.get("description");
                Double temp = (Double) data.get("temp");
                Double humidity = (Double) data.get("humidity");
                Double precipitation = (Double) data.get("precip");
                Double windSpeed = (Double) data.get("windspeed");

                // Create new JSONObject from parsed data
                json.put("datetime", locationDate);
                json.put("description", description);
                json.put("temp", temp);
                json.put("humidity", humidity);
                json.put("precip", precipitation);
                json.put("windspeed", windSpeed);
            }
        }

        return json;
    }

}
