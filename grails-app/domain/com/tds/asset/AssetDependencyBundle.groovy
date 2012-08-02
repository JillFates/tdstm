package com.tds.asset

class AssetDependencyBundle {
	Integer dependencyBundle = 0
	AssetEntity asset
	String dependencySource
	Date lastUpdated
	Project project
    
    static constraints = {
		dependencyBundle( nullable:false, unique:'asset')
		asset( nullable:false, unique:true)
		dependencySource( blank:false, nullable:false )
		lastUpdated( nullable:true )
		project( nullable:false )
    }
	static mapping  = {
		version false
	}
}
