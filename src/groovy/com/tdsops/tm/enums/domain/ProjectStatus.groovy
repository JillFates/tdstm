package com.tdsops.tm.enums.domain

import com.tdssrc.grails.EnumUtil

/**
 * Define all the possible status that can have a project
 *  
 * @author Diego Scarpa <diego.scarpa@bairesdev.com>
 *
 */
enum ProjectStatus {
	ANY("any"), ACTIVE("active"), COMPLETED("completed");

	final String value

	ProjectStatus(String value) {
		this.value = value
	}

	String toString() {
		value
	}

	static ProjectStatus valueOfParam( String param ) {
		return EnumUtil.searchfParam(values(), param)
	}
}
