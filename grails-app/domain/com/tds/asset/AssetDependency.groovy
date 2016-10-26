package com.tds.asset

import com.tdssrc.grails.TimeUtil
import net.transitionmanager.domain.Person

import static com.tdsops.tm.enums.domain.AssetDependencyStatus.FUTURE
import static com.tdsops.tm.enums.domain.AssetDependencyStatus.QUESTIONED

class AssetDependency {

	AssetEntity asset         // The asset that that REQUIRES the 'dependent'
	AssetEntity dependent     // The asset that SUPPORTS 'asset'
	String type = 'Unknown'
	String dataFlowFreq = 'Unknown'
	String dataFlowDirection = 'Unknown'
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

	Boolean isFuture
	Boolean isStatusResolved

	static constraints = {
		asset unique: ['dependent', 'type']
		c1 nullable: true
		c2 nullable: true
		c3 nullable: true
		c4 nullable: true
		comment nullable: true
		dataFlowDirection blank: false, size: 0..14, inList: ['Unknown', 'bi-directional', 'incoming', 'outgoing']
		dataFlowFreq nullable: true, size: 0..8, inList: ['Unknown', 'constant', 'hourly', 'daily', 'weekly', 'monthly']
		dependent nullable: true
		status blank: false
		type blank: false
	}

	static mapping = {
		autoTimestamp false
		createdBy column: 'created_by'
		id column: 'asset_dependency_id'
		isFuture formula: "status = '$FUTURE'"
		isStatusResolved formula: "status != '$QUESTIONED'"
		updatedBy column: 'updated_by'
		columns {
			comment sqltype: 'text'
		}
	}

	def beforeInsert = {
		dateCreated = lastUpdated = TimeUtil.nowGMT()
	}
	def beforeUpdate = {
		lastUpdated = TimeUtil.nowGMT()
	}
}
