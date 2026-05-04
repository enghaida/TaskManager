package taskmanager.api;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import taskmanager.model.ScheduleRecommendation;
import taskmanager.model.Task;

/**
 * Facade interface that unifies task management and schedule planning.
 *
 * <p>Acts as the single entry point for the UI layer, delegating to
 * {@link TaskService} for storage operations and {@link SchedulePlanner} for
 * weather-based recommendations.
 *
 * <p>The nested {@link ChangeListener} interface lets external observers (e.g.,
 * the Swing UI) react to task lifecycle events without polling the store.
 */
public interface TaskManager {

    /**
     * Creates and stores a new task.
     *
     * <p><b>Side effect:</b> fires {@link ChangeListener#onTaskCreated(Task)} on
     * all registered listeners after the task is successfully stored.
     *
     * @param task the task to create; title must not be null or empty
     * @return a {@link Mono} emitting the created task, or an error signal with
     *         {@link taskmanager.exception.InvalidTaskException} if validation fails
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
     * @param task the task to evaluate; {@link Task#isOutdoor()} determines
     *             whether weather conditions are factored in
     * @return a {@link Mono} emitting a {@link ScheduleRecommendation}
     */
    Mono<ScheduleRecommendation> getRecommendation(Task task);

    /**
     * Deletes the task with the given ID.
     *
     * <p><b>Side effect:</b> fires {@link ChangeListener#onTaskDeleted(String)} on
     * all registered listeners after the task is successfully removed.
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
     * <p><b>Side effect:</b> fires {@link ChangeListener#onTaskUpdated(Task)} on
     * all registered listeners after the task is successfully updated.
     *
     * @param task the updated task; must have a valid ID and non-empty title
     * @return a {@link Mono} emitting the updated task, or an error signal with
     *         {@link taskmanager.exception.TaskNotFoundException} if the task
     *         does not exist, or {@link taskmanager.exception.InvalidTaskException}
     *         if the title is missing
     */
    Mono<Task> updateTask(Task task);

    /**
     * Returns tasks that match the given filter.
     *
     * <p>The default implementation delegates to {@link #getTasks()} and filters
     * inline. Implementations may override for storage-level optimisation.
     *
     * @param filter the predicate to apply; must not be null
     * @return a {@link Flux} emitting only matching tasks
     */
    default Flux<Task> getFilteredTasks(TaskService.TaskFilter filter) {
        return getTasks().filter(filter::test);
    }

    /**
     * Registers a {@link ChangeListener} to receive task lifecycle events.
     *
     * <p>The default implementation is a no-op; concrete classes override this
     * to maintain a list of registered observers.
     *
     * @param listener the listener to register; {@code null} values are ignored
     */
    default void addChangeListener(ChangeListener listener) {
        // no-op; override in concrete implementations
    }

    // -------------------------------------------------------------------------
    // Nested interface — observer for task lifecycle events
    // -------------------------------------------------------------------------

    /**
     * Observer for task lifecycle events emitted by a {@link TaskManager}.
     *
     * <p>This is a <b>nested interface</b>; it is defined inside {@link TaskManager}
     * because it has no meaning outside the context of observing a manager's
     * operations.
     *
     * <p><b>Threading note:</b> callbacks are invoked from reactive worker threads,
     * <em>not</em> from the Swing Event Dispatch Thread. Swing implementations must
     * wrap all component mutations in {@link javax.swing.SwingUtilities#invokeLater}:
     * <pre>{@code
     * manager.addChangeListener(new TaskManager.ChangeListener() {
     *     public void onTaskCreated(Task t) {
     *         SwingUtilities.invokeLater(() -> tableModel.addTask(t));
     *     }
     *     public void onTaskDeleted(String id) {
     *         SwingUtilities.invokeLater(() -> tableModel.removeTask(id));
     *     }
     *     public void onTaskUpdated(Task t) {
     *         SwingUtilities.invokeLater(() -> tableModel.updateTask(t));
     *     }
     * });
     * }</pre>
     */
    interface ChangeListener {

        /**
         * Called when a new task has been successfully created and stored.
         *
         * @param task the newly created task; never {@code null}
         */
        void onTaskCreated(Task task);

        /**
         * Called when a task has been successfully deleted from the store.
         *
         * @param id the ID of the deleted task; never {@code null}
         */
        void onTaskDeleted(String id);

        /**
         * Called when an existing task has been successfully updated.
         *
         * @param task the updated task reflecting the new state; never {@code null}
         */
        void onTaskUpdated(Task task);
    }
}
