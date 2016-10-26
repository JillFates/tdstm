package com.tdsops.tm.enums.domain

import groovy.transform.CompileStatic

/**
 * Define all the fields that can be used to sort a project
 *
 * @author Diego Scarpa <diego.scarpa@bairesdev.com>
 */
@CompileStatic
enum ProjectSortProperty {

	PROJECT_CODE('projectCode'),
	NAME('name'),
	COMMENT('comment'),
	START_DATE('startDate'),
	COMPLETION_DATE('completionDate')

	final String value

	private ProjectSortProperty(String value) {
		this.value = value
	}

	String toString() { value }

	static ProjectSortProperty valueOfParam(String param) {
		values().find { it.value == param }
	}
}
