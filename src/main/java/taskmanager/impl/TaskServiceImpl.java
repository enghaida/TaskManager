package taskmanager.impl;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import taskmanager.api.TaskService;
import taskmanager.model.Task;
import taskmanager.exception.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * In-memory implementation of {@link TaskService}.
 * Tasks are stored in a {@link ConcurrentHashMap} to support
 * thread-safe access from reactive streams.
 */
public class TaskServiceImpl implements TaskService {
    private final Map<String, Task> storage = new ConcurrentHashMap<>();

    /**
     * {@inheritDoc}
     *
     * @throws InvalidTaskException if the task title is null or empty
     */
    @Override
    public Mono<Task> addTask(Task task) {
        if (task.getTitle() == null || task.getTitle().isEmpty()) {
            return Mono.error(new InvalidTaskException("Title required"));
        }
        storage.put(task.getId(), task);
        return Mono.just(task);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Flux<Task> getAllTasks() {
        return Flux.fromIterable(storage.values());
    }

    /**
     * {@inheritDoc}
     *
     * @throws TaskNotFoundException if no task with the given ID exists
     */
    @Override
    public Mono<Void> deleteTask(String id) {
        if (!storage.containsKey(id)) {
            return Mono.error(new TaskNotFoundException("Not found"));
        }
        storage.remove(id);
        return Mono.empty();
    }
}
