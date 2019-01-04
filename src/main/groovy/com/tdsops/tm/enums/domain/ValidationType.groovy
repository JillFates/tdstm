package com.tdsops.tm.enums.domain

import groovy.transform.CompileStatic

@CompileStatic()
class ValidationType {
	static final String UNKNOWN    = 'Unknown'
	static final String VALIDATED  = 'Validated'
	static final String PLAN_READY = 'PlanReady'

	static final List<String> list = [UNKNOWN, VALIDATED, PLAN_READY].asImmutable()
}
