package com.tdsops

import net.transitionmanager.asset.AssetOptions

/**
 * <p>This class is a cache for the valid AssetDependency types defined in system</p>
 * <p>It used by {@code DependencyBuilder} to validate if a AssetDependency type is valid or not</p>
 * <p>In the following example AssetDependency type 'Runs on' must be validated using this cache implementation</p>
 * <pre>
 * 	iterate {
 * 		domain Application
 * 		...
 * 		set assetResultVar with DOMAIN
 *
 * 	    domain Device
 * 	    ...
 * 	    set dependentResult with DOMAIN
 *
 * 		// Here's the cool stuff
 * 		domain Dependency with assetResultVar 'Runs on' dependentResult
 * 		...
 *} </pre>
 * @see AssetDependencyTypesCache#isValidType(java.lang.String)
 */
class AssetDependencyTypesCache {

	List<String> assetDependencyTypesCache

	/**
	 * Initializes the cache used to validate if an {@code AssetDependency # type}
	 * is valid or not
	 */
	AssetDependencyTypesCache() {
		assetDependencyTypesCache = AssetOptions.where {
			type == AssetOptions.AssetOptionsType.DEPENDENCY_TYPE
		}.projections {
			property 'value'
		}.list()
	}

	/**
	 * Check if a dependencyType is a valid {@code AssetDependency # type}
	 * @param dependencyType
	 * @return true is dependencyType is a valid {@code AssetDependency # type}
	 * 			otherwise returns null
	 */
	boolean isValidType(String dependencyType) {
		return dependencyType in assetDependencyTypesCache
	}
}
