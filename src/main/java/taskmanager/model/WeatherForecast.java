package taskmanager.model;

/**
 * Immutable snapshot of weather conditions retrieved from the OpenWeatherMap API.
 *
 * <p>Implemented as a <b>Java Record</b> so all weather data is guaranteed
 * immutable and safe to cache and share across threads.
 *
 * @param temperature temperature in Celsius
 * @param condition   primary weather condition string from the API
 *                    (e.g., {@code "Rain"}, {@code "Clear"}, {@code "Thunderstorm"})
 * @param humidity    relative humidity as a percentage (0–100)
 * @param cityName    name of the city the forecast applies to
 */
public record WeatherForecast(
        double temperature,
        String condition,
        int    humidity,
        String cityName) {

    /**
     * Compact constructor — ensures neither string component is {@code null}.
     */
    public WeatherForecast {
        condition = (condition == null) ? "Unknown" : condition;
        cityName  = (cityName  == null) ? "Unknown" : cityName;
    }

    /**
     * Convenience constructor for code that only has temperature and condition
     * (e.g., unit tests or legacy callers).
     *
     * @param temperature temperature in Celsius
     * @param condition   weather condition string
     */
    public WeatherForecast(double temperature, String condition) {
        this(temperature, condition, 0, "Unknown");
    }

    // ------------------------------------------------------------------
    // Backward-compatible getter aliases
    // ------------------------------------------------------------------

    /** @return temperature in Celsius */
    public double getTemperature() { return temperature(); }

    /** @return primary weather condition (e.g., {@code "Rain"} or {@code "Clear"}) */
    public String getCondition()   { return condition(); }

    /** @return relative humidity percentage (0–100) */
    public int    getHumidity()    { return humidity(); }

    /** @return name of the forecast city */
    public String getCityName()    { return cityName(); }
}
