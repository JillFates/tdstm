package com.tdsops.tm.enums.domain;

import com.tdssrc.grails.EnumUtil

/**
 * Define all sort types.
 * 
 * @author Diego Scarpa <diego.scarpa@bairesdev.com>
 *
 */
enum SortOrder {
	ASC("asc"), DESC("desc");

	final String value

	SortOrder(String value) {
		this.value = value
	}

	String toString() {
		value
	}

	static SortOrder valueOfParam( String param ) {
		return EnumUtil.searchfParam(values(), param)
	}
}
