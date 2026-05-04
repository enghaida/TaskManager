package taskmanager.exception;

/**
 * Thrown when the weather service cannot be reached or returns an
 * unexpected response.
 *
 * <p>Causes include:
 * <ul>
 *   <li>Network connectivity failure.</li>
 *   <li>HTTP error status returned by the remote API (e.g., 401 Unauthorized,
 *       429 Too Many Requests).</li>
 *   <li>Malformed or empty JSON response that cannot be parsed.</li>
 * </ul>
 *
 * <p>The reactive pipeline in {@link taskmanager.impl.SchedulePlannerImpl}
 * uses {@code onErrorReturn} to fall back to a safe default recommendation
 * when this exception is emitted, ensuring the UI always receives a result
 * even when weather data is unavailable.
 *
 * <p>Example fallback:
 * <pre>{@code
 * weatherService.getWeather()
 *               .onErrorReturn(WeatherAPIException.class,
 *                              new WeatherForecast(25, "Unknown"))
 *               .map(w -> buildRecommendation(task, w));
 * }</pre>
 */
public class WeatherAPIException extends RuntimeException {

    /**
     * Constructs a {@code WeatherAPIException} with a descriptive message.
     *
     * @param msg describes the nature of the failure
     *            (e.g., {@code "HTTP 401 from weather API"} or
     *            {@code "Network error: connection timed out"})
     */
    public WeatherAPIException(String msg) {
        super(msg);
    }
}
