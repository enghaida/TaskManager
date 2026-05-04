package taskmanager.impl;

import reactor.core.publisher.Mono;
import taskmanager.annotation.WeatherSensitive;
import taskmanager.api.SchedulePlanner;
import taskmanager.model.*;
import taskmanager.service.WeatherService;

/**
 * Implementation of {@link SchedulePlanner} that uses live weather data
 * to generate scheduling recommendations.
 * Outdoor tasks are flagged with a warning when rain is detected.
 */
public class SchedulePlannerImpl implements SchedulePlanner {
    private final WeatherService weatherService = new WeatherService();

    /**
     * {@inheritDoc}
     * <p>
     * Fetches live weather from {@link WeatherService} and warns the user
     * if the task is outdoor and rain is expected. Falls back to a warning
     * recommendation if the weather API is unavailable.
     *
     * @param task the task to analyze
     * @return a {@link Mono} emitting a {@link ScheduleRecommendation}
     */
    @Override
    @WeatherSensitive("Returns a warning recommendation when rain is detected for outdoor tasks")
    public Mono<ScheduleRecommendation> analyzeTask(Task task) {
        return weatherService.getWeather()
            .map(weather -> {
                if (task.isOutdoor() && weather.getCondition().equalsIgnoreCase("Rain")) {
                    return new ScheduleRecommendation(task, "! Avoid rain", true);
                }
                return new ScheduleRecommendation(task, "Good time", false);
            })
            .onErrorReturn(new ScheduleRecommendation(task, "Weather error", true));
    }
}
