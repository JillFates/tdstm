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

    TimelineDependency(Object values) {
        this.taskDependencyId = values[0]
        this.successorId = values[1]
        this.successorTaskNumber = values[2]
        this.predecessorId = values[3]
        this.predecessorTaskNumber = values[4]
    }
}
