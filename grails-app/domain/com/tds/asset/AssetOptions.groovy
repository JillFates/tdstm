package com.tds.asset

class AssetOptions {
	static enum AssetOptionsType{STATUS_OPTION,PRIORITY_OPTION,DEPENDENCY_TYPE,DEPENDENCY_STATUS,ENVIRONMENT_OPTION}
	
	AssetOptionsType type
	String value
	
	static mapping = {
		version false
	}
	
    static constraints = {
		type(nullable:false)
		value(blank:false,nullable:false)
    }
}
