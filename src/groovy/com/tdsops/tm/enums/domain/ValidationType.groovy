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
	static final getListAsMap() {
		return [ D:DIS, V:VL, R:DR, S:DS, B:BR  ]
	}
	static final getValuesAsMap() {
		return [ Discovery:'D', Validated:'V', DependencyReview:'R', DependencyScan:'S', BundleReady:'B'  ]
	}
}
