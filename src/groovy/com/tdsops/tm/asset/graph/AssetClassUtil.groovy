package com.tdsops.tm.asset.graph

import com.tds.asset.AssetType
import com.tdsops.tm.enums.domain.AssetClass

/**
 * Contains utility functions for Assets
 */
class AssetClassUtil {

	/**
	 * Gets the image type string for an asset given its type and class
	 */
	def getImageName (String clazz, String type) {
		if (clazz == AssetClass.APPLICATION.toString()) {
			return AssetType.APPLICATION.toString()
		} else if (clazz == AssetClass.DATABASE.toString()) {
			return AssetType.DATABASE.toString()
		} else if (clazz == AssetClass.DEVICE.toString() && type in AssetType.getVirtualServerTypes()) {
			return AssetType.VM.toString()
		} else if (clazz == AssetClass.DEVICE.toString() && type in AssetType.getPhysicalServerTypes()) {
			return AssetType.SERVER.toString()
		} else if (clazz == AssetClass.STORAGE.toString()) {
			return AssetType.FILES.toString()
		} else if (clazz == AssetClass.DEVICE.toString() && type in AssetType.getStorageTypes()) {
			return AssetType.STORAGE.toString()
		} else if (clazz == AssetClass.DEVICE.toString() && type in AssetType.getNetworkDeviceTypes()) {
			return AssetType.NETWORK.toString()
		} else {
			return 'Other'
		}
	}
}
