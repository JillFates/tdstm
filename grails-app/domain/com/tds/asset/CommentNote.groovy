package com.tds.asset

import com.tdssrc.grails.GormUtil

/** 
  * CommentNote represents a note created by users and associated to AssetComment (aka Task)
  */
class CommentNote {
	
	Person createdBy
	String note
	
	Date dateCreated
	Date lastUpdated
	
	static belongsTo = [ assetComment : AssetComment ]
	
    static constraints = {
		dateCreated( nullable:true  )
		createdBy( nullable:false  )
		note( blank:false, nullable:false  )
    }

	static mapping  = {	
		version false
		autoTimestamp false
		note sqlType:'text'
	}
	
	def beforeInsert = {
		dateCreated = GormUtil.convertInToGMT( "now", "EDT" )
		lastUpdated = GormUtil.convertInToGMT( "now", "EDT" )
	}
	def beforeUpdate = {
		lastUpdated = GormUtil.convertInToGMT( "now", "EDT" )
	}
	
	String toString(){
	    note
	}
}