package taskmanager.api;

import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import taskmanager.model.Task;

/**
 * Defines the core CRUD operations for managing tasks.
 * Implementations store and retrieve tasks reactively using
 * Project Reactor's {@link Mono} and {@link Flux}.
 */
public interface TaskService {

    /**
     * Adds a new task to the store.
     *
     * @param task the task to add; title must not be null or empty
     * @return a {@link Mono} emitting the saved task, or an error
     *         signal with {@link taskmanager.exception.InvalidTaskException}
     *         if the title is missing
     */
    Mono<Task> addTask(Task task);

    /**
     * Retrieves all stored tasks.
     *
     * @return a {@link Flux} emitting every task currently in the store
     */
    Flux<Task> getAllTasks();

    /**
     * Deletes the task with the given ID.
     *
     * @param id the unique identifier of the task to delete
     * @return an empty {@link Mono} on success, or an error signal with
     *         {@link taskmanager.exception.TaskNotFoundException} if no
     *         task with that ID exists
     */
    Mono<Void> deleteTask(String id);
}
