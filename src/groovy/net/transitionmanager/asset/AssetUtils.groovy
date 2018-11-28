package net.transitionmanager.asset

import com.tds.asset.AssetOptions

class AssetUtils {

	/**
	 * Return the Asset Options for the given type.
	 * @param assetOptionsType - type of options requested.
	 * @return  a list with all the AssetOptions for the type passed as argument.
	 */
	static List<AssetOptions> getAssetOptions(AssetOptions.AssetOptionsType assetOptionsType) {
		return AssetOptions.where {
			type == assetOptionsType
		}.order("value").list()
	}

	/**
	 * Create and return a list with the values for all the AssetOptions with the given type.
	 * @param assetOptionsType - type of options
	 * @return a list with only the value for each AssetOptions found.
	 */
	static List<String> getAssetOptionsValues(AssetOptions.AssetOptionsType assetOptionsType) {
		List<AssetOptions.AssetOptionsType> assetOptions = getAssetOptions(assetOptionsType)
		List<String> assetOptionsValues = null
		if (assetOptions) {
			assetOptionsValues = assetOptions.value
		}
		return assetOptionsValues
	}
}
