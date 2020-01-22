package net.transitionmanager.task.timeline

/**
 * DTO class for {@code TaskDependency} results in database. It's a preview with an small amount of fields used in CPA calculation.
 * @see TimelineService#getTaskDependencies(java.util.List)
 */
class TimelineDependency {

    Long taskDependencyId
    Long successorId
    Integer successorTaskNumber
    Long predecessorId
    Integer predecessorTaskNumber

    static TimelineDependency fromResultSet(Object values) {
        return new TimelineDependency(
                taskDependencyId: values[0],
                successorId: values[1],
                successorTaskNumber: values[2],
                predecessorId: values[3],
                predecessorTaskNumber: values[4]
        )
    }
}
