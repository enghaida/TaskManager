package taskmanager.exception;

public class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException(String msg) { super(msg); }
}
