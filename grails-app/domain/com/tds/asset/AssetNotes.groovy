package com.tds.asset

class AssetNotes {
	
	Person createdBy
	String note
	
	Date dateCreated
	Date lastUpdated
	
	static belongsTo = [ assetComment : AssetComment ]
	
    static constraints = {
		dateCreated( blank:true, nullable:true  )
		createdBy( blank:true, nullable:true  )
		note( blank:true, nullable:true  )
    }
	
	String toString(){
	    note
	}
}
