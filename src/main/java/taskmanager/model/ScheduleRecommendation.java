package taskmanager.model;

/**
 * Represents a scheduling recommendation for a given task.
 * Contains a human-readable message and a flag indicating
 * whether the recommendation is a warning (e.g., bad weather).
 */
public class ScheduleRecommendation {
    private Task task;
    private String message;
    private boolean warning;

    /**
     * Constructs a ScheduleRecommendation.
     *
     * @param task    the task this recommendation applies to
     * @param message a human-readable recommendation message shown in the UI
     * @param warning {@code true} if the recommendation is a warning
     */
    public ScheduleRecommendation(Task task, String message, boolean warning) {
        this.task = task;
        this.message = message;
        this.warning = warning;
    }

    /**
     * Returns the recommendation message.
     *
     * @return the message to display to the user
     */
    public String getMessage() { return message; }
}
