package net.transitionmanager.task.timeline

import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import net.transitionmanager.project.MoveEvent
import net.transitionmanager.service.ServiceMethods
import net.transitionmanager.task.Task
import net.transitionmanager.task.TaskDependency
import org.springframework.jdbc.core.BatchPreparedStatementSetter
import org.springframework.jdbc.core.JdbcTemplate

import java.sql.PreparedStatement
import java.sql.SQLException

@Transactional
@CompileStatic
class TimelineService implements ServiceMethods {

    /**
     * Instance of {@code JdbcTemplate} used to save CPA results in Database
     * with a better performance.
     */
    JdbcTemplate jdbcTemplate

    /**
     * Execute critical path analysis using  using a List of {@code TimelineTask} and a List of {@code TimelineDependency}
     * and returning an instance of {@code TimelineSummary} with results
     * and instance of {@code TaskTimeLineGraph}.
     *
     * @param event an instance of {@code MoveEvent}
     * @param viewUnpublished show only published tasks or all tasks
     *
     * @return CPA calculation results in an instance of {@code TimelineSummary} and
     * 			and instance of {@code TaskTimeLineGraph}
     */
    CPAResults calculateCPA(MoveEvent event, Boolean viewUnpublished = false) {

        List<TimelineTask> tasks = getEventTasks(event, viewUnpublished)
        List<TimelineDependency> taskDependencies = getTaskDependencies(tasks)

        TaskTimeLineGraph graph = createTaskTimeLineGraph(tasks, taskDependencies)
        TimelineSummary summary = calculateTimeline(event.estStartTime, event.estCompletionTime, graph)

        return new CPAResults(graph, summary, tasks, taskDependencies)
    }

    /**
     * Used to load all related tasks associated with an event
     *
     * @param moveEvent the event to retrieve tasks for
     * @param viewUnpublished show only published tasks or all tasks
     * @return List<Task>                  a list of tasks
     */
    @CompileStatic(TypeCheckingMode.SKIP)
    List<TimelineTask> getEventTasks(MoveEvent event, Boolean viewUnpublished = false) {
        List<TimelineTask> tasks

        if (event) {
            tasks = Task.executeQuery(""" 
                    SELECT t.id,
                           t.taskNumber,
                           t.comment,
                           t.duration,
                           t.isCriticalPath,
                           t.status,
                           t.actStart,
                           t.statusUpdated,
                           t.durationScale,
                           t.estStart,
                           t.estFinish,
                           t.latestFinish,
                           t.slack,
                           t.role,
                           t.apiAction.id,
                           t.apiAction.name,
                           t.assignedTo.id,
                           t.assignedTo.firstName,
                           t.assignedTo.lastName,
                           t.assetEntity.id,
                           t.assetEntity.assetName,
                           t.assetEntity.assetType,
                           t.assetEntity.assetClass
                                   
                    from Task t
                    left outer join t.apiAction
                    left outer join t.assignedTo
                    left outer join t.assetEntity
                   where t.moveEvent.id = :eventId	
                """, [eventId: event.id]).collect { TimelineTask.fromResultSet(it) }

        } else {
            tasks = []
        }

        return tasks
    }

    /**
     * Used to get the list of task dependencies for a given list of tasks
     *
     * @param List <AssetComment> a list of tasks
     * @return List<TimelineDependency>                  a list of the dependencies associated to the tasks
     */
    @CompileStatic(TypeCheckingMode.SKIP)
    List<TimelineDependency> getTaskDependencies(List<TimelineTask> tasks) {
        List<TimelineDependency> dependencies

        if (tasks) {
            List<Long> ids = tasks*.id

            dependencies = TaskDependency.executeQuery("""
                SELECT t.id,
                       t.assetComment.id,
                       t.assetComment.taskNumber,
                       t.predecessor.id,
                       t.predecessor.taskNumber
                from TaskDependency t
                         left outer join t.assetComment
                         left outer join t.predecessor
                where t.assetComment.id in :ids)
                UNION
                SELECT t.id,
                       t.assetComment.id,
                       t.assetComment.taskNumber,
                       t.predecessor.id,
                       t.predecessor.taskNumber
                from TaskDependency t
                         left outer join t.assetComment
                         left outer join t.predecessor
                where t.predecessor.id in :ids)
            """, [ids: ids]).collect { TimelineDependency.fromResultSet(it) }

        } else {
            dependencies = []
        }

        return dependencies
    }
    /**
     * Execute critical path analysis using an instance of {@code TaskTimeLineGraph}
     * and returning an instance of {@code TimelineSummary} with results
     * and instance of {@code TaskTimeLineGraph}.
     * It also update {@code TimelineTask} domain in database
     *
     * It throws an Exception if {@code TimelineSummary#cycles} is not empty.
     *
     * @param event an instance of {@code MoveEvent}
     * @param viewUnpublished show only published tasks or all tasks
     *
     * @return CPA calculation results in an instance of {@code TimelineSummary} and
     * 			and instance of {@code TaskTimeLineGraph}
     */
    CPAResults updateTaskFromCPA(MoveEvent event, Boolean viewUnpublished) {

        List<TimelineTask> tasks = getEventTasks(event, viewUnpublished)
        List<TimelineDependency> taskDependencies = getTaskDependencies(tasks)

        TaskTimeLineGraph graph = createTaskTimeLineGraph(tasks, taskDependencies)
        TimelineSummary summary = calculateTimeline(event.estStartTime, event.estCompletionTime, graph)
        updateVertexListInDatabase(graph.vertices.toList())

        return new CPAResults(graph, summary, tasks, taskDependencies)
    }

