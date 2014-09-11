package com.tdsops.tm.enums.domain

import com.tdssrc.grails.EnumUtil
import com.tds.asset.AssetType

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