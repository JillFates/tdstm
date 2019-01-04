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

/**
 * AESCodec class used fro cryptography.
 * It supports a block length of 128 bits and key lengths of 128, 192, and 256 bits.
 *
 * For Cipher Block Chaining (CBC) see:
 * https://en.wikipedia.org/wiki/Block_cipher_mode_of_operation#Cipher_Block_Chaining_(CBC)
 */
@CompileStatic
class AESCodec {
	private static final int SALT_LEN = 16
	private static String ENCRYPTION_SALT_PREFIX = null
	private static final String SALT_PREFIX_CONFIG_NAME = 'com.tdsops.tm.skp'
	private static final String SALT_KEY_SUFFIX = 's@lt_k3y_suff1x'
	private static final String SALT_PADDING = 'rnu8FkDMG8jhH7EgfCV5krmgE9ZjDKRp'
	private static final int MINIMAL_SECRET_KEY_LENGTH = (int) (KEYLEN_BITS / 8)
	private static final int ITERATION_COUNT = 65536
	private static final String SECRET_KEY_ALGORITHM = 'PBKDF2WithHmacSHA256'
	private static final String AES_ALGORITHM = 'AES'
	private static final String AES_WITH_CBC_ALGORITHM = 'AES/CBC/PKCS5Padding'

	/*
	 * In order to do 256 bit crypto, you have to muck with the files for Java's "unlimited security"
	 * The end user must also install them (not compiled in) so beware.
	 * see here:  http://www.javamex.com/tutorials/cryptography/unrestricted_policy_files.shtml
	 */
	private static int KEYLEN_BITS = 0

	private static final AESCodec INSTANCE = new AESCodec()

	private AESCodec() {
		KEYLEN_BITS = getSupportedBitsKeyLength()
		ENCRYPTION_SALT_PREFIX = getSystemSaltKey()
	}

	static AESCodec getInstance() {
		return INSTANCE
	}

	/**
	 * Generates a random salt of SALT_LEN size in bytes
	 * @return
	 */
	String generateRandomSalt() {
		byte[] salt = new byte[SALT_LEN]
		SecureRandom secureRandom = new SecureRandom()
		secureRandom.nextBytes(salt)
		return Hex.encodeHexString(salt)
	}

	/**
	 * Encodes a plain text string with the given salt
	 * @param value - the plain text value to encode
	 * @param salt - the salt to use
	 * @return an encoded string
	 */
	String encode(String value, String salt) {
		return encode(value, getSecretKey(salt), salt)
	}

	/**
	 * Encodes a plain text string with the given secret key and salt
	 * @param value - the plain text value to encode
	 * @param secretKey - the secret key/password to use
	 * @param salt - the salt
	 * @return an encoded string
	 */
	String encode(String value, String secretKey, String salt) {
		return getEncodedValue(value, secretKey, salt)
	}

	/**
	 * Decodes an encoded value and returns its plain text representation
	 * @param value - the encoded value
	 * @param salt - the salt used during the encode
	 * @return a decoded plain text
	 */
	String decode(String value, String salt) {
		return decode(value, getSecretKey(salt), salt)
	}

	/**
	 * Decodes an encoded value and returns its plain text representation
	 * @param value - the encoded value
	 * @param secretKey - the secret key/password used during the encode
	 * @param salt - the salt used during the encode
	 * @return a decoded plain text
	 */
	String decode(String value, String secretKey, String salt) {
		getDecodedValue(value, secretKey, salt)
	}

	/**
	 * Encodes a plain text string with the given secret key and salt.
	 * It uses CBC (Cipher block chaining)
	 * @param value - the plain text value to encode
	 * @param secretKey - the secret key/password to use
	 * @param salt - the salt
	 * @return a Base64 representation of the encoded value
	 */
	private String getEncodedValue(String value, String secretKey, String salt) {
		// Cipher cipher = getCipher(Cipher.ENCRYPT_MODE, secretKey)
		Cipher cipher = getCipherWithBlockChaining(Cipher.ENCRYPT_MODE, secretKey, salt, null)
		AlgorithmParameters params = cipher.getParameters()
		byte[] encodedValue = cipher.doFinal(value.bytes)

		if (params) {
			// get the initialization vector
			byte[] initializationVector = params.getParameterSpec(IvParameterSpec.class).getIV()

			// return encrypted value with initialization vector prepended
			String encodedValueWithCBC = '{' + initializationVector.encodeHex() + '}' + encodedValue.encodeHex()
			return encodedValueWithCBC.bytes.encodeBase64()
		} else {
			// return simple encrypted value
			return encodedValue.encodeBase64()
		}
	}

	/**
	 * Decodes an encoded value and returns its plain text representation
	 * It detects if the encoded value was generated CBC or simple Cipher to decode the value
	 * @param value - the encoded value
	 * @param secretKey - the secret key/password used during the encode
	 * @param salt - the salt used during the encode
	 * @return a decoded plain text
	 */
	private String getDecodedValue(String value, String secretKey, String salt) {
		// Cipher cipher = getCipher(Cipher.DECRYPT_MODE, secretKey, salt)
		Cipher cipher
		String encryptedPassword = new String(value.decodeBase64())

		// if value matches initialization vector and encrypted password pattern
		// {HEX}base64
		// then use cipher with initialization vector
		if (encryptedPassword ==~ /^\{[A-Fa-f0-9]{32}\}.*$/) {
			String[] encodedText = encryptedPassword.split("(?<=^\\{[A-Fa-f0-9]{32}\\})(?=.*)")
			encryptedPassword = encodedText[1]
			Matcher initializationVectorMatcher = encodedText[0] =~ /([A-Fa-f0-9]{32})/
			String[] initializationVectorMatch = initializationVectorMatcher[0] as String[]
			byte[] initializationVector = initializationVectorMatch[0].decodeHex()
			cipher = getCipherWithBlockChaining(Cipher.DECRYPT_MODE, secretKey, salt, initializationVector)
			return new String(cipher.doFinal(encryptedPassword.decodeHex()))
		} else {
			// else, decrypt simple value
			cipher = getCipher(Cipher.DECRYPT_MODE, secretKey)
			return new String(cipher.doFinal(value.decodeBase64()))
		}
	}

