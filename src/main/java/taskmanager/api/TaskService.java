package taskmanager.api;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import taskmanager.model.Task;

/**
 * Defines the core CRUD operations for managing tasks.
 *
 * <p>Implementations store and retrieve tasks reactively using Project Reactor's
 * {@link Mono} and {@link Flux}. All operations are non-blocking; callers must
 * subscribe to trigger execution.
 *
 * <p>The nested {@link TaskFilter} functional interface lets callers retrieve
 * task subsets without coupling to implementation-specific query languages.
 */
public interface TaskService {

    /**
     * Adds a new task to the store.
     *
     * <p><b>Precondition:</b> {@code task.getTitle()} must not be null or blank.<br>
     * <b>Postcondition:</b> the task is persisted and returned via the emitted item.
     *
     * @param task the task to add; title must not be null or empty
     * @return a {@link Mono} emitting the saved task, or an error signal with
     *         {@link taskmanager.exception.InvalidTaskException} if the title is missing
     */
    Mono<Task> addTask(Task task);

    /**
     * Retrieves all stored tasks.
     *
     * @return a {@link Flux} emitting every task currently in the store;
     *         completes immediately if the store is empty
     */
    Flux<Task> getAllTasks();

    /**
     * Deletes the task with the given ID.
     *
     * <p><b>Precondition:</b> a task with {@code id} must exist.<br>
     * <b>Postcondition:</b> the task is removed from the store.
     *
     * @param id the unique identifier of the task to delete
     * @return an empty {@link Mono} on success, or an error signal with
     *         {@link taskmanager.exception.TaskNotFoundException} if no task
     *         with that ID exists
     */
    Mono<Void> deleteTask(String id);

    /**
     * Updates an existing task in the store.
     *
     * <p><b>Precondition:</b> a task with {@code task.getId()} must already exist
     * and {@code task.getTitle()} must not be null or blank.<br>
     * <b>Postcondition:</b> the stored task is replaced with the provided task.
     *
     * @param task the updated task; title must not be null or empty
     * @return a {@link Mono} emitting the updated task, or an error signal with
     *         {@link taskmanager.exception.TaskNotFoundException} if no task with
     *         that ID exists, or {@link taskmanager.exception.InvalidTaskException}
     *         if the title is missing
     */
    Mono<Task> updateTask(Task task);

    /**
     * Returns tasks that match the given filter predicate.
     *
     * <p>The default implementation filters the result of {@link #getAllTasks()}.
     * Concrete implementations may override this for more efficient storage-level
     * filtering.
     *
     * <p>Example — retrieve only outdoor tasks:
     * <pre>{@code
     * TaskService.TaskFilter outdoorOnly = Task::isOutdoor;
     * service.getFilteredTasks(outdoorOnly).subscribe(System.out::println);
     * }</pre>
     *
     * @param filter the predicate to apply; must not be null
     * @return a {@link Flux} emitting only tasks for which
     *         {@link TaskFilter#test(Task)} returns {@code true}
     */
    default Flux<Task> getFilteredTasks(TaskFilter filter) {
        return getAllTasks().filter(filter::test);
    }

    // -------------------------------------------------------------------------
    // Nested interface — groups filtering semantics inside the service API
    // -------------------------------------------------------------------------

    /**
     * A predicate used to select a subset of tasks from the store.
     *
     * <p>This is a <b>nested functional interface</b>. Placing it inside
     * {@link TaskService} keeps the filter concept tightly coupled to its natural
     * consumer and avoids polluting the top-level package with a single-purpose type.
     *
     * <p>Because it is annotated {@link FunctionalInterface}, it can be supplied
     * as a lambda or method reference wherever a filter is expected:
     * <pre>{@code
     * // Lambda
     * TaskService.TaskFilter byTitle = t -> t.getTitle().startsWith("A");
     *
     * // Method reference
     * TaskService.TaskFilter outdoorOnly = Task::isOutdoor;
     * }</pre>
     */
    @FunctionalInterface
    interface TaskFilter {

        /**
         * Tests whether the given task matches this filter's criteria.
         *
         * @param task the task to evaluate; never {@code null}
         * @return {@code true} if the task should be included in the result set
         */
        boolean test(Task task);
    }
}
