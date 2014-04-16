package com.tdsops.tm.enums.domain;

import com.tdssrc.grails.EnumUtil

/**
 * Define all the fields that can be used to sort a project
 * 
 * @author Diego Scarpa <diego.scarpa@bairesdev.com>
 *
 */
enum ProjectSortProperty {
	PROJECT_CODE("projectCode"), NAME("name"), COMMENT("comment"), START_DATE("startDate"), COMPLETION_DATE("completionDate");

	final String value

	ProjectSortProperty(String value) {
		this.value = value
	}

	String toString() {
		value
	}

	static ProjectSortProperty valueOfParam( String param ) {
		return EnumUtil.searchfParam(values(), param)
	}
}
