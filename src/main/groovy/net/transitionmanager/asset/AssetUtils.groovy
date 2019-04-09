package net.transitionmanager.asset


import com.tdsops.tm.enums.domain.AssetClass
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import net.transitionmanager.project.MoveBundle
import net.transitionmanager.project.Project

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
	static final ASSET_OPTION_TYPE_DEFAULT = AssetType.SERVER.toString()

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


	/**
	 * Return a map with the total number of assets for the given project or bundle.
	 * @param project - user's current project.
	 * @param moveBundle - bundle to narrow down the assets being queried.
	 * @param justPlanning - flag to filter only assets assigned to bundles used for planning.
	 * @return a map with the total broken down by type.
	 */
	static Map<String, Integer> getAssetSummary(Project project, MoveBundle moveBundle, boolean justPlanning) {
		return [
		    'applications': getApplicationCount(project, moveBundle, justPlanning),
			'databases': getDatabaseCount(project, moveBundle, justPlanning),
			'files': getFileCount(project, moveBundle, justPlanning),
			'physical': getPhysicalCount(project, moveBundle, justPlanning),
			'servers': getServerCount(project, moveBundle, justPlanning)
		]
	}

	/**
	 * Get the total number of databases for the given project (or bundle).
	 * @param project - user's current project.
	 * @param bundle - bundle to narrow down the assets being queried.
	 * @param justPlanning - flag to filter only assets assigned to bundles used for planning.
	 * @return the number of databases found.
	 */
	private static Integer getDatabaseCount(Project project, MoveBundle moveBundle, boolean justPlanning) {
		return getAssetCount(project, moveBundle, AssetClass.DATABASE, justPlanning)
	}

	/**
	 * Get the total number of applications for the given project (or bundle).
	 * @param project - user's current project.
	 * @param bundle - bundle to narrow down the assets being queried.
	 * @param justPlanning - flag to filter only assets assigned to bundles used for planning.
	 * @return the number of applications found.
	 */
	private static Integer getApplicationCount(Project project, MoveBundle moveBundle, boolean justPlanning) {
		return getAssetCount(project, moveBundle, AssetClass.APPLICATION, justPlanning)
	}

	/**
	 * Get the total number of files for the given project (or bundle).
	 * @param project - user's current project.
	 * @param bundle - bundle to narrow down the assets being queried.
	 * @param justPlanning - flag to filter only assets assigned to bundles used for planning.
	 * @return the number of files found.
	 */
	private static Integer getFileCount(Project project, MoveBundle moveBundle, boolean justPlanning) {
		return getAssetCount(project, moveBundle, AssetClass.STORAGE, justPlanning)
	}

	/**
	 * Get the total number of physical devices for the given project (or bundle).
	 * @param project - user's current project.
	 * @param bundle - bundle to narrow down the assets being queried.
	 * @param justPlanning - flag to filter only assets assigned to bundles used for planning.
	 * @return the number of physical devices found.
	 */
	private static Integer getPhysicalCount(Project project, MoveBundle bundle, boolean justPlanning) {
		if (justPlanning && ! bundle?.useForPlanning) {
			return 0
		} else {
			return AssetEntity.where {
				if (bundle) {
					moveBundle == bundle
				} else {
					project == project
				}
				assetClass == AssetClass.DEVICE
				assetType == null || !(assetType in AssetType.virtualServerTypes)

			}.count().toInteger()
		}
	}

	/**
	 * Get the total number of servers for the given project (or bundle).
	 * @param project - user's current project.
	 * @param bundle - bundle to narrow down the assets being queried.
	 * @param justPlanning - flag to filter only assets assigned to bundles used for planning.
	 * @return the number of servers found.
	 */
	private static Integer getServerCount(Project project, MoveBundle bundle, boolean justPlanning) {
		if (justPlanning && ! bundle?.useForPlanning) {
			return 0
		} else {
			return AssetEntity.where {
				if (bundle) {
					moveBundle == bundle
				} else {
					project == project
				}

				assetClass == AssetClass.DEVICE
				assetType in AssetType.serverTypes

			}.count().toInteger()
		}
	}


	/**
	 * Get the total number of assets for the AssetClass given. If a MoveBundle instance is provided,
	 * only assets assigned to that bundle will be considered.
	 *
	 * @param project - user's current project.
	 * @param bundle - MoveBundle to be used for querying the asset count.
	 * @param assetClass - the AssetClass requested.
	 * @param justPlanning - flag to filter planning assets.
	 * @return an integer with the number of assets for the bundle.
	 */
	private static Integer getAssetCount(Project project, MoveBundle bundle, AssetClass assetClass, boolean justPlanning) {
		if (justPlanning && ! bundle?.useForPlanning) {
			return 0
		} else {
			return AssetEntity.where {
				// Check if a bundle was passed. If so, use it. If not, use the project.
				if (bundle) {
					moveBundle == bundle
				} else {
					project == project
				}

				assetClass == assetClass

			}.count().toInteger()
		}
	}

}
