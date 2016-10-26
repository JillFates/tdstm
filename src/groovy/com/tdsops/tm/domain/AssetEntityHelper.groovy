package com.tdsops.tm.domain

import com.tds.asset.AssetEntity
import com.tdsops.common.grails.ApplicationContextHolder
import com.tdsops.tm.enums.domain.AssetClass
import com.tdssrc.grails.NumberUtil
import net.transitionmanager.domain.Project
import net.transitionmanager.service.SecurityService

/**
 * Provides helper functionality for the AssetEntity domain
 */
class AssetEntityHelper {

	/**
	 * Used to lookup an asset by it's ID number and will validate that it belongs to the user's current project
	 * @param project - the project the user is assigned to
	 * @param deviceId - the device id of the device to lookup
	 * @param params - the parameters passed from the browser
	 * @return The asset for the specified id as long it is a valid id number, the asset exists and it belongs to the user's current project, otherwise null
	 */
	static AssetEntity getAssetById(Project project, AssetClass ac, assetId) {
		AssetEntity asset
		Long id = NumberUtil.toLong(assetId)
		def domainClass
		if (ac == null) {
			domainClass = AssetEntity
		}
		else {
			domainClass = AssetClass.domainClassFor(ac)
		}

		if (id) {
			asset = domainClass.get(id)
			if (asset) {
				if (asset.projectId != project?.id) {
					SecurityService securityService = ApplicationContextHolder.getBean('securityService', SecurityService)
					securityService.reportViolation("Attempt to access ${AssetClass.domainNameFor(ac)} ($assetId) of unassociated project ($project.id)")
				}
			}
		}
		return asset
	}

	/**
	 * Creates a simple map/model of an asset that contains just the basic details of the asset.
	 * @param asset - the domain object of the asset
	 * @return  the model map populated from the asset properties
	 */
	static Map simpleModelOfAsset(asset) {
		[asset: [id:         asset.id,
		         assetName:  asset.assetName,
		         assetType:  asset.assetType,
		         assetClass: asset.assetClass,
		         assetTag:   asset.assetTag]]
	}
}
