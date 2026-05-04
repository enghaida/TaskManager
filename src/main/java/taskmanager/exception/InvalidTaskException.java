package taskmanager.exception;

/**
 * Thrown when a task fails business-rule validation before it is persisted.
 *
 * <p>Common precondition violations that trigger this exception:
 * <ul>
 *   <li>The task title is {@code null} or blank.</li>
 *   <li>A required field is missing or out of range.</li>
 * </ul>
 *
 * <p>The exception message always describes the specific rule that was violated
 * so the UI layer can display an actionable error to the user without inspecting
 * the exception type.
 *
 * <p>This is an unchecked exception. It is emitted as a reactive error signal
 * (e.g., {@code Mono.error(new InvalidTaskException(...))}) so callers in the
 * reactive pipeline can recover via {@code onErrorReturn} or {@code onErrorResume}.
 */
public class InvalidTaskException extends RuntimeException {

    /**
     * Constructs an {@code InvalidTaskException} with a descriptive message.
     *
     * @param msg describes the validation rule that was violated
     *            (e.g., {@code "Task title must not be empty"})
     */
    public InvalidTaskException(String msg) {
        super(msg);
    }
}