    /**
     * Creates an instance of {@code TaskTimeLineGraph} using a List of {@code TimelineTask} and a List of {@code TimelineDependency}
     *
     * @param tasks a List of {@code TimelineTask}
     * @param taskDependencies a List of {@code TimelineDependency}
     * @return
     * Service used to retrieve {@code TimelineTask} and {@code TimelineDependency}
     */
    private TaskTimeLineGraph createTaskTimeLineGraph(List<TimelineTask> tasks, List<TimelineDependency> taskDependencies) {
        return new TaskTimeLineGraph.Builder()
                .withVertices(tasks)
                .withEdges(taskDependencies)
                .build()
    }

    /**
     * Execute critical path analysis using an instance of {@code TaskTimeLineGraph}
     * and returning an instance of {@code TimelineSummary} with results
     * and instance of {@code TaskTimeLineGraph}.
     *
     * @param event an instance of {@code MoveEvent}
     * @param graph an instance of {@code TaskTimeLineGraph}
     * @return CPA calculation results in an instance of {@code TimelineSummary} and
     */
    private TimelineSummary calculateTimeline(Date estimatedStartTime, Date estimatedCompletionTime, TaskTimeLineGraph graph) {
        return new TimeLine(graph).calculate(estimatedStartTime, estimatedCompletionTime)
    }

    /**
     * Updates {@code TaskVertex} in {@code TimelineTask} domain class.
     * In particular, we are using {@code JdbcTemplate} looking for a better performance.
     * It saves the following fields from {@code TaskVertex} in {@code TimelineTask} domain class:
     * Task#slack
     *
     * @param tasks a list of {@code TaskVertex} after CPA calculation
     * @see TimeLine#calculate(java.util.Date, java.util.Date)
     */
    private void updateVertexListInDatabase(List<TaskVertex> tasks) {

        String batchUpdateSQLSentence = '''
			update asset_comment 
			   set is_critical_path = ?,
				   slack = ?,
				   est_start = ?,
				   est_finish = ?
			 where asset_comment_id = ?
		'''

        jdbcTemplate.batchUpdate(batchUpdateSQLSentence, new BatchPreparedStatementSetter() {

            @Override
            void setValues(PreparedStatement ps, int i) throws SQLException {
                TaskVertex task = tasks.get(i)
                ps.setBoolean(1, task.criticalPath)
                ps.setInt(2, task.slack)
                ps.setTimestamp(3, new java.sql.Timestamp(task.earliestStartDate.time))
                ps.setTimestamp(4, new java.sql.Timestamp(task.earliestFinishDate.time))
                ps.setLong(5, task.taskId)
            }

            @Override
            int getBatchSize() {
                return tasks.size()
            }
        })
    }
}
/**
 * Class used by TaskTimeLine CPA in calculation response results.
 */
@CompileStatic
class CPAResults {

    final TaskTimeLineGraph graph
    final TimelineSummary summary
    final List<TimelineTask> tasks
    final List<TimelineDependency> taskDependencies

    CPAResults(TaskTimeLineGraph graph, TimelineSummary summary, List<TimelineTask> tasks, List<TimelineDependency> taskDependencies) {
        this.graph = graph
        this.summary = summary
        this.tasks = tasks
        this.taskDependencies = taskDependencies
    }
}
