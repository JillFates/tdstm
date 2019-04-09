package net.transitionmanager.asset

import net.transitionmanager.project.Project

class AssetDependencyBundle {

	Integer dependencyBundle = 0
	String dependencySource
	Date lastUpdated
	Project project

	static belongsTo = [asset: AssetEntity]

	static constraints = {
		asset unique: true
		dependencyBundle unique: 'asset'
		dependencySource blank: false
		lastUpdated nullable: true
	}

	static mapping = {
		version false
	}
}
