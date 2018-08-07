package com.tdsops.tm.enums.domain

import com.tdssrc.grails.EnumUtil
import groovy.transform.CompileStatic

@CompileStatic
enum ApiActionHttpMethod {
	GET, POST, PUT, PATCH, DELETE, OPTIONS, HEAD

	/**
	 * Get enum names
	 * @return a list of http method names
	 */
	static List<String> names() {
		List<String> names = new ArrayList<>()
		for (ApiActionHttpMethod e : values()) {
			names.add(e.name())
		}
		return names
	}

	/**
	 * Check if method is a valid ApiActionHttpMethod entry
	 * @param method - the enum http method name, null returns false
	 * @return
	 */
	static boolean isValidHttpMethod(String method) {
		return EnumUtil.isValidEnum(ApiActionHttpMethod.class, method)
	}
}
