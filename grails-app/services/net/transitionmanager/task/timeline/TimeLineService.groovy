package net.transitionmanager.task.timeline

import grails.gorm.transactions.Transactional
import net.transitionmanager.project.MoveEvent
import net.transitionmanager.service.ServiceMethods
import net.transitionmanager.task.Task
import net.transitionmanager.task.TaskDependency
import org.springframework.jdbc.core.BatchPreparedStatementSetter
import org.springframework.jdbc.core.JdbcTemplate

import java.sql.PreparedStatement
import java.sql.SQLException

@Transactional
class TimeLineService implements ServiceMethods {

	/**
	 * Instance of {@code JdbcTemplate} used to save CPA results in Database
	 * with a better performance.
	 */
	JdbcTemplate jdbcTemplate

	/**
	 * Creates an instance of {@code TaskTimeLineGraph} using a List of {@code Task} and a List of {@code TaskDependency}
	 *
	 * @param tasks a List of {@code Task}
	 * @param taskDependencies a List of {@code TaskDependency}
	 * @return
	 */
	TaskTimeLineGraph createTaskTimeLineGraph(List<Task> tasks, List<TaskDependency> taskDependencies) {
		return new TaskTimeLineGraph.Builder()
			.withVertices(tasks)
			.withEdges(taskDependencies)
			.build()
	}

	/**
	 * Execute critical path analysis using  using a List of {@code Task} and a List of {@code TaskDependency}
	 * and returning an instance of {@code TimelineSummary} with results
	 * and instance of {@code TaskTimeLineGraph}.
	 *
	 * @param event an instance of {@code MoveEvent}
	 * @param tasks a List of {@code Task}
	 * @param taskDependencies a List of {@code TaskDependency}
	 *
	 * @return CPA calculation results in an instance of {@code TimelineSummary} and
	 * 			and instance of {@code TaskTimeLineGraph}
	 */
	List calculateCPA(MoveEvent event, List<Task> tasks, List<TaskDependency> taskDependencies) {

		TaskTimeLineGraph graph = createTaskTimeLineGraph(tasks, taskDependencies)
		TimelineSummary summary = calculateCPA(event, graph)

		return [graph, summary]
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
	TimelineSummary calculateCPA(MoveEvent event, TaskTimeLineGraph graph) {
		return new TimeLine(graph).calculate(event.estStartTime, event.estCompletionTime)
	}

	/**
	 * Execute critical path analysis using an instance of {@code TaskTimeLineGraph}
	 * and returning an instance of {@code TimelineSummary} with results
	 * and instance of {@code TaskTimeLineGraph}.
	 * It also update {@code Task} domain in database
	 *
	 * It throws an Exception if {@code TimelineSummary#cycles} is not empty.
	 *
	 * @param event an instance of {@code MoveEvent}
	 * @param tasks a List of {@code Task}
	 * @param taskDependencies a List of {@code TaskDependency}
	 *
	 * @return CPA calculation results in an instance of {@code TimelineSummary} and
	 * 			and instance of {@code TaskTimeLineGraph}
	 */
	List updateTaskFromCPA(MoveEvent event, List<Task> tasks, List<TaskDependency> taskDependencies) {

		def (TaskTimeLineGraph graph, TimelineSummary summary) = calculateCPA(event, tasks, taskDependencies)

		updateVertexListInDatabase(graph.vertices.toList())

		return [graph, summary]
	}

	/**
	 * Updates {@code TaskVertex} in {@code Task} domain class.
	 * In particular, we are using {@code JdbcTemplate} looking for a better performance.
	 * It saves the following fields from {@code TaskVertex} in {@code Task} domain class:
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
