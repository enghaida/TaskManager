package taskmanager.impl;

import reactor.core.publisher.Mono;
import taskmanager.annotation.WeatherSensitive;
import taskmanager.api.SchedulePlanner;
import taskmanager.model.ScheduleRecommendation;
import taskmanager.model.Task;
import taskmanager.model.WeatherForecast;
import taskmanager.service.WeatherService;

/**
 * Implementation of {@link SchedulePlanner} that uses live weather data
 * from {@link WeatherService} to generate scheduling recommendations.
 *
 * <p>Analysis rules for <em>outdoor</em> tasks (applied in order):
 * <ol>
 *   <li>Rain / Drizzle / Thunderstorm / Snow &rarr; <b>Warning</b> (reschedule).</li>
 *   <li>Temperature &ge; {@value #EXTREME_HEAT_C} °C &rarr; <b>Warning</b> (extreme heat).</li>
 *   <li>Temperature &ge; {@value #HIGH_HEAT_C} °C &rarr; <b>Caution</b> (high heat).</li>
 *   <li>Otherwise &rarr; <b>Good to go</b>.</li>
 * </ol>
 *
 * <p>Indoor tasks always receive a neutral "no weather impact" recommendation
 * without making an API call, keeping unnecessary network traffic at zero.
 *
 * <p>If the weather API is unreachable, {@code onErrorReturn} substitutes a safe
 * fallback so the reactive chain never propagates an unhandled error.
 */
public class SchedulePlannerImpl implements SchedulePlanner {

    /** Temperature threshold for an extreme-heat warning. */
    private static final double EXTREME_HEAT_C = 42.0;

    /** Temperature threshold for a high-heat caution. */
    private static final double HIGH_HEAT_C    = 35.0;

    private final WeatherService weatherService = new WeatherService();

    /**
     * {@inheritDoc}
     *
     * <p>Fetches live weather and maps it to a {@link ScheduleRecommendation}.
     * Falls back to a safe warning if the weather API is unavailable.
     *
     * @param task the task to analyze; {@link Task#isOutdoor()} controls
     *             whether weather data is consulted
     * @return a {@link Mono} emitting a {@link ScheduleRecommendation}
     */
    @Override
    @WeatherSensitive("Checks rain, extreme heat, and high temperature for outdoor tasks")
    public Mono<ScheduleRecommendation> analyzeTask(Task task) {
        if (!task.isOutdoor()) {
            return Mono.just(
                new ScheduleRecommendation(task, "Indoor task — no weather impact.", false));
        }

        return weatherService.getWeather()
            .map(w -> buildRecommendation(task, w))
            .onErrorReturn(
                new ScheduleRecommendation(task,
                    "Weather data unavailable. Proceed with caution for outdoor activities.",
                    true));
    }

    // ------------------------------------------------------------------
    // Private helpers
    // ------------------------------------------------------------------

    /**
     * Applies the ordering rules to produce the correct recommendation.
     *
     * @param task the outdoor task being analyzed
     * @param w    the live weather snapshot from the API
     * @return the appropriate {@link ScheduleRecommendation}
     */
    private ScheduleRecommendation buildRecommendation(Task task, WeatherForecast w) {
        String cond = w.getCondition();
        double temp = w.getTemperature();
        int    hum  = w.getHumidity();
        String city = w.getCityName();

        String context = String.format("(%s, %.1f°C, %d%% humidity in %s)",
            cond, temp, hum, city);

        boolean rainy = cond.matches("(?i)Rain|Drizzle|Thunderstorm|Snow|Sleet");

        if (rainy) {
            return new ScheduleRecommendation(task,
                "Warning: " + cond + " expected. Consider rescheduling. " + context,
                true);
        }
        if (temp >= EXTREME_HEAT_C) {
            return new ScheduleRecommendation(task,
                String.format(
                    "Warning: Extreme heat (%.1f°C). Outdoor activity not recommended. %s",
                    temp, context),
                true);
        }
        if (temp >= HIGH_HEAT_C) {
            return new ScheduleRecommendation(task,
                String.format(
                    "Caution: High temperature (%.1f°C). Plan outdoor activities carefully. %s",
                    temp, context),
                false);
        }
        return new ScheduleRecommendation(task,
            "Good time for your outdoor activity! " + context, false);
    }
}
