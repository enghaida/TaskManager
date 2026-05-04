package taskmanager.service;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import taskmanager.annotation.WeatherSensitive;
import taskmanager.exception.WeatherAPIException;
import taskmanager.model.WeatherForecast;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Fetches live weather data from the OpenWeatherMap "Current Weather" API.
 *
 * <p>The blocking HTTP call is wrapped in {@link Mono#fromCallable} and
 * offloaded to {@link Schedulers#boundedElastic()} so the reactive pipeline
 * and the Swing Event Dispatch Thread are never blocked.
 *
 * <p>Responses are parsed with lightweight regex — no external JSON library
 * is required. Results are cached for {@value #CACHE_TTL_MS} ms to avoid
 * redundant network requests within the same session.
 *
 * <p>API endpoint:
 * <pre>
 *   GET https://api.openweathermap.org/data/2.5/weather?q={city}&amp;appid={key}&amp;units=metric
 * </pre>
 */
public class WeatherService {

    private static final String CITY         = "Mecca";
    private static final String URL_TEMPLATE =
        "https://api.openweathermap.org/data/2.5/weather?q=%s&appid=%s&units=metric";

    /** Five-minute cache TTL to avoid excessive API calls. */
    private static final long CACHE_TTL_MS = 5L * 60 * 1_000;

    private volatile WeatherForecast cachedForecast;
    private volatile long            cacheTimestamp = 0L;

    /**
     * Retrieves the current weather forecast for Makkah.
     *
     * <p>Returns cached data if the previous successful call was within
     * {@value #CACHE_TTL_MS} ms; otherwise performs a live HTTP GET on a
     * bounded-elastic background thread.
     *
     * <p><b>Side effect:</b> updates the in-memory cache on a successful call.
     *
     * @return a {@link Mono} emitting a {@link WeatherForecast} with condition,
     *         temperature (°C), humidity (%) and city name; emits a
     *         {@link WeatherAPIException} error signal if the request fails
     */
    @WeatherSensitive("Live temperature, condition and humidity from OpenWeatherMap for Makkah")
    public Mono<WeatherForecast> getWeather() {
        return Mono.fromCallable(this::fetchOrUseCached)
                   .subscribeOn(Schedulers.boundedElastic());
    }

    // ------------------------------------------------------------------
    // Private helpers
    // ------------------------------------------------------------------
    private static String requireApiKey() {
        String key = System.getenv("OPENWEATHER_API_KEY");
        if (key == null || key.isBlank()) {
            throw new WeatherAPIException("Missing OpenWeather API key");
        }
        return key;
    }

    private WeatherForecast fetchOrUseCached() {
        long now = System.currentTimeMillis();
        if (cachedForecast != null && (now - cacheTimestamp) < CACHE_TTL_MS) {
            return cachedForecast;
        }
        WeatherForecast fresh = callApi();
        cachedForecast = fresh;
        cacheTimestamp = now;
        return fresh;
    }

    private WeatherForecast callApi() {
        try {
            String endpoint = String.format(URL_TEMPLATE, CITY, requireApiKey());
            HttpURLConnection conn =
                (HttpURLConnection) new URL(endpoint).openConnection();
            conn.setConnectTimeout(5_000);
            conn.setReadTimeout(10_000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            int status = conn.getResponseCode();
            if (status != 200) {
                throw new WeatherAPIException("HTTP " + status + " from weather API");
            }

            String json;
            try (Scanner sc = new Scanner(conn.getInputStream())) {
                json = sc.useDelimiter("\\A").next();
            }
            return parseResponse(json);

        } catch (IOException e) {
            throw new WeatherAPIException("Network error: " + e.getMessage());
        }
    }

    /**
     * Parses the OpenWeatherMap JSON response body using regex.
     *
     * <p>Extracted fields:
     * <ul>
     *   <li>{@code weather[0].main} — primary condition string (Rain, Clear, …)</li>
     *   <li>{@code main.temp}       — temperature in Celsius ({@code units=metric})</li>
     *   <li>{@code main.humidity}   — relative humidity %</li>
     *   <li>{@code name}            — city name as returned by the API</li>
     * </ul>
     */
    private WeatherForecast parseResponse(String json) {
        // weather[0].main — first "main":"…" inside the weather array
        String condition = extract(json,
            "\"weather\":\\[\\{[^}]*\"main\":\"([^\"]+)\"", 1, "Unknown");

        // main.temp — first numeric value after "temp":
        double temp = parseDouble(extract(json, "\"temp\":(-?[\\d.]+)", 1, "25.0"), 25.0);

        // main.humidity
        int hum = parseInt(extract(json, "\"humidity\":(\\d+)", 1, "50"), 50);

        // city name
        String city = extract(json, "\"name\":\"([^\"]+)\"", 1, CITY);

        return new WeatherForecast(temp, condition, hum, city);
    }

    private String extract(String text, String pattern, int group, String fallback) {
        Matcher m = Pattern.compile(pattern).matcher(text);
        return m.find() ? m.group(group) : fallback;
    }

    private double parseDouble(String s, double fallback) {
        try   { return Double.parseDouble(s); }
        catch (NumberFormatException e) { return fallback; }
    }

    private int parseInt(String s, int fallback) {
        try   { return Integer.parseInt(s); }
        catch (NumberFormatException e) { return fallback; }
    }
}
