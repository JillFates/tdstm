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
	 * @param tasks
	 */
	private void updateVertexListInDatabase(List<TaskVertex> tasks) {

		String sqlSentence = '''
			UPDATE asset_comment 
				set is_critical_path = ?
				where id = ?
		'''

		jdbcTemplate.batchUpdate(sqlSentence, new BatchPreparedStatementSetter() {

			@Override
			void setValues(PreparedStatement ps, int i) throws SQLException {
				TaskVertex task = tasks.get(i)
				ps.setBoolean(1, task.criticalPath)
				ps.setLong(2, task.taskId)
			}

			@Override
			int getBatchSize() {
				return tasks.size()
			}
		})

	}
}
