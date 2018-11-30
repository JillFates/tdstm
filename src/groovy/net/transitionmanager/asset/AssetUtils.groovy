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
		}.order("value").readOnly(true).list()
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

	/**
	 * Return a list with all the possible values for the environment field.
	 * @return a list of string corresponding to the values for the environment field for assets.
	 */
	static List<String> getEnvironmentOptions() {
		return getAssetOptionsValues(AssetOptions.AssetOptionsType.ENVIRONMENT_OPTION)
	}

	/**
	 * Return a list with all the plan status options.
	 * @return a list of the plan status options available.
	 */
	static List<String> getPlanStatusOptions() {
		return getAssetOptionsValues(AssetOptions.AssetOptionsType.STATUS_OPTION)
	}

	/**
	 * Retrieve and return a list of dependency type options.
	 * @return the list of dependency type options.
	 */
	List<String> getDependencyTypeOptions() {
		return getAssetOptionsValues(AssetOptions.AssetOptionsType.DEPENDENCY_TYPE)
	}

	/**
	 * Retrieve and return a list of dependency status options.
	 * @return the list of dependency status options.
	 */
	List<String> getDependencyStatusOptions() {
		return getAssetOptionsValues(AssetOptions.AssetOptionsType.DEPENDENCY_STATUS)
	}

}
