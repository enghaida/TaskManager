package taskmanager.impl;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import taskmanager.api.SchedulePlanner;
import taskmanager.api.TaskManager;
import taskmanager.api.TaskService;
import taskmanager.model.ScheduleRecommendation;
import taskmanager.model.Task;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Concrete implementation of the {@link TaskManager} facade.
 *
 * <p>Delegates storage operations to {@link TaskServiceImpl} and scheduling
 * analysis to {@link SchedulePlannerImpl}.
 *
 * <p>After each successful create, update, or delete operation, this class
 * fires the corresponding {@link ChangeListener} callback on all registered
 * observers. Callbacks are invoked from the reactive worker thread; Swing
 * listeners must dispatch to the EDT via
 * {@link javax.swing.SwingUtilities#invokeLater}.
 */
public class TaskManagerImpl implements TaskManager {

    private final TaskService    taskService = new TaskServiceImpl();
    private final SchedulePlanner planner    = new SchedulePlannerImpl();

    /** Thread-safe listener list (copy-on-write is safe for low-frequency mutations). */
    private final List<ChangeListener> listeners = new CopyOnWriteArrayList<>();

    // ------------------------------------------------------------------
    // TaskManager — listener registration
    // ------------------------------------------------------------------

    /**
     * {@inheritDoc}
     *
     * <p>Adds {@code listener} to the internal observer list.
     * {@code null} values are silently ignored.
     */
    @Override
    public void addChangeListener(ChangeListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    // ------------------------------------------------------------------
    // TaskManager — CRUD operations
    // ------------------------------------------------------------------

    /**
     * {@inheritDoc}
     *
     * <p><b>Side effect:</b> fires {@link ChangeListener#onTaskCreated(Task)}
     * on all registered listeners after the task is successfully stored.
     */
    @Override
    public Mono<Task> createTask(Task task) {
        return taskService.addTask(task)
            .doOnNext(t -> listeners.forEach(l -> l.onTaskCreated(t)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Flux<Task> getTasks() {
        return taskService.getAllTasks();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<ScheduleRecommendation> getRecommendation(Task task) {
        return planner.analyzeTask(task);
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Side effect:</b> fires {@link ChangeListener#onTaskDeleted(String)}
     * on all registered listeners after the task is successfully removed.
     */
    @Override
    public Mono<Void> deleteTask(String id) {
        return taskService.deleteTask(id)
            .doOnSuccess(v -> listeners.forEach(l -> l.onTaskDeleted(id)));
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Side effect:</b> fires {@link ChangeListener#onTaskUpdated(Task)}
     * on all registered listeners after the task is successfully updated.
     */
    @Override
    public Mono<Task> updateTask(Task task) {
        return taskService.updateTask(task)
            .doOnNext(t -> listeners.forEach(l -> l.onTaskUpdated(t)));
    }
}
