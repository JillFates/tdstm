package com.tdssrc.grails

/**
 * The ObjectUtil class contains a collection of useful object manipulation methods.
 * Utility methods applicable to (almost) any object.
 */
class ObjectUtil {

    /**
     * Checks if an {@code Object} instance is null of if it is not null and it is a String value and it is not empty.
     * @param object
     * @return
     */
    static boolean isNotNullOrBlankString(Object object) {
        if (object != null && object instanceof CharSequence) {
            return StringUtil.isNotBlank(object)
        } else {
            return object != null
        }
    }


}
