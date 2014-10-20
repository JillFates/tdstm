package com.tdsops.tm.enums.domain

import com.tdssrc.grails.EnumUtil
import com.tds.asset.AssetType
import com.tds.asset.Application
import com.tds.asset.AssetEntity
import com.tds.asset.Database
import com.tds.asset.Files

/**
 * Define all the possible classes of assets that are supported by TransitionManager
 */
enum AssetClass {
	APPLICATION,
	DATABASE,
	DEVICE, 
	STORAGE;

	static AssetClass safeValueOf(String key) {
		AssetClass obj
		try {
			obj = key as AssetClass
		} catch (e) { }
		return obj
	}

	/**
	 * Used to get the name of the name of the domain class associated with the AssetClass
	 * @param assetClass - the enum of the asset class
	 * @return the name of the GORM domain class
	 */
	static String domainNameFor(AssetClass assetClass) {
		def domain = domainClassFor(assetClass).name
		domain.split(/\./)[3]
	}

	/**
	 * Used to get the appropriate Domain Class for a specified asset class
	 * @param assetClass - the enum of the asset class
	 * @return the GORM domain class
	 */
	static Object domainClassFor(Object assetClass) {
		def domain
		def map = [
			(AssetClass.DEVICE) : AssetEntity,
			(AssetClass.DATABASE) : Database,
			(AssetClass.APPLICATION) : Application,
			(AssetClass.STORAGE) : Files
		]
		if (map.containsKey(assetClass)) {
			return map[assetClass]
		} else {
			throw RuntimeException("domainClassFor() Unhandled case for $assetClass")
		}
	}

	/**
	 * A list of the Asset Class Options used for filtering
	 * @return Map of names
	 */
	static Map getClassOptions() {
		TreeMap list = [
			'APPLICATION': 'Applications',
			'SERVER-DEVICE': 'Servers',
			'DATABASE': 'Databases',
			'NETWORK-DEVICE': 'Network Devices',
			// 'NETWORK-LOGICAL': 'Logical Network',
			'STORAGE-DEVICE': 'Storage Devices',
			'STORAGE-LOGICAL': 'Logical Storage',
			'OTHER-DEVICE': 'Other Devices',
		]
	}


	/** 
	 * Used to determine the Class Option based on the assetType
	 * @param type - the asset type
	 * @return the name of the class option
	 */
	static String getClassOptionForAsset(Object asset) {
		String option

		switch(asset?.assetClass) {
			case AssetClass.DEVICE:
				def type = asset.model?.assetType
				if (type) {
					if (AssetType.getServerTypes().contains(type)) {
						option = 'SERVER-DEVICE'
					} else if (AssetType.getStorageTypes().contains(type)) {
						option = 'STORAGE-DEVICE'
					} else if (AssetType.getNetworkDeviceTypes().contains(type)) {
						option = 'NETWORK-DEVICE'
					}
				}
				if (! option)
					option = 'OTHER-DEVICE'

				break

			case AssetClass.APPLICATION:
				option = 'APPLICATION'
				break

			case AssetClass.DATABASE:
				option = 'DATABASE'
				break

			case AssetClass.STORAGE:
				option = 'STORAGE-LOGICAL'
				break

			default:
				throw new RuntimeException("Unhandled switch statement for value ${asset.assetClass}")

		}

		return option
	}

}