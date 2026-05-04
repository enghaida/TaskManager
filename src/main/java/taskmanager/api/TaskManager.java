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

    /**
     * Deletes the task with the given ID.
     *
     * @param id the unique identifier of the task to delete
     * @return an empty {@link Mono} on success, or an error signal with
     *         {@link taskmanager.exception.TaskNotFoundException} if no task
     *         with that ID exists
     */
    Mono<Void> deleteTask(String id);

    /**
     * Updates an existing task.
     *
     * @param task the updated task; must have a valid ID and non-empty title
     * @return a {@link Mono} emitting the updated task, or an error signal with
     *         {@link taskmanager.exception.TaskNotFoundException} if the task
     *         does not exist, or {@link taskmanager.exception.InvalidTaskException}
     *         if the title is missing
     */
    Mono<Task> updateTask(Task task);
}
