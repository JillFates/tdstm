package com.tdsops.tm.enums.domain

import groovy.transform.CompileStatic
/**
 * Define all the possible status that can have a project
 *
 * @author Diego Scarpa <diego.scarpa@bairesdev.com>
 */
@CompileStatic
enum ProjectStatus {

	ANY('any'),
	ACTIVE('active'),
	COMPLETED('completed')

	final String value

	private ProjectStatus(String value) {
		this.value = value
	}

	String toString() { value }

	/**
	 * Safely return the corresponding Enum constant
	 * @param value
	 * @return
	 */
	static ProjectStatus lookup(String value) {
		ProjectStatus projectStatus
		try {
			projectStatus = ProjectStatus.valueOf(value.toUpperCase())
		} catch (e) {}
		return projectStatus
	}

}
