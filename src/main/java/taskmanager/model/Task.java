package taskmanager.model;

import java.time.LocalDateTime;

/**
 * Immutable data carrier representing a user-defined task.
 *
 * <p>Implemented as a <b>Java Record</b> to eliminate boilerplate
 * (canonical constructor, {@code equals}, {@code hashCode}, {@code toString})
 * while keeping the data fully immutable after construction.
 *
 * <p>A task can be marked as outdoor ({@link #outdoor()}), which makes it
 * subject to weather-aware scheduling recommendations from
 * {@link taskmanager.api.SchedulePlanner}.
 *
 * @param id          unique identifier (UUID string)
 * @param title       display title; may be blank — validated at the service layer
 * @param description optional additional detail about the task (never null after construction)
 * @param dateTime    scheduled date and time for the activity
 * @param outdoor     {@code true} if the task takes place outdoors
 */
public record Task(
        String id,
        String title,
        String description,
        LocalDateTime dateTime,
        boolean outdoor) {

    /**
     * Compact constructor — normalises {@code null} inputs so every
     * component is safe to use without null-checks downstream.
     */
    public Task {
        title       = (title == null)       ? "" : title.trim();
        description = (description == null) ? "" : description.trim();
    }

    /**
     * Convenience constructor for callers that do not supply a description.
     *
     * @param id       unique identifier
     * @param title    task title
     * @param dateTime scheduled date/time
     * @param outdoor  {@code true} for outdoor tasks
     */
    public Task(String id, String title, LocalDateTime dateTime, boolean outdoor) {
        this(id, title, "", dateTime, outdoor);
    }

    // ------------------------------------------------------------------
    // Backward-compatible getter aliases
    // Records expose components as outdoor(), id() etc.
    // These aliases preserve the conventional get/is-prefix API.
    // ------------------------------------------------------------------

    /** @return the unique task identifier */
    public String getId() { return id(); }

    /** @return the task display title */
    public String getTitle() { return title(); }

    /** @return optional task description, never {@code null} */
    public String getDescription() { return description(); }

    /** @return the scheduled date and time */
    public LocalDateTime getDateTime() { return dateTime(); }

    /**
     * Returns whether this task is an outdoor activity.
     *
     * <p>Records generate an accessor named {@code outdoor()} for boolean
     * components (no {@code is}-prefix). This alias satisfies callers that
     * expect the conventional naming.
     *
     * @return {@code true} if the task is outdoors
     */
    public boolean isOutdoor() { return outdoor(); }
}
