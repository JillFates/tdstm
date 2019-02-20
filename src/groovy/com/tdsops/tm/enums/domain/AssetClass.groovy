package com.tdsops.tm.enums.domain

import com.tdssrc.grails.StringUtil
import com.tds.asset.Application
import com.tds.asset.AssetEntity
import com.tds.asset.AssetType
import com.tds.asset.Database
import com.tds.asset.Files
import com.tdssrc.grails.StringUtil
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

/**
 * Defines all the possible classes of assets that are supported by TransitionManager.
 */
@CompileStatic
enum AssetClass {

	APPLICATION(Application),
	DATABASE(Database),
	DEVICE(AssetEntity),
	STORAGE(Files)

	/**
	 * The Asset Class Options used for filtering
	 */
	static final Map<String, String> classOptions = ([
			'APPLICATION'    : 'Applications',
			'SERVER-DEVICE'  : 'Servers',
			'DATABASE'       : 'Databases',
			'NETWORK-DEVICE' : 'Network Devices',
		// 'NETWORK-LOGICAL': 'Logical Network',
			'STORAGE-DEVICE' : 'Storage Devices',
			'STORAGE-LOGICAL': 'Logical Storage',
			'OTHER-DEVICE'   : 'Other Devices'
	] as TreeMap).asImmutable()

	private final Class domainClass

	private AssetClass(Class clazz) {
		domainClass = clazz
	}

	static AssetClass safeValueOf(String key) {
		values().find { it.name() == key }
	}

	/**
	 * The name of the name of the domain class associated with the AssetClass
	 * @param assetClass  the enum of the asset class
	 * @return the name of the GORM domain class
	 */
	static String domainNameFor(AssetClass assetClass) {
		domainClassFor(assetClass).name.split(/\./)[3]
	}

	/**
	 * Get the appropriate Domain Class for a specified asset class
	 * @param assetClass - the enum of the asset class
	 * @return the GORM domain class
	 */
	static Class domainClassFor(AssetClass assetClass) {
		assetClass.domainClass
	}

	/**
	 * The Asset Class Options used for filtering
	 */
	static final String classOptionsDefinition
	static {
		List<String> results = ["{ id: 'ALL', 'text': 'Filter: All Classes' }"]
		for (String key in classOptions.keySet()) {
			results.push("{ id: '$key', 'text': '${classOptions[key]}' }".toString())
		}
		classOptionsDefinition = results
	}

	/**
	 * Determine the Class Option based on the assetType
	 * @param type - the asset type
	 * @return the name of the class option
	 */
	@CompileDynamic
	static String getClassOptionForAsset(asset) {
		getClassOptionForAsset(asset?.assetClass, asset?.assetType)
	}

	/**
	 * Used to determine the Class Option based on the assetType
	 * @param assetClass - the asset class
	 * @param assetType - the asset type
	 * @return the name of the class option
	 */
	static String getClassOptionForAsset(AssetClass assetClass, String assetType) {
		switch (assetClass) {
			case APPLICATION: return 'APPLICATION'
			case DATABASE:    return 'DATABASE'
			case STORAGE:     return 'STORAGE-LOGICAL'
			case DEVICE:
				if (assetType) {
					if (AssetType.serverTypes.contains(assetType))        return 'SERVER-DEVICE'
					if (AssetType.storageTypes.contains(assetType))       return 'STORAGE-DEVICE'
					if (AssetType.networkDeviceTypes.contains(assetType)) return 'NETWORK-DEVICE'
				}
				return 'OTHER-DEVICE'
		}
	}

	/**
	 * This method determines the domain for a given Asset Type.
	 * Presently, the only domains supported are:
	 *	- Application
	 *	- Storage
	 *	- Database
	 *	- Device
	 * @param assetType : string with the given asset type
	 * @return domain
	 */
	static String getDomainForAssetType(String assetType) {
		String domain
		if(!StringUtil.isBlank(assetType)){
			assetType = assetType.toUpperCase()
			switch (assetType) {
				case "APPLICATION":
					domain = "Application"
					break
				case "FILES":
					domain = "Storage"
					break
				case "DATABASE":
					domain = "Database"
					break
				case "ASSETENTITY":
					domain = "Device"
					break
			}
		}
		return domain
	}

	/**
	 * Prepares an static Map for look up AssetClass by Domain Class
	 */
	private static final Map<Class<? extends AssetEntity>, AssetClass> assetClassByDomainClassMap = [:]

	/**
	 * Initialize Map.
	 */
	static {
		AssetClass.values().each { AssetClass clazz ->
			assetClassByDomainClassMap[clazz.domainClass] = clazz
		}
	}

	/**
	 * Lookups an AssetClass by its domain class
	 * @param clazz a Class in the AssetEntity Hierarchy
	 * @return an AssetClass instance or null if there is not a class mapped
	 */
	static lookup(Class<? extends AssetEntity> clazz){
		return assetClassByDomainClassMap[clazz]
	}

}
