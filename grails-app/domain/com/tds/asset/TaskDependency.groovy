/**
 * TaskDependency represents the association of tasks with various types of dependencies amongst them 
 */

package com.tds.asset

import com.tdsops.tm.enums.domain.TaskDependencyType

class TaskDependency {
	
	AssetComment assetComment		// aka successor
	AssetComment predecessor

	Integer downstreamTaskCount	= 0	// The number of tasks that are downstream from the predecessor task
	Integer pathDepth = 0			// The depth from the furthest sink task in the map
	Integer pathDuration = 0		// The total duration forward to the furthest sink task on the path in the map

	TaskDependencyType type = TaskDependencyType.getDefault()
		
	static constraints = {
		assetComment()
		predecessor()
		type( inList:TaskDependencyType.getKeys() )
	}
	
	static mapping = {
		id column:'task_dependency_id'
		assetComment fetch:'join'
		predecessor fetch:'join'
		type sqltype: 'char', length:2
		downstreamTaskCount sqltype: 'INT(6)'
		pathDepth sqltype: 'INT(6)'
		pathDuration sqltype: 'INT(6)'
	}

	static transients = ['successor']
	
	// Add successor accessors that are easier to understand than the assetComment property
	void setSuccessor(AssetComment task) { this.assetComment = task }
	AssetComment getSuccessor() { this.assetComment }

	String toString() {
		"$id: ${predecessor?.taskNumber ?: predecessor.id} to ${successor?.taskNumber ?: successor.id}"
	}
}
