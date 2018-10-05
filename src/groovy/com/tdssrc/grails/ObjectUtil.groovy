package com.tdssrc.grails

/**
 * The ObjectUtil class contains a collection of useful object manipulation methods.
 * Utility methods applicable to (almost) any object.
 */
class ObjectUtil {

	/**
	 * Checks if an {@code Object} instance is null and
	 * @param object
	 * @return
	 */
	static boolean isNotNullOrBlankString(Object object) {
		return object != null || (object instanceof CharSequence && StringUtil.isNotBlank(object))
	}


}
