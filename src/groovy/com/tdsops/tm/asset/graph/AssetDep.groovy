package com.tdsops.tm.asset.graph

/**
 * Stores a asset dependency reference
 */
class AssetDep {

	def status
	def type
	def depId

	/**
	 * Destroy method used to help GC
	 */
	def destroy() {
		status = null
		type = null
		depId = null
	}

}