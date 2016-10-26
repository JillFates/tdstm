package com.tdsops.tm.enums.domain

import com.tdssrc.grails.EnumUtil
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

	static ProjectStatus valueOfParam(String param) {
		values().find { it.value == param }
	}
}
