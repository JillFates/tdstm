package net.transitionmanager.asset

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import com.tds.asset.AssetOptions

@CompileStatic
class AssetUtils {

	/*
	 * Default value for AssetDependency.status field
	 */
	static final ASSET_OPTION_DEPENDENCY_STATUS_DEFAULT = 'Unknown'

	/*
	 * Default value for AssetDependency.type field
	 */
	static final ASSET_OPTION_DEPENDENCY_TYPE_DEFAULT = 'Unknown'

	/*
	 * Default value for AssetEntity.priority field
	 */
	static final ASSET_OPTION_PRIORITY_DEFAULT = '3'

	/*
	 * Default value for AssetEntity.status field
	 */
	static final ASSET_OPTION_STATUS_DEFAULT = com.tdsops.tm.enums.domain.AssetEntityPlanStatus.UNASSIGNED

	/*
	 * Default value for AssetEntity.environment field
	 */
	static final ASSET_OPTION_ENVIRONMENT_DEFAULT = 'Unknown'

	/*
	 * Default value for AssetEntity.assetType field
	 */
	static final ASSET_OPTION_TYPE_DEFAULT = com.tds.asset.AssetType.SERVER.toString()

	/**
	 * Create and return a list with the values for all the AssetOptions with the given type.
	 * @param assetOptionsType - type of options
	 * @return a list with only the value for each AssetOptions found.
	 */
	@CompileStatic(TypeCheckingMode.SKIP)
	static List<String> getAssetOptionsValues(AssetOptions.AssetOptionsType assetOptionsType) {
		return AssetOptions.where {
			type == assetOptionsType
		}.projections {
			property('value')
		}.order("value").readOnly(true).list()
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
	static List<String> getDependencyTypeOptions() {
		return getAssetOptionsValues(AssetOptions.AssetOptionsType.DEPENDENCY_TYPE)
	}

	/**
	 * Retrieve and return a list of dependency status options.
	 * @return the list of dependency status options.
	 */
	static List<String> getDependencyStatusOptions() {
		return getAssetOptionsValues(AssetOptions.AssetOptionsType.DEPENDENCY_STATUS)
	}

	/**
	 * Used to find the matching AssetOption value case using a case insensitive search
	 * @param value - the value to lookup
	 * @param assetOptions - the list of AssetOption codes
	 * @param defaultWhenNotFound
	 * @return the case sensitive code when found otherwise the defaultWhenNotFound value
	 */
	@CompileStatic
	static String matchAssetOptionCaseInsensitive(String value, List<String> assetOptions, String defaultWhenNotFound = null) {
		for (String option in assetOptions) {
			if (option.equalsIgnoreCase(value)) {
				return option
			}
		}
		// If no existing option matched the given value, return 'Unknown'.
		return defaultWhenNotFound
	}

}
