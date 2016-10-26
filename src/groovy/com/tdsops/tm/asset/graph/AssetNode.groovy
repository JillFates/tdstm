package com.tdsops.tm.asset.graph

import groovy.transform.CompileStatic

/**
 * Stores asset information and its dependencies.
 */
@CompileStatic
class AssetNode {

	Long id
	Long moveBundleId
	List<AssetDep> deps = []
	String assetType
	boolean checked = false

	/**
	 * Help GC
	 */
	void destroy() {
		id = null
		moveBundleId = null
		assetType = null
		deps*.destroy()
		deps = null
	}
}
