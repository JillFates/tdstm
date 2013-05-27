package com.tdsops.tm.enums.domain

/**
 * 
 * @author 
 *
 */
class ValidationType {
	static final String DIS='Discovery'
	static final String VL='Validated'
	static final String DR='DependencyReview'
	static final String DS='DependencyScan'
	static final String BR='BundleReady'
	static final getList() {
		return [ DIS, VL, DR, DS, BR ]
	}
}
