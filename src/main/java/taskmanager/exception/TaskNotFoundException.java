package taskmanager.exception;

/**
 * Thrown when an operation references a task ID that does not exist
 * in the task store.
 *
 * <p><b>Postcondition</b>: the task store is left unchanged — no partial
 * mutations occur when this exception is raised.
 *
 * <p>This is an unchecked exception. Reactive callers should handle it
 * in their {@code onError} callback or via operators such as
 * {@code Mono.onErrorResume} to provide meaningful feedback to the user.
 *
 * <p>Example:
 * <pre>{@code
 * manager.deleteTask(id)
 *        .onErrorResume(TaskNotFoundException.class,
 *                       ex -> Mono.error(new RuntimeException("No task: " + id)));
 * }</pre>
 */
public class TaskNotFoundException extends RuntimeException {

    /**
     * Constructs a {@code TaskNotFoundException} with a descriptive message.
     *
     * @param msg explains which task was not found; should include the task ID
     *            so the caller can log or display a useful error
     */
    public TaskNotFoundException(String msg) {
        super(msg);
    }
}
