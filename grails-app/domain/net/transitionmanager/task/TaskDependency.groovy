package net.transitionmanager.task

import com.tdsops.tm.enums.domain.TaskDependencyType
import net.transitionmanager.task.AssetComment

/**
 * Represents the association of tasks with various types of dependencies amongst them.
 */
class TaskDependency {

	AssetComment assetComment       // aka successor
	AssetComment predecessor

	TaskDependencyType type = TaskDependencyType.getDefault()
	Integer downstreamTaskCount = 0 // The number of tasks that are downstream from the predecessor task
	Integer pathDepth = 0           // The depth from the furthest sink task in the map
	Integer pathDuration = 0        // The total duration forward to the furthest sink task on the path in the map

	Integer tmpSuccessorDepCount    // Transient for TaskService.getNeighborhood
	Integer tmpPredecessorDepCount  // Transient for TaskService.getNeighborhood

	static mapping = {
		assetComment fetch: 'join'
		downstreamTaskCount sqltype: 'INT(6)'
		id column: 'task_dependency_id'
		pathDepth sqltype: 'INT(6)'
		pathDuration sqltype: 'INT(6)'
		predecessor fetch: 'join'
		type sqltype: 'char', length: 2
	}

	static transients = ['successor', 'tmpPredecessorDepCount', 'tmpSuccessorDepCount']

	// Add successor accessors that are easier to understand than the assetComment property
	void setSuccessor(AssetComment task) { assetComment = task }
	AssetComment getSuccessor() { assetComment }

	String toString() {
		"$id: ${predecessor?.taskNumber ?: predecessorId} to ${assetComment?.taskNumber ?: assetCommentId}"
	}
}