	/**
	 * Retrieves the salt prefix value from System properties
	 * This is used in the constructor fo this class. The class is being warmed up
	 * in the BootStrap.groovy, if System property is not found it throws an exception
	 * and the application won't start.
	 *
	 * @return the system salt key
	 */
	private String getSystemSaltKey() {
		String encryptionSaltPrefix = System.getProperty(SALT_PREFIX_CONFIG_NAME)
		if (!encryptionSaltPrefix) {
			throw new InvalidConfigurationException("Property $SALT_PREFIX_CONFIG_NAME needed for encryption was not found")
		}
		return encryptionSaltPrefix
	}

	/**
	 * Gets the password being used in the secret key using the system configured
	 * salt prefix and suffix
	 * @param salt - the salt
	 * @return
	 */
	private String getSecretKey(String salt) {
		return getSecretKey(ENCRYPTION_SALT_PREFIX, salt, SALT_KEY_SUFFIX)
	}

	/**
	 * Gets the password being used in the secret key using provided prefix, salt and suffix
	 * @param prefix - secret key prefix
	 * @param salt - the salt
	 * @param suffix - secret key suffix
	 * @return the constructed password secret key
	 */
	private String getSecretKey(String prefix, String salt, String suffix) {
		String secretKey = prefix + salt + suffix
		String secretKeyWithPadding = secretKey.padRight(MINIMAL_SECRET_KEY_LENGTH, SALT_PADDING)

		return secretKeyWithPadding
	}

	/**
	 * Creates password array from the given secret key/password string
	 * @param secretKey - the secret key/password to use
	 * @return a 16 bytes password array
	 */
	private byte[] getPassword(String secretKey) {
		if (secretKey.size() > 16) {
			secretKey = secretKey[0..(KEYLEN_BITS / 8) - 1]
		}
		secretKey.bytes
	}

	/**
	 * Creates the password key specification
	 * @param secretKey - the secret key/password to use
	 * @return
	 */
	private SecretKey deriveSecretKey(String secretKey) {
		// Derive the key, given secretKey
		SecretKey secret = new SecretKeySpec(getPassword(secretKey), AES_ALGORITHM)

		return secret
	}

	/**
	 * Creates a cryptographic cipher to encode or decode a value
	 * @param mode - the cipher mode of operation (encrypt, decrypt)
	 * @param secretKey - the secret key/password to use
	 * @param salt - the salt
	 * @return a cryptographic cipher for encryption and decryption
	 */
	private Cipher getCipher(int mode, String secretKey) {
		Cipher cipher = Cipher.getInstance(AES_ALGORITHM)
		cipher.init(mode, deriveSecretKey(secretKey))
		return cipher
	}

	/**
	 * Creates the password key specification
	 * @param secretKey - the secret key/password to use
	 * @param salt - the salt
	 *
	 * For more see:
	 * https://en.wikipedia.org/wiki/PBKDF2
	 *
	 * @return a password key specification
	 */
	private SecretKey deriveSecretKey(String secretKey, String salt) {
		// Derive the key, given secretKey and salt
		SecretKeyFactory factory = SecretKeyFactory.getInstance(SECRET_KEY_ALGORITHM)
		KeySpec spec = new PBEKeySpec(secretKey.toCharArray(), salt.bytes, ITERATION_COUNT, KEYLEN_BITS)
		SecretKey tmp = factory.generateSecret(spec)
		SecretKey secret = new SecretKeySpec(tmp.getEncoded(), AES_ALGORITHM)

		return secret
	}

	/**
	 * Creates a cryptographic cipher to encode or decode a value using CBC
	 * @param mode - the cipher mode of operation (encrypt, decrypt)
	 * @param secretKey - the secret key/password to use
	 * @param salt - the salt
	 * @param initializationVector - required when using CBC for decrypting
	 * @return a cryptographic cipher for encryption and decryption
	 */
	private Cipher getCipherWithBlockChaining(int mode, String secretKey, String salt, byte[] initializationVector) {
		Cipher cipher = Cipher.getInstance(AES_WITH_CBC_ALGORITHM)
		if (Cipher.ENCRYPT_MODE == mode) {
			cipher.init(mode, deriveSecretKey(secretKey, salt))
		} else {
			cipher.init(mode, deriveSecretKey(secretKey, salt), new IvParameterSpec(initializationVector))
		}
		return cipher
	}

	/**
	 * Gets how many key length bits current Java version supports
	 * @return
	 */
	private static int getSupportedBitsKeyLength() {
		// TODO <sl> this needs to be re-worked, by now implementing 128 bits by default
		// return Cipher.getMaxAllowedKeyLength(AES_WITH_CBC_ALGORITHM) == 128 ? 128 : 256
		return 128
	}

}
