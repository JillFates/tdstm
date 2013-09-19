/**
 * TaskDependency represents the association of tasks with various types of dependencies amongst them 
 */

package com.tds.asset

import com.tdsops.tm.enums.domain.TaskDependencyType

class TaskDependency {
	
	AssetComment assetComment	// aka successor
	AssetComment predecessor

	Integer downstreamTaskCount	= 0	// The number of tasks that are downstream from the predecessor task
	Integer pathDepth = 0			// The depth from the furthest sink task in the map
	Integer pathDuration = 0		// The total duration forward to the furthest sink task on the path in the map

	// AssetComment successor
	String type = TaskDependencyType.FS
	// Integer delayTime		// # of minutes to delay either lead or lag 
	// Integer delayType		// 1=Lead, 2=Lag
	
// 	static belongsTo =  [ assetComment : AssetComment ]
	
	static constraints = {
		assetComment(nullable:false)
		predecessor(nullable:false)
		type( inList:TaskDependencyType.getKeys() )
	}
	
	static mapping = {
		type sqltype: 'char', length:2
		id column:'task_dependency_id'
		assetComment fetch:'join'
		predecessor fetch:'join'
	}

	static transients = ['successor']
	
	// Add successor accessors that are easier to understand than the assetComment property
	void setSuccessor(task) { this.assetComment = task }
	AssetComment getSuccessor() { this.assetComment }

	String toString() {
		"$id: ${predecessor?.taskNumber ?: predecessor.id} to ${successor?.taskNumber ?: successor.id}"
	}
}
