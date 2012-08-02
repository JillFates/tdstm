package com.tds.asset

class CommentNote {
	
	Person createdBy
	String note
	
	Date dateCreated
	Date lastUpdated
	
	static belongsTo = [ assetComment : AssetComment ]
	
    static constraints = {
		dateCreated( nullable:true  )
		createdBy( nullable:true  )
		note( blank:true, nullable:true  )
    }
	
	String toString(){
	    note
	}
}