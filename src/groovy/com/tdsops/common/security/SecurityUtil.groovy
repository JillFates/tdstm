package com.tdsops.common.security

import com.tdsops.common.grails.ApplicationContextHolder
import com.tdsops.common.security.spring.TdsUserDetails
import org.springframework.security.authentication.AuthenticationTrustResolver
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.codec.Hex

import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Utility class containing general methods to aid in security functionality.
 *
 * Note that all references to encryption or decryption actually involve hashing, not encryption.
 */
class SecurityUtil {
	public static final String ACCOUNT_LOCKED_OUT = "accountLockedOut"

	private static SecureRandom randomGenerator = new SecureRandom()

	/**
	 * Helper method that converts a binary GUID to a string
	 * @param guid a binary string value
	 * @return The guid converted to a hex string
	 */
	static String guidToString(String guid) {
		// Where GUID is a byte array returned by a previous LDAP search
		// guid.each { b -> log.info "b is ${b.class}"; str += addLeadingZero( (int)b & 0xFF ) }
		StringBuilder str = new StringBuilder()
		for (int c = 0; c < guid.size(); c++) {
			appendWithLeadingZero guid.charAt(c), str
		}
		return str
	}

	private static void appendWithLeadingZero(char c, StringBuilder sb) {
		int k = (int)c & 0xFF
		if (k <= 0xF) {
			sb << '0'
		}
		sb << Integer.toHexString(k)
	}

	/**
	 * Hashes a clear text password - using legacy SHA
	 * @param text  the cleartext password
	 * @return the hashed password
	 */
	static String encryptLegacy(String text) {
		new String(Hex.encode(MessageDigest.getInstance('SHA1').digest(text.bytes)))
	}

	/**
	 * Hashes a clear text password - using SHA2
	 * @param text  the cleartext password
	 * @param salt_key  the salt to use to hash the password
	 * @return the hashed password
	 */
	static String encrypt(String text, String salt_key=null) {
		return SHA2Codec.encode(text, salt_key)
	}

	/**
	 * Creates a random string
	 */
	static String randomString(int len) {
		return new BigInteger(len * 8, randomGenerator).toString(16).substring(0, len)
	}

	// TODO BB clean these up

	static boolean isLoggedIn() {
		Authentication authentication = SecurityContextHolder.context?.authentication
		AuthenticationTrustResolver atr = ApplicationContextHolder.getBean('authenticationTrustResolver', AuthenticationTrustResolver)
		authentication && !atr.isAnonymous(authentication)
	}

	static TdsUserDetails getPrincipal() {
		isLoggedIn() ? (TdsUserDetails) SecurityContextHolder.context?.authentication?.principal : null
	}
}
