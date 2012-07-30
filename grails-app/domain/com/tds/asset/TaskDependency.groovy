package com.tds.asset

/**
 * 
 * 
 * @author pbwebguy
 *
 */
class TaskDependency {
	// enum DependencyType { FS,FF,SF,SS }
	
	AssetComment predecessor
	// AssetComment successor
	String dependencyType = 'FS'
	// Integer delayTime		// # of minutes to delay either lead or lag 
	// Integer delayType		// 1=Lead, 2=Lag
	
	static belongsTo =  [ assetComment : AssetComment ]
	
	static constraints = {
		predecessor(nullable:false)
		dependencyType( inList:['FS','FF','SF','SS'] )
	}
	
	static mapping = {
		dependencyType sqltype: 'char', length:2
		id column:'task_dependency_id'
	}

	String toString() {
		"${assetComment.id}(${dependencyType})~${id}"
	}
}
