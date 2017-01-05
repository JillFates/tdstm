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

	// This map is used to match import values and object properties.
	static Map CROSS_REFERENCES = [sme1:'sme', sme2:'sme2', owner:'appOwner']

	// This maps and import value to the corresponding value to be assigned.
	static Map CROSS_REFERENCE_VALUES = [sme1: "#SME1", sme2: "#SME2", owner: "#Owner"]

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
				if (asset.project.id != project?.id) {
					SecurityService securityService = ApplicationContextHolder.getBean('securityService', SecurityService)
					// ac is always null from some context
					if(ac != null) {
						securityService.reportViolation("Attempt to access ${AssetClass.domainNameFor(ac)} ($assetId) of unassociated project ($project.id)")
					} else{
						securityService.reportViolation("Attempt to access ($assetId) of unassociated project ($project.id)")
					}
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


	/**
	 * This method determines if a given hash references a valid
	 * asset property.
	 *
	 * @param asset: AssetEntity instance used for looking up the property.
	 * @param propertyRef: the property to be looked up.
	 *
	 * @return the property name if it exists, null otherwise.
	 * 
	 */

	 static String getPropertyNameByHashReference(AssetEntity asset, String propertyRef){

	 	String propertyName = null

	 	if(asset && propertyRef){

	 		propertyRef = propertyRef.toLowerCase()

	 		// checks if we need to strip off the #
	 		if (propertyRef[0] == '#') {
           	 	propertyRef = propertyRef.substring(1)
        	}

        	// Determines if the property is a valid cross reference and the asset has it.
        	if (CROSS_REFERENCES.containsKey(propertyRef)){
        		String cr = CROSS_REFERENCES[propertyRef]
        		if(asset.metaClass.hasProperty(asset.getClass(), cr)){
        			propertyName = CROSS_REFERENCE_VALUES[propertyRef]
        		}
        	}

	 	}


	 	return propertyName
	 }
}
