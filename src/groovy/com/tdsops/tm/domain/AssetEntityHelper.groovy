package com.tdsops.tm.domain

import com.tds.asset.AssetEntity
import com.tdsops.common.grails.ApplicationContextHolder
import com.tdsops.tm.enums.domain.AssetClass
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.NumberUtil
import net.transitionmanager.domain.Project
import net.transitionmanager.service.CustomDomainService
import net.transitionmanager.service.SecurityService

/**
 * Provides helper functionality for the AssetEntity domain
 */
class AssetEntityHelper {

	// This is a list of properties that are referenced externally that are named differently
	// in the AssetEntity or Application domain classes appropriately
	private static Map PROPERTY_ALIASES = [ sme1:'sme', sme2:'sme2', owner:'appOwner']

	// TODO : JPM 1/2017 : remove the fixupHashtag TM-5894
	private static Map HASHTAG_FIXUP = [ '#sme':'#SME1', '#sme2':'#SME2', '#appOwner':'#Owner']

	/**
	 * Reverts a property name back to a messed up value that got hacked in
	 * the Application CRUD (see views/application/_bySelect.gsp)
	 * sme>SME1, sme2>SME2, appOwner>Owner
	 * TODO : JPM 1/2017 : remove the fixupHashtag TM-5894
	 * @param hashtag - the hashtag to check
	 * @return the fixed up hashtag or the original hashtag
	 */
	static String fixupHashtag(String hashtag) {
		( HASHTAG_FIXUP.containsKey(hashtag) ? HASHTAG_FIXUP[hashtag] : hashtag )
	}

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

		if (asset) {
			asset.customDomainService = ApplicationContextHolder.getBean('customDomainService', CustomDomainService)
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
	 * Used to get the actual property name of a domain object by the hashtag
	 * reference that which may be different from the actual property name
	 * @param asset - the AssetEntity (app, db, etc) instance used for looking up the property
	 * @param hashtag - the property to be looked up.
	 * @return the property name if it exists, null otherwise
	 */
	static String getPropertyNameByHashReference(AssetEntity asset, String hashtag){
		String result = null
		if (asset && hashtag) {
			// Strip off the has if necessary
			String propertyRef = (hashtag[0] == '#' ? hashtag.substring(1) : hashtag)

			// Check if the hash was one of the aliased references first
			result = PROPERTY_ALIASES[ propertyRef.toLowerCase() ]

			// If not, make sure that it is one of the properties in the domain class
			if (!result && GormUtil.isDomainProperty(asset, propertyRef)) {
				result = propertyRef
			}
		}
		return result
	 }


	/**
	 * Helper method lookup indirect property references that will recurse once if necessary
	 * This supports two situations:
	 *    1) taskSpec whom:'#prop' and asset.prop contains name/email
	 *    2) taskSpec whom:'#prop' and asset.prop contains #prop2 reference (indirect reference)
	 * @param asset - the Asset domain object to extract property references from
	 * @param propertyRef - the property name to begin indirect reference from that can be a #hashtag
	 * @param depth - controls the nested level of indirection, defaults to 3
	 * @return the value stored in the referenced or indirect referenced property
	 * @throws RuntimeException:
	 *    1. If a reference is made to an invalid property name
	 *    2. Nested indirect reference exceeds depth limit
	 */
	static Object getIndirectPropertyRef(Object asset, String propertyRef, int depth=3) {
		// println "getIndirectPropertyRef() property=$propertyRef, depth=$depth"

		def value
		String property = getPropertyNameByHashReference(asset, propertyRef)

		// Check to make sure that the asset has the field referenced
		if (property == null ) {
			throw new RuntimeException("Invalid property name ($propertyRef) used in name lookup in asset $asset")
		}

		// TODO : Need to see if we can eliminate the multiple if statements by determining the asset type upfront
		Class type = GormUtil.getDomainPropertyType(asset, property)
		if (type == java.lang.String) {
			// Check to see if we're referencing a person object vs a string
			// println "getIndirectPropertyRef() $property of type $type has value (${asset[property]})"
			if ( asset[property]?.size() && asset[property][0] == '#' ) {
				if (--depth > 0)  {
					value = getIndirectPropertyRef( asset, asset[property], depth)
				} else {
					throw new RuntimeException('Nested indirection limit exceeded')
				}
			} else {
				value = asset[property]
			}
		} else {
			// println "getIndirectPropertyRef() indirect references property $property of type $type"
			value = asset[property]
		}

		return value
	}
}
