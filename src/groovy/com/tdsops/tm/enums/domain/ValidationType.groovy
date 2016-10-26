package com.tdsops.tm.enums.domain

import groovy.transform.CompileStatic

@CompileStatic()
class ValidationType {
	public static final String DIS = 'Discovery'
	public static final String VL  = 'Validated'
	public static final String DR  = 'DependencyReview'
	public static final String DS  = 'DependencyScan'
	public static final String BR  = 'BundleReady'

	static final List<String> list = [DIS, VL, DR, DS, BR].asImmutable()

	static final Map<String, String> listAsMap = [D: DIS, V: VL, R: DR, S: DS, B: BR].asImmutable()

	static final Map<String, String> valuesAsMap = [(DIS): 'D', (VL): 'V', (DS): 'S', (BR): 'B'].asImmutable()
}
