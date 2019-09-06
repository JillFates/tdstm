package net.transitionmanager.task.timeline

import net.transitionmanager.project.MoveEvent
import net.transitionmanager.service.ServiceMethods
import net.transitionmanager.task.Task
import net.transitionmanager.task.TaskDependency
import org.springframework.jdbc.core.BatchPreparedStatementSetter
import org.springframework.jdbc.core.JdbcTemplate

import java.sql.PreparedStatement
import java.sql.SQLException

class TimeLineService implements ServiceMethods {

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
	 */
	TimelineSummary executeCPA(MoveEvent event, List<Task> tasks, List<TaskDependency> taskDependencies) {

		TaskTimeLineGraph graph = new TaskTimeLineGraph.Builder()
			.withVertices(tasks)
			.withEdges(taskDependencies)
			.build()

		TimelineSummary summary = new TimeLine(graph)
			.calculate(event.estStartTime, event.estCompletionTime)

		updateVertexListInDatabase(graph.vertices.toList())

		return summary
	}

	/**
	 * Updates {@code TaskVertex} in {@code Task} domain class.
	 * In particular, we are using {@code JdbcTemplate} looking for a better performance.
	 *
	 * @param tasks a list of {@code TaskVertex} after CPA calculation
	 * @see TimeLine#calculate(java.util.Date, java.util.Date)
	 */
	private void updateVertexListInDatabase(List<TaskVertex> tasks) {

		jdbcTemplate.batchUpdate(Task.batchUpdateSQLSentence, new BatchPreparedStatementSetter() {

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
