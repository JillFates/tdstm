package com.tds.asset

import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.TimeUtil
import net.transitionmanager.domain.Person

import static com.tdsops.tm.enums.domain.AssetDependencyStatus.FUTURE
import static com.tdsops.tm.enums.domain.AssetDependencyStatus.QUESTIONED
import static com.tdsops.validators.CustomValidators.inList
import static com.tdsops.validators.CustomValidators.optionsClosure
import static com.tds.asset.AssetOptions.AssetOptionsType.DEPENDENCY_STATUS
import static com.tds.asset.AssetOptions.AssetOptionsType.DEPENDENCY_TYPE

class AssetDependency {

	AssetEntity asset         // The asset that that REQUIRES the 'dependent'
	AssetEntity dependent     // The asset that SUPPORTS 'asset'
	String type = 'Unknown'
	String dataFlowFreq = 'Unknown'
	String dataFlowDirection = 'Unknown'
	String status = 'Unknown'
	String comment = ''
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
		dependent nullable: false
		c1 nullable: true, blank: true, size:0..255
		c2 nullable: true, blank: true, size:0..255
		c3 nullable: true, blank: true, size:0..255
		c4 nullable: true, blank: true, size:0..255
		comment nullable: true, blank: true, size: 0..65535
    	createdBy nullable: true // @See TM-8392
		updatedBy nullable: true
		dataFlowDirection blank: false, size: 0..14, inList: ['Unknown', 'bi-directional', 'incoming', 'outgoing']
		dataFlowFreq nullable: true, size: 0..8, inList: ['Unknown', 'constant', 'hourly', 'daily', 'weekly', 'monthly']
		status blank: false, validator: inList(optionsClosure(DEPENDENCY_STATUS), 'status')
		type blank: false, validator: inList(optionsClosure(DEPENDENCY_TYPE), 'type')
	}

	static mapping = {
		autoTimestamp false
		comment sqltype: 'text'
		createdBy column: 'created_by'
		id column: 'asset_dependency_id'
		isFuture formula: "status = '$FUTURE'"
		isStatusResolved formula: "status != '$QUESTIONED'"
		updatedBy column: 'updated_by'
	}

	def beforeInsert = {
		dateCreated = lastUpdated = TimeUtil.nowGMT()
	}
	def beforeUpdate = {
		lastUpdated = TimeUtil.nowGMT()
	}

	/**
	 * Clone this Entity and replace properties if a map is specified
	 * @param replaceKeys
	 * @return
	 */
	AssetDependency clone(Map replaceKeys = [:]){
		AssetDependency clonedAsset = GormUtil.cloneDomain(this, replaceKeys) as AssetDependency
		return clonedAsset
	}

	/** Helper Fetchers ************************/
	static List<AssetDependency> fetchSupportedDependenciesOf(AssetEntity assetEntity){
		return AssetDependency.findAll(
			'from AssetDependency where dependent=? order by asset.assetType, asset.assetName asc',
			[assetEntity]
		)
	}

	static List<AssetDependency> fetchRequiredDependenciesOf(AssetEntity assetEntity){
		return AssetDependency.findAll(
			'from AssetDependency where asset=? order by dependent.assetType, dependent.assetName asc',
			[assetEntity]
		)
	}
}
