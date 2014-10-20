package com.tdsops.tm.domain

import com.tds.asset.AssetEntity
import com.tdsops.common.grails.ApplicationContextHolder
import com.tdsops.tm.enums.domain.AssetClass
import com.tdssrc.grails.NumberUtil

/**
 * Provides helper functionality for the AssetEntity domain
 */
class AssetEntityHelper {
	
	/**
	 * Used to lookup an asset by it's ID number and will validate that it belongs to the user's current project
	 * @param project - the project the user is assigned to
	 * @param deviceId - the device id of the device to lookup
	 * @param params - the parameters passed from the browser
	 * @return The asset for the specified id as long it is a valid id number, the asset exists and it belongs to the user's current project otherwise null
	 */
	static AssetEntity getAssetById(project, AssetClass ac, Object assetId) {
		AssetEntity asset
		Long id = NumberUtil.toLong(assetId)
		def domainClass = AssetClass.domainClassFor(ac)
		println "AssetEntityHelper.getAssetById() called"
		if (id) {
			asset = domainClass.get(id)
			if (asset) {
				if (asset.project != project) {
					def securityService = ApplicationContextHolder.getBean('securityService')
					securityService.reportViolation("Attempt to access ${AssetClass.domainNameFor(ac)} ($assetId) of unassociated project (${project.id})")
				}
			}
		}
		return asset
	}

	/**
	 * Used to create a simple map/model of an asset that contains just the basic details of an asset
	 * @param asset - the domain object of the asset
	 * @return the model with properties from the asset
	 */
	static Map simpleModelOfAsset(asset) {
		[ asset: [
			id: asset.id,
			assetName: asset.assetName,
			assetType: asset.assetType,
			assetClass: asset.assetClass,
			assetTag: asset.assetTag
		] ]
	}


}