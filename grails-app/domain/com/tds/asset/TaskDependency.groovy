/**
 * TaskDependency represents the association of tasks with various types of dependencies amongst them 
 */

package com.tds.asset

import com.tdsops.tm.enums.domain.TaskDependencyType

class TaskDependency {
	
	AssetComment assetComment	// aka successor
	AssetComment predecessor

	// AssetComment successor
	String type = TaskDependencyType.FS
	// Integer delayTime		// # of minutes to delay either lead or lag 
	// Integer delayType		// 1=Lead, 2=Lag
	
// 	static belongsTo =  [ assetComment : AssetComment ]
	
	static constraints = {
		assetComment(nullable:false)
		predecessor(nullable:false)
		type( inList:TaskDependencyType.getList() )
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
		"${assetComment.id}(${type})~${id}"
	}
}
