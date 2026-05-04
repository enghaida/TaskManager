package taskmanager.model;

/**
 * Immutable scheduling recommendation produced by
 * {@link taskmanager.impl.SchedulePlannerImpl}.
 *
 * <p>Implemented as a <b>Java Record</b> to enforce immutability — once a
 * recommendation is created it can be safely passed between reactive streams,
 * cached, or displayed in the UI without defensive copying.
 *
 * @param task    the task this recommendation applies to
 * @param message a human-readable recommendation message shown in the UI
 * @param warning {@code true} if the recommendation signals a potential problem
 *                (bad weather, extreme heat, etc.)
 */
public record ScheduleRecommendation(
        Task    task,
        String  message,
        boolean warning) {

    /**
     * Compact constructor — ensures the message is never {@code null}.
     */
    public ScheduleRecommendation {
        message = (message == null) ? "" : message;
    }

    // ------------------------------------------------------------------
    // Backward-compatible getter aliases
    // ------------------------------------------------------------------

    /** @return the recommendation message to display */
    public String  getMessage()  { return message(); }

    /** @return the task this recommendation was generated for */
    public Task    getTask()     { return task(); }

    /**
     * Returns whether this recommendation indicates a scheduling concern.
     *
     * <p>Records generate an accessor named {@code warning()} for boolean
     * components. This alias follows the conventional {@code is}-prefix.
     *
     * @return {@code true} if the recommendation is a warning
     */
    public boolean isWarning()   { return warning(); }
}
