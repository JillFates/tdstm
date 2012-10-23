package com.tds.asset

class AssetDependencyBundle {
	Integer dependencyBundle = 0
	String dependencySource
	Date lastUpdated
	Project project
    
	static belongsTo = [asset: AssetEntity]
	
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