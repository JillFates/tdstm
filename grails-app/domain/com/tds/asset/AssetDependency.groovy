package com.tds.asset
import com.tdssrc.grails.GormUtil;

class AssetDependency {

	AssetEntity  asset
	AssetEntity dependent
	String type = "Unknown"
	String dataFlowFreq = "Unknown"
	String dataFlowDirection = "Unknown"
	String status
	String comment
	Person createdBy
	Person updatedBy
	
	
	Date dateCreated
	Date lastUpdated

	static constraints = {
		asset( blank:false, nullable:false )
		dependent(blank:true, nullable:true)
		type( blank:false, nullable:false ,inList:[
													"Unknown",
													"Runs On",
													"Hosts" ,
													"DB" ,
													"Web" ,
													"Backup" ,
													"File",
													"Other"
												])
		dataFlowFreq(blank:true, nullable:true, inList:[
													"Unknown",
													"constant",
													"batch"
												])
		dataFlowDirection(blank:false, nullable:false , inList:[
													"Unknown",
													"bi-directional",
													"incoming",
													"outgoing"
												])
		status(blank:false, nullable:false ,inList:["Unknown", "Validated", "Questioned"])
		comment(blank:true, nullable:true)
		updatedBy( blank:false, nullable:false )
	}

	static mapping={	
		version true
		autoTimestamp false
		id column: 'asset_dependency_id'
		updatedBy column: 'updated_by'
		createdBy column: 'created_by'
		columns {
			comment sqltype: 'text'
		}
	}
	/*
	 * Date to insert in GMT
	 */
	def beforeInsert = {
		dateCreated = GormUtil.convertInToGMT( "now", "EDT" )
		lastUpdated = GormUtil.convertInToGMT( "now", "EDT" )
	}
	def beforeUpdate = {
		lastUpdated = GormUtil.convertInToGMT( "now", "EDT" )
	}
}
