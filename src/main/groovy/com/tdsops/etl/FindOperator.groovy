package com.tdsops.etl

import groovy.transform.CompileStatic

/**
 * Defines an operator for find command
 */
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

	/**
	 * Lookups a String value inside the {@code FindOperator} enum values.
	 * In case of found it, it returns an instance of {@code FindOperator}.
	 * If It didn't find the value, it throws an {@code an ETLProcessorException}
	 *
	 * @param name
	 * @return a {@code FindOperator} enum value
	 * @see ETLProcessorException#unrecognizedFindCriteria(java.lang.String)
	 * //TODO: dcorrea, 08/11/2018. I can not override valueOf method. That's why I called this 'lookup'
	 */
	static FindOperator lookup(String name) {

		if (name == null)
			throw new NullPointerException("Name is null")

		FindOperator result = FindOperator.enumConstantDirectory().get(name)
		if (result != null)
			return result

		throw ETLProcessorException.unrecognizedFindCriteria(name)
	}
}
