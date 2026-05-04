package taskmanager.api;

import reactor.core.publisher.Mono;
import taskmanager.model.*;

/**
 * Analyzes tasks and produces weather-aware scheduling recommendations.
 * Implementations consult a weather source to determine whether
 * outdoor tasks should be performed at their scheduled time.
 */
public interface SchedulePlanner {

    /**
     * Analyzes a task and returns a scheduling recommendation
     * based on current weather conditions.
     *
     * @param task the task to analyze; outdoor flag is used to decide
     *             whether weather conditions are relevant
     * @return a {@link Mono} emitting a {@link ScheduleRecommendation}
     *         with either an approval or a weather-based warning
     */
    Mono<ScheduleRecommendation> analyzeTask(Task task);
}
