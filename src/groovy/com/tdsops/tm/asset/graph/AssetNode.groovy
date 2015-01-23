package com.tdsops.tm.asset.graph

/**
 * Stores a asset information and his dependencies
 */
class AssetNode {

	def id
	def moveBundleId
	def deps = []
	boolean checked = false

	/**
	 * Destroy method used to help GC
	 */
	def destroy() {
		id = null
		moveBundleId = null
		deps.each {
			it.destroy()
		}
		deps = null
	}

}