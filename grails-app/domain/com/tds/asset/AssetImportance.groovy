package com.tds.asset

class AssetImportance {
	
	String entityType
	Project project
	String data

    static constraints = {
		entityType( nullable:false, inList:['AssetEntity', 'Application', 'Database','Files'] )
		project( nullable:true )
		data( nullable:true , blank:true )
    }
	
	static mapping  = {
		version false
		columns {
			data sqltype: 'text'
		}
	}
}