package taskmanager.service;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import taskmanager.annotation.WeatherSensitive;
import taskmanager.model.WeatherForecast;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

/**
 * Fetches live weather data from the OpenWeatherMap API for Makkah.
 * The HTTP call is executed on a bounded elastic thread pool to avoid
 * blocking the main reactive pipeline.
 */
public class WeatherService {
    private static final String API_KEY = "3464c76dadbcea045870c81c97112f01";

    /**
     * Retrieves the current weather forecast for Makkah.
     * <p>
     * Makes a blocking HTTP GET request wrapped in a {@link Mono#fromCallable}
     * and offloaded to {@link Schedulers#boundedElastic()} so the UI thread
     * is never blocked.
     *
     * @return a {@link Mono} emitting a {@link WeatherForecast} with the
     *         current condition; emits an error if the API call fails
     * @throws taskmanager.exception.WeatherAPIException if the HTTP request
     *         cannot be completed
     */
    @WeatherSensitive("Fetches live rain/clear condition from OpenWeatherMap for Makkah")
    public Mono<WeatherForecast> getWeather() {
        return Mono.fromCallable(() -> {
            URL url = new URL(
                "https://api.openweathermap.org/data/2.5/weather?q=Makkah&appid=" + API_KEY
            );
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            Scanner sc = new Scanner(conn.getInputStream());
            String response = sc.useDelimiter("\\A").next();
            sc.close();
            if (response.toLowerCase().contains("rain")) {
                return new WeatherForecast(18, "Rain");
            }
            return new WeatherForecast(30, "Clear");
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
