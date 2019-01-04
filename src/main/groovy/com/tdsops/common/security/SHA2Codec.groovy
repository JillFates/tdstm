package com.tdsops.common.security

import org.apache.commons.codec.binary.Hex

import java.security.MessageDigest

class SHA2Codec {

	private static ALGORITHM = "SHA-256"
	private static SALT_KEY_SUFIX = "t45tm5vf1X"

	/**
	 * Hash a string value using SHA-256 algorithm
	 * @param String password to hash
	 * @param String salt_key_prefix salt used to hash the password
	 * @param String maxLength max length that can have the password
	 * @return String hashed password
	 */
	static String encode(String password, String salt_key_prefix=null, int maxLength=100) {
		MessageDigest digest = MessageDigest.getInstance(ALGORITHM)

		byte[] composedPassword = (SALT_KEY_SUFIX + password + salt_key_prefix ?: getSystemSaltKey()).getBytes("UTF-8")
		def bytesArray = digest.digest(composedPassword)

		String result = Hex.encodeHexString(bytesArray)
		if ((maxLength % 2) == 1) {
			maxLength--
		}
		if (result.size() > maxLength) {
			result.substring(0, maxLength)
		}
		else {
			result
		}
	}

	/**
	 * Look for the system salt key, if not found returns an empty value
	 * @return system salt key
	 */
	private static String getSystemSaltKey() {
		System.getProperty 'com.tdsops.tm.skp', ''
	}
}
