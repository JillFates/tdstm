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
	 * Execute critical path analysis using an instance of {@code TaskTimeLineGraph}
	 * and returning an instance of {@code TimelineSummary} with results.
	 *
	 * @param event an instance of {@code MoveEvent}
	 * @param tasks a List of {@code Task}
	 * @param taskDependencies a List of {@code TaskDependency}
	 *
	 * @return CPA calculation results in an instance of {@code TimelineSummary}
	 * 			and an instance {@code TaskTimeLineGraph}
	 */
	List executeCPA(MoveEvent event, List<Task> tasks, List<TaskDependency> taskDependencies) {

		TaskTimeLineGraph graph = new TaskTimeLineGraph.Builder()
			.withVertices(tasks)
			.withEdges(taskDependencies)
			.build()

		TimelineSummary summary = new TimeLine(graph)
			.calculate(new Date(event.estStartTime.time), new Date(event.estCompletionTime.time))

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
