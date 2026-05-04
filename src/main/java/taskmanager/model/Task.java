package taskmanager.model;

import java.time.LocalDateTime;

/**
 * Represents a user-defined task with scheduling metadata.
 * A task can be marked as outdoor, which makes it subject to
 * weather-based scheduling recommendations.
 */
public class Task {
    private String id;
    private String title;
    private LocalDateTime dateTime;
    private boolean outdoor;

    /**
     * Constructs a new Task.
     *
     * @param id       unique identifier for the task
     * @param title    display title of the task; must not be null or empty
     * @param dateTime the scheduled date and time for the task
     * @param outdoor  {@code true} if the task takes place outdoors
     */
    public Task(String id, String title, LocalDateTime dateTime, boolean outdoor) {
        this.id = id;
        this.title = title;
        this.dateTime = dateTime;
        this.outdoor = outdoor;
    }

    /**
     * Returns the unique identifier of this task.
     *
     * @return the task ID
     */
    public String getId() { return id; }

    /**
     * Returns the title of this task.
     *
     * @return the task title
     */
    public String getTitle() { return title; }

    /**
     * Returns the scheduled date and time of this task.
     *
     * @return the task's {@link LocalDateTime}
     */
    public LocalDateTime getDateTime() { return dateTime; }

    /**
     * Returns whether this task is an outdoor activity.
     *
     * @return {@code true} if the task is outdoor
     */
    public boolean isOutdoor() { return outdoor; }
}
