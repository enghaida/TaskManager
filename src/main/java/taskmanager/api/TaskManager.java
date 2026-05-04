package taskmanager.api;

import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import taskmanager.model.*;

/**
 * Facade interface that unifies task management and schedule planning.
 * Acts as the single entry point for the UI layer, delegating to
 * {@link TaskService} for storage and {@link SchedulePlanner} for
 * weather-based recommendations.
 */
public interface TaskManager {

    /**
     * Creates and stores a new task.
     *
     * @param task the task to create; title must not be null or empty
     * @return a {@link Mono} emitting the created task
     */
    Mono<Task> createTask(Task task);

    /**
     * Retrieves all existing tasks.
     *
     * @return a {@link Flux} emitting all stored tasks
     */
    Flux<Task> getTasks();

    /**
     * Returns a weather-based scheduling recommendation for the given task.
     *
     * @param task the task to evaluate
     * @return a {@link Mono} emitting a {@link ScheduleRecommendation}
     */
    Mono<ScheduleRecommendation> getRecommendation(Task task);
}
