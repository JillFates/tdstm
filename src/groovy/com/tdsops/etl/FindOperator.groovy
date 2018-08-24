package com.tdsops.etl

import groovy.transform.CompileStatic

/**
 * Defines an operator for find command
 */
@CompileStatic
enum FindOperator {

	eq,
	ne,
	nseq,
	lt,
	le,
	gt,
	ge,
	like,
	notLike,
	contains,
	notContains,
	inList,
	notInList,
	between,
	notBetween,
	isNull,    /* is  null */
	isNotNull  /* not null */
}
