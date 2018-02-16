package com.tdsops.common.security

import groovy.transform.CompileStatic
import net.transitionmanager.service.InvalidConfigurationException
import org.apache.commons.codec.binary.Hex

import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import java.security.AlgorithmParameters
import java.security.SecureRandom
import java.security.spec.KeySpec
import java.util.regex.Matcher

@CompileStatic
class AESCodec {
	private static final int SALT_LEN = 16
	private static String ENCRYPTION_SALT_PREFIX = null
	private static final String SALT_PREFIX_CONFIG_NAME = 'com.tdsops.tm.skp'
	private static final String SALT_KEY_SUFFIX = 's@lt_k3y_suff1x'
	private static final String SALT_PADDING = 'rnu8FkDMG8jhH7EgfCV5krmgE9ZjDKRp'
	private static final int MINIMAL_SECRET_KEY_LENGTH = (int) (KEYLEN_BITS / 8)
	private static final int ITERATION_COUNT = 65536

	/*
	* In order to do 256 bit crypto, you have to muck with the files for Java's "unlimited security"
	* The end user must also install them (not compiled in) so beware.
	* see here:  http://www.javamex.com/tutorials/cryptography/unrestricted_policy_files.shtml
	*/
	private static final int KEYLEN_BITS = 256

	private static final AESCodec INSTANCE = new AESCodec()

	private AESCodec() {
		ENCRYPTION_SALT_PREFIX = getSystemSaltKey()
	}

	static AESCodec getInstance() {
		return INSTANCE
	}

	String generateRandomSalt() {
		byte[] salt = new byte [SALT_LEN]
		SecureRandom secureRandom = new SecureRandom()
		secureRandom.nextBytes(salt)
		return Hex.encodeHexString(salt)
	}

	String encode(String value, String salt) {
		return encode(value, getSecretKey(salt), salt)
	}

	String encode(String value, String secretKey, String salt) {
		return getEncodedValue(value, secretKey, salt)
	}

	private String getEncodedValue(String value, String secretKey, String salt) {
//		Cipher cipher = getCipher(Cipher.ENCRYPT_MODE, secretKey, salt)
		Cipher cipher = getCipherWithBlockChaining(Cipher.ENCRYPT_MODE, secretKey, salt, null)
		AlgorithmParameters params = cipher.getParameters()
		String encodedValue = cipher.doFinal(value.bytes).encodeBase64().toString()

		if (params) {
			// get the initialization vector
			byte[] initializationVector = params.getParameterSpec(IvParameterSpec.class).getIV()

			// return encrypted value with initialization vector appended
			return '{' + initializationVector.encodeHex() + '}' + encodedValue
		} else {
			// return simple encrypted value
			return encodedValue
		}
	}

	String decode(String value, String salt) {
		return decode(value, getSecretKey(salt), salt)
	}

	String decode(String value, String secretKey, String salt) {
		getDecodedValue(value, secretKey, salt)
	}


	private String getDecodedValue(String value, String secretKey, String salt) {
//		Cipher cipher = getCipher(Cipher.DECRYPT_MODE, secretKey, salt)
		Cipher cipher = null
		String encryptedPassword = value

		// if value matches initialization vector and encrypted password pattern
		// {HEX}base64
		// then use cipher with initialization vector
		if (value ==~ /^\{[A-Fa-f0-9]{32}\}.*$/) {
			String[] encodedText = value.split("(?<=^\\{[A-Fa-f0-9]{32}\\})(?=.*)")
			encryptedPassword = encodedText[1]
			Matcher initializationVectorMatcher = encodedText[0] =~ /([A-Fa-f0-9]{32})/
			String[] initializationVectorMatch = initializationVectorMatcher[0] as String[]
			byte[] initializationVector = initializationVectorMatch[0].decodeHex()
			cipher = getCipherWithBlockChaining(Cipher.DECRYPT_MODE, secretKey, salt, initializationVector)
		} else {
			// else, decrypt simple value
			cipher = getCipher(Cipher.DECRYPT_MODE, secretKey, salt)
		}

//		Cipher cipher = getCipherWithBlockChaining(Cipher.DECRYPT_MODE, secretKey, salt)
		return new String(cipher.doFinal(encryptedPassword.decodeBase64()))
	}

	private String getSystemSaltKey() {
		String encryptionSaltPrefix = System.getProperty(SALT_PREFIX_CONFIG_NAME)
		if (!encryptionSaltPrefix) {
			throw new InvalidConfigurationException("Property $SALT_PREFIX_CONFIG_NAME needed for encryption was not found")
		}
		return encryptionSaltPrefix
	}

	private String getSecretKey(String salt) {
		return getSecretKey(ENCRYPTION_SALT_PREFIX, salt, SALT_KEY_SUFFIX)
	}

	private String getSecretKey(String prefix, String salt, String suffix) {
		String secretKey = prefix + salt + suffix
		String secretKeyWithPadding = secretKey.padRight(MINIMAL_SECRET_KEY_LENGTH, SALT_PADDING)

		return secretKeyWithPadding
	}

	// old way
	private byte[] getPassword(String secretKey) {
		if (secretKey.size() > 16) {
			secretKey = secretKey[0..(KEYLEN_BITS / 8) - 1]
		}
		secretKey.bytes
	}

	// old way
	private SecretKey deriveSecretKey(String secretKey) {
		// Derive the key, given secretKey
		SecretKey secret = new SecretKeySpec(getPassword(secretKey), 'AES')

		return secret
	}

	// old way
	private Cipher getCipher(int mode, String secretKey, String salt) {
		// Create the cipher object
		Cipher cipher = Cipher.getInstance('AES')
		cipher.init(mode, deriveSecretKey(secretKey))
		return cipher
	}

	// new way using block-chaining
	private SecretKey deriveSecretKey(String secretKey, String salt) {
		// Derive the key, given secretKey and salt
		SecretKeyFactory factory = SecretKeyFactory.getInstance('PBKDF2WithHmacSHA256')
		KeySpec spec = new PBEKeySpec(secretKey.toCharArray(), salt.bytes, ITERATION_COUNT, KEYLEN_BITS)
		SecretKey tmp = factory.generateSecret(spec)
		SecretKey secret = new SecretKeySpec(tmp.getEncoded(), 'AES')

		return secret
	}

	// new way using block-chaining
	private Cipher getCipherWithBlockChaining(int mode, String secretKey, String salt, byte[] initializationVector) {
		// Create the cipher object using block-chaining
		Cipher cipher = Cipher.getInstance('AES/CBC/PKCS5Padding')
		if (Cipher.ENCRYPT_MODE == mode) {
			cipher.init(mode, deriveSecretKey(secretKey, salt))
		} else {
			cipher.init(mode, deriveSecretKey(secretKey, salt), new IvParameterSpec(initializationVector))
		}
		return cipher
	}

}
