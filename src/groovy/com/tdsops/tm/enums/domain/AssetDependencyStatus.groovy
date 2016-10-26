package com.tdsops.tm.enums.domain

import groovy.transform.CompileStatic

/**
 * The valid options for the AssetDependancy.status property.
 */
@CompileStatic
class AssetDependencyStatus {

	public static final String VALIDATED  = 'Validated'
	public static final String NA         = 'Not Applicable'
	public static final String QUESTIONED = 'Questioned'
	public static final String UNKNOWN    = 'Unknown'
	public static final String ARCHIVED   = 'Archived'
	public static final String FUTURE     = 'Future'
	public static final String TESTING    = 'Testing'

	static final List<String> list = [UNKNOWN, QUESTIONED, VALIDATED, ARCHIVED, NA, FUTURE, TESTING].asImmutable()

	/**
	 * The codes that are considered needing to be reviewed.
	 */
	static final List<String> reviewCodes = [QUESTIONED, UNKNOWN].asImmutable()

	/**
	 * The codes that are considered needing to be reviewed, as a quote comma delimited string.
	 */
	public static final String reviewCodesAsString = "'" + QUESTIONED + "','" + UNKNOWN + "'"
}
