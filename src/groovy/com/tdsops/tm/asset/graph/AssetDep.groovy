package com.tdsops.tm.asset.graph

import groovy.transform.CompileStatic

/**
 * Stores a asset dependency reference.
 */
@CompileStatic
class AssetDep {

	def status
	def type
	def depId

	/**
	 * Destroy method used to help GC
	 */
	void destroy() {
		status = null
		type = null
		depId = null
	}
}
