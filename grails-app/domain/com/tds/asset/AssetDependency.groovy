package com.tds.asset
import com.tdssrc.grails.TimeUtil;

class AssetDependency {

	AssetEntity  asset			// The asset that that REQUIRES the 'dependent'
	AssetEntity dependent		// The asset that SUPPORTS 'asset' 
	String type = "Unknown"
	String dataFlowFreq = "Unknown"
	String dataFlowDirection = "Unknown"
	String status
	String comment
	Person createdBy
	Person updatedBy
		
	Date dateCreated
	Date lastUpdated

	String c1
	String c2
	String c3
	String c4
	static constraints = {
		asset( nullable:false, unique:['dependent', 'type'] )
		dependent( nullable:true)
		type( blank:false, nullable:false )
		dataFlowFreq(blank:true, nullable:true, inList:[
			"Unknown",
			"constant",
			"hourly",
			"daily",
			"weekly",
			"monthly"
		])
		dataFlowDirection(blank:false, nullable:false , inList:[
			"Unknown",
			"bi-directional",
			"incoming",
			"outgoing"
		])
		status(blank:false, nullable:false )
		comment(blank:true, nullable:true)
		updatedBy( nullable:false )
		c1( blank:true, nullable:true )
		c2( blank:true, nullable:true )
		c3( blank:true, nullable:true )
		c4( blank:true, nullable:true )
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
		dateCreated = TimeUtil.nowGMT()
		lastUpdated = TimeUtil.nowGMT()
	}
	def beforeUpdate = {
		lastUpdated = TimeUtil.nowGMT()
	}
}
