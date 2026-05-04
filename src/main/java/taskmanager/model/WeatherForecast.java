package taskmanager.model;

/**
 * Holds weather forecast data retrieved from the weather API.
 * Used by {@link taskmanager.impl.SchedulePlannerImpl} to evaluate
 * whether conditions are suitable for outdoor tasks.
 */
public class WeatherForecast {
    private double temperature;
    private String condition;

    /**
     * Constructs a WeatherForecast with the given temperature and condition.
     *
     * @param temperature current temperature in Celsius
     * @param condition   weather condition string (e.g., "Rain", "Clear")
     */
    public WeatherForecast(double temperature, String condition) {
        this.temperature = temperature;
        this.condition = condition;
    }

    /**
     * Returns the weather condition description.
     *
     * @return weather condition (e.g., "Rain" or "Clear")
     */
    public String getCondition() { return condition; }
}
