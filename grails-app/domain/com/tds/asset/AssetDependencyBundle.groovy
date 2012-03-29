package com.tds.asset

class AssetDependencyBundle {
	Integer dependencyBundle = 0
	AssetEntity asset
	String dependencySource
    
    static constraints = {
		dependencyBundle( blank:false, nullable:false )
		asset( blank:false, nullable:false )
		dependencySource( blank:false, nullable:false )
		
    }
	static mapping  = {
		version false
	}
}
