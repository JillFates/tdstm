package com.tdsops.common.security

/**
 * Utility class containing general methods to aid in security functionality
 */
class SecurityUtil {

	/**
	 * Helper method that converts a binary GUID to a string
	 * @param guid a binary string value
	 * @return The guid converted to a hex string
	 */
	static String guidToString( guid ) {
		def addLeadingZero = { k -> 
			return (k <= 0xF ? '0' + Integer.toHexString(k) : Integer.toHexString(k))
		}

		// Where GUID is a byte array returned by a previous LDAP search
		// guid.each { b -> log.info "b is ${b.class}"; str += addLeadingZero( (int)b & 0xFF ) }
		StringBuffer str = new StringBuffer()
		for (int c=0; c < guid.size(); c++) {
			Integer digit = guid.charAt(c)
			str.append( addLeadingZero( digit & 0xFF ) )
		}
		return str
	}

}