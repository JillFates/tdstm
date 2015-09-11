package com.tdsops.common.security

import java.security.*;

class SHA2Codec {

	private static ALGORITHM = "SHA-256"
	private static SALT_KEY_SUFIX = "t45tm5vf1X"

	/**
	 * Used to hash a string value using SHA-256 algorithm
	 * @param String password to hash
	 * @param String salt_key_prefix salt used to hash the password
	 * @param String maxLength max length that can have the password
	 * @return String hashed password
	 */
	static String encode(String password, String salt_key_prefix=null, int maxLength=100) {
		MessageDigest digest = MessageDigest.getInstance(ALGORITHM);

		def bytesArray = digest.digest(createComposedPassword(password, salt_key_prefix));

		return toHex(bytesArray, maxLength)
	}

	/**
	 * Converts a byte[] to a hec string, limiting his length to maxLength
	 * @param String bytesArray array to convert
	 * @param String maxLength max length that can have the value
	 * @return String hex representation
	 */
	private static String toHex(byte[] bytesArray, int maxLength) {
		def result = SecurityUtil.toHex(bytesArray)
		if ((maxLength % 2) == 1) {
			maxLength--
		}
		if (result.size() > maxLength) {
			result = result.substring(0, maxLength)
		}
		return result
	}

	/**
	 * Look for th system salt key, if not found returns an empty value
	 * @return String system salt key
	 */
	private static String getSystemSaltKey() {
		def skp = System.properties["com.tdsops.tm.skp"]?:""
	}

	/**
	 * Unify password and salt key in one value
	 * @param String password
	 * @param String salt_key_prefix salt used to hash the password
	 * @return byte[] bytes representing the new value
	 */
	private static byte[] createComposedPassword(String password, String salt_key_prefix) {
		if (!salt_key_prefix) {
			salt_key_prefix = getSystemSaltKey()
		}
		def composedPassword = (SALT_KEY_SUFIX + password + salt_key_prefix)
		composedPassword.getBytes("UTF-8")
	}

}
