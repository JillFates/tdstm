package net.transitionmanager.task.timeline

import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import net.transitionmanager.imports.TaskBatch
import net.transitionmanager.project.MoveEvent
import net.transitionmanager.service.ServiceMethods
import net.transitionmanager.task.Task
import org.springframework.jdbc.core.BatchPreparedStatementSetter
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

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
     * Instance of {@code NamedParameterJdbcTemplate} to query task dependencies
     */
    NamedParameterJdbcTemplate namedParameterJdbcTemplate

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
        List<TimelineDependency> taskDependencies = getTaskDependencies(event)

        TaskTimeLineGraph graph = createTaskTimeLineGraph(tasks, taskDependencies)
        TimelineSummary summary = calculateTimeline(event.estStartTime, event.estCompletionTime, graph)

        return new CPAResults(graph, summary, tasks, taskDependencies)
    }

    /**
     * Used to load all related tasks associated with an event
     *
     * @param moveEvent the event to retrieve tasks for
     * @param viewUnpublished show only published tasks or all tasks
     * @return List<Task>                                        a list of tasks
     */
    @CompileStatic(TypeCheckingMode.SKIP)
    List<TimelineTask> getEventTasks(TaskBatch taskBatch) {
        List<TimelineTask> tasks

        if (taskBatch) {
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
                           t.assetEntity.assetClass,
                           t.taskSpec
                                   
                    from Task t
                    left outer join t.apiAction
                    left outer join t.assignedTo
                    left outer join t.assetEntity
                    left outer join t.taskBatch 
                   where t.taskBatch.id = :task_batch_id
                """, [task_batch_id: taskBatch.id]).collect { TimelineTask.fromResultSet(it) }

        } else {
            tasks = []
        }

        return tasks
    }

    /**
     * Used to load all related tasks associated with an event
     *
     * @param moveEvent the event to retrieve tasks for
     * @param viewUnpublished show only published tasks or all tasks
     * @return List<Task>                                        a list of tasks
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
     * Used to get the list of task dependencies for a given {@code MoveEvent}
     *
     * @param MoveEvent event
     * @return List<TimelineDependency>              a list of the tasks dependencies associated to a MoveEvent
     */
    @CompileStatic(TypeCheckingMode.SKIP)
    List<TimelineDependency> getTaskDependencies(List<TimelineTask> taskList) {
        List<TimelineDependency> dependencies = []

        if (taskList) {
            List<Long> ids = taskList*.id

            String query = """
                select distinct task_dependency_id, successor_id, successor_task_number, predecessor_id, predecessor_task_number
                from (
                         select TD.task_dependency_id as task_dependency_id,
                                TD.asset_comment_id   as successor_id,
                                SUC.task_number       as successor_task_number,
                                TD.predecessor_id     as predecessor_id,
                                PRE.task_number       as predecessor_task_number
                         from task_dependency TD
                                  join asset_comment SUC on TD.asset_comment_id = SUC.asset_comment_id
                                  join asset_comment PRE on TD.predecessor_id = PRE.asset_comment_id
                         where TD.predecessor_id in (:tasks_ids)
                         UNION
                         select TD.task_dependency_id as task_dependency_id,
                                TD.asset_comment_id   as successor_id,
                                SUC.task_number       as successor_task_number,
                                TD.predecessor_id     as predecessor_id,
                                PRE.task_number       as predecessor_task_number
                         from task_dependency TD
                                  join asset_comment SUC on TD.asset_comment_id = SUC.asset_comment_id
                                  join asset_comment PRE on TD.predecessor_id = PRE.asset_comment_id
                         where TD.asset_comment_id in (:tasks_ids)
                     ) as results                
            """.stripIndent()

            dependencies = namedParameterJdbcTemplate.queryForList(
                    query,
                    [tasks_ids: ids]
            ).collect { TimelineDependency.fromResultSet(it) }
        }

        return dependencies
    }

    /**
     * Used to get the list of task dependencies for a given list of tasks
     *
     * @param List <AssetComment> a list of tasks
     * @return List<TimelineDependency>              a list of the tasks dependencies associated to a MoveEvent
     */
    @CompileStatic(TypeCheckingMode.SKIP)
    List<TimelineDependency> getTaskDependencies(MoveEvent event) {
        List<TimelineDependency> dependencies = []

        if (event) {
            String query = """
                select distinct task_dependency_id, successor_id, successor_task_number, predecessor_id, predecessor_task_number
                from (
                         select TD.task_dependency_id as task_dependency_id,
                                TD.asset_comment_id   as successor_id,
                                SUC.task_number        as successor_task_number,
                                TD.predecessor_id     as predecessor_id,
                                PRE.task_number        as predecessor_task_number
                         from task_dependency TD
                                  join asset_comment SUC on TD.asset_comment_id = SUC.asset_comment_id
                                  join asset_comment PRE on TD.predecessor_id = PRE.asset_comment_id
                                  join (select asset_comment_id as id from asset_comment WHERE move_event_id = :event_id) as tasks_ids
                                       on TD.asset_comment_id = tasks_ids.id
                         UNION
                         select TD.task_dependency_id as task_dependency_id,
                                TD.asset_comment_id   as successor_id,
                                SUC.task_number        as successor_task_number,
                                TD.predecessor_id     as predecessor_id,
                                PRE.task_number        as predecessor_task_number
                         from task_dependency TD
                                  join asset_comment SUC on TD.asset_comment_id = SUC.asset_comment_id
                                  join asset_comment PRE on TD.predecessor_id = PRE.asset_comment_id
                                  join (select asset_comment_id as id from asset_comment WHERE move_event_id = :event_id) as tasks_ids
                                       on TD.predecessor_id = tasks_ids.id
                     ) as results
                where results.successor_id in (select asset_comment_id as id from asset_comment WHERE move_event_id = :event_id)
                  and results.predecessor_id in (select asset_comment_id as id from asset_comment WHERE move_event_id = :event_id)
            """.stripIndent()

            dependencies = namedParameterJdbcTemplate.queryForList(
                    query,
                    [event_id: event.id]
            ).collect { TimelineDependency.fromResultSet(it) }
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
        List<TimelineDependency> taskDependencies = getTaskDependencies(event)

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
