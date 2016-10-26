package com.tdsops.tm.enums.domain

import com.tds.asset.Application
import com.tds.asset.AssetEntity
import com.tds.asset.AssetType
import com.tds.asset.Database
import com.tds.asset.Files
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
		getClassOptionForAsset(asset?.assetClass, asset?.model?.assetType)
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
}
