package com.tdsops

import com.tds.asset.AssetOptions

class AssetDependencyTypesCache {

	List<String> assetDependencyTypesCache

	/**
	 * Initializes the cache used to validate if an {@code AssetDependency#type}
	 * is valid or not
	 */
	AssetDependencyTypesCache(){
		// Validate methodName as a Dependency Type
		assetDependencyTypesCache = AssetOptions.where {
			type == AssetOptions.AssetOptionsType.DEPENDENCY_TYPE
		}.projections {
			property 'value'
		}.list()
	}

	/**
	 * Check if a dependencyType is a valid {@code AssetDependency#type}
	 * @param dependencyType
	 * @return true is dependencyType is a valid {@code AssetDependency#type}
	 * 			otherwise returns null
	 */
	boolean isValidType(String dependencyType){
		return dependencyType in assetDependencyTypesCache
	}
}
