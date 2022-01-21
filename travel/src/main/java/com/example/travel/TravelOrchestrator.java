package com.example.travel;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/")
    public String test() {
        return "Hello, World!";
    }

    @GetMapping("/id")
    public String getWeatherTest() throws InterruptedException, ExecutionException, TimeoutException {
        HttpRequest req = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://www.randomnumberapi.com/api/v1.0/random?min=5&max=500"))
                .setHeader("User-Agent", "Java HttpClient")
                .build();

        // Async task
        CompletableFuture<HttpResponse<String>> res =
                httpClient.sendAsync(req, HttpResponse.BodyHandlers.ofString());

        return res.thenApply(HttpResponse::body).get(5, TimeUnit.SECONDS);
    }

}
