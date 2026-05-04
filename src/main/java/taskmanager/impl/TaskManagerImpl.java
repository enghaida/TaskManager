package taskmanager.impl;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import taskmanager.api.*;
import taskmanager.model.*;

/**
 * Concrete implementation of the {@link TaskManager} facade.
 * Delegates storage operations to {@link TaskServiceImpl} and
 * scheduling analysis to {@link SchedulePlannerImpl}.
 */
public class TaskManagerImpl implements TaskManager {
    private final TaskService taskService = new TaskServiceImpl();
    private final SchedulePlanner planner = new SchedulePlannerImpl();

    /**
     * {@inheritDoc}
     */
    @Override
    public Mono<Task> createTask(Task task) {
        return taskService.addTask(task);
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
     * Deletes a task by its unique identifier.
     *
     * @param id the ID of the task to delete
     * @return an empty {@link Mono} on success, or an error signal with
     *         {@link taskmanager.exception.TaskNotFoundException} if not found
     */
    public Mono<Void> deleteTask(String id) {
        return taskService.deleteTask(id);
    }
}
