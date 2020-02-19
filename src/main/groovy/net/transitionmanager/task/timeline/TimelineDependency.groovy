package net.transitionmanager.task.timeline
/**
 * DTO class for {@code TaskDependency} results in database. It's a preview with an small amount of fields used in CPA calculation.
 * @see TimelineService#getTaskDependencies(net.transitionmanager.project.MoveEvent)
 */
class TimelineDependency {

    Long taskDependencyId
    Long successorId
    Integer successorTaskNumber
    Long predecessorId
    Integer predecessorTaskNumber

    static TimelineDependency fromResultSet(Map<String, ?> row) {
        return new TimelineDependency(
                taskDependencyId: row.task_dependency_id,
                successorId: row.successor_id,
                successorTaskNumber: row.successor_task_number,
                predecessorId: row.predecessor_id,
                predecessorTaskNumber: row.predecessor_task_number
        )
    }
}
