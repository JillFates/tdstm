package com.tds.asset

class AssetDependencyBundle {
	Integer dependencyBundle = 0
	AssetEntity asset
	String dependencySource
	Date lastUpdated
    
    static constraints = {
		dependencyBundle( blank:false, nullable:false, unique:'asset')
		asset( blank:false, nullable:false, unique:true)
		dependencySource( blank:false, nullable:false )
		lastUpdated( blank:true, nullable:true )
    }
	static mapping  = {
		version false
	}
}
