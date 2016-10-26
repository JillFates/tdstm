package com.tds.asset

import net.transitionmanager.domain.Project

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
