package com.tdsops.common.security

import org.apache.shiro.crypto.hash.Sha1Hash
import java.security.SecureRandom
import com.tdssrc.grails.TimeUtil
import org.apache.commons.codec.binary.Hex

/**
 * Utility class containing general methods to aid in security functionality
 */
class SecurityUtil {

	private static SecureRandom randomGenerator = new SecureRandom()

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

	/** 
	 * Encrypts a clear text password - using legacy SHA
	 * @param String password
	 * @return String Encripted passsword
	 */
	public static String encryptLegacy(String text) {
		def etext = new Sha1Hash(text).toHex()
		return etext.toString()
	}

	/** 
	 * Apply the new password to the userLogin, encrypt it and update 
	 * @param userLogin userLogin to apply the new password
	 * @param password new password to use
	 */
// TODO : 9/1/2015 : Remove references to applyPassword
	public static applyPassword(userLogin, String password) {
		throw new RuntimeException("replace with UserLogin")
	}

	/** 
	 * Encrypts a clear text password - using SHA2
	 * @param String password
	 * @param String salt_key to use to hash the password
	 * @return String Encrypted passsword
	 */
	public static String encrypt(String text, String salt_key=null) {
		return SHA2Codec.encode(text, salt_key)
	}

	/** 
	 * Encrypts a clear text password - using AES
	 * @param String password
	 * @return String Encrypted passsword
	 */
	public static String encryptAES(String text) {
		return toHex(AESCodec.encode(text).bytes)
	}

	/**
	 * Used to convert a byte[] to a hex string
	 */
	private static String toHex(byte[] bytesArray) {
		return Hex.encodeHexString(bytesArray)
	}

	/**
	 * Creates a random string
	 */
	public static String randomString(int len) {
		return new BigInteger(130, randomGenerator).toString(16).substring(0, len)
	}

}