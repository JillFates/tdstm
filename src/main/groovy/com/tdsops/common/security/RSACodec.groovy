package com.tdsops.common.security

import groovy.transform.CompileStatic
import net.transitionmanager.exception.InvalidParamException

import javax.crypto.Cipher
import java.security.Key
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec

/**
 * A class to enable the use of RSA public key encryption.
 */
@CompileStatic
class RSACodec {
	private static       int    keySize        = 1024
	private static final String cypherInstance = 'RSA/ECB/OAEPWithSHA1AndMGF1Padding'
	private static final String RSAAlgorithm   = 'RSA'

	static final String DefaultPrivateKeyHeader = '-----BEGIN PRIVATE KEY-----\n'
	static final String DefaultPrivateKeyFooter = '-----END PRIVATE KEY-----'

	static final String DefaultPublicKeyHeader = '-----BEGIN PUBLIC KEY-----\n'
	static final String DefaultPublicKeyFooter = '-----END PUBLIC KEY-----'

	/**
	 * Default constructor, where the key size for the RSA algorithm is set to 1024
	 */
	RSACodec() {}

	/**
	 * Constructor with the ability to set the key size for the RSA algorithm.
	 *
	 * @param keySize The key size for RSA algorithm.
	 */
	RSACodec(int keySize) {
		this.keySize = keySize
	}

	/**
	 * Generates a map with a public and a private key, for RSA encryption.
	 *
	 * @param size the size of the RSA key to use the default being 1024
	 * @param publicKeyHeader the header for the public key defaulting to the DefaultPublicKeyHeader
	 * @param publicKeyFooter the footer for the public key defaulting to the DefaultPublicKeyFooter
	 * @param privateKeyHeader the header for the public key defaulting to the DefaultPrivateKeyHeader
	 * @param privateKeyFooter the footer for the public key defaulting to the DefaultPrivateKeyFooter
	 *
	 * @return a map containing both the public and private keys(in pkcs8 format):
	 * [
	 *     public : '-----BEGIN PUBLIC KEY-----\nu9aa$sy7*...f6hh598=\n-----END PRIVATE KEY-----',
	 *     private: '-----BEGIN PRIVATE KEY-----\nu489dfk5!...jfg83k=\n-----END PRIVATE KEY-----'
	 * ]
	 */
	Map generateKeys(
		int size = keySize,
		String publicKeyHeader = DefaultPublicKeyHeader,
		String publicKeyFooter = DefaultPublicKeyFooter,
		String privateKeyHeader = DefaultPrivateKeyHeader,
		String privateKeyFooter = DefaultPrivateKeyFooter) {
		KeyPairGenerator generator = KeyPairGenerator.getInstance(RSAAlgorithm)
		generator.initialize(size)
		KeyPair pair = generator.generateKeyPair()

		return [
			public : formatKey(pair.public, publicKeyHeader, publicKeyFooter),
			private: formatKey(pair.private, privateKeyHeader, privateKeyFooter)
		]
	}

	/**
	 * A Utility method to encode(base64) and format a key with its header and footer for RSA encryption.
	 *
	 * @param key The key for encode(base64) and format
	 * @param header the string header for the key.
	 * @param footer the String footer for the key.
	 *
	 * @return the formatted  key as a string in pkcs8 format
	 */
	private String formatKey(Key key, String header, String footer) {
		return "$header${key.getEncoded().encodeBase64()}\n$footer"
	}

	/**
	 * Gets a public key instance from a string in pkcs8(X509EncodedKeySpec) format, striping out the header and footer.
	 *
	 * @param text The test of the public key.
	 * @param publicKeyHeader the header for the public key defaulting to the DefaultPublicKeyHeader
	 * @param publicKeyFooter the footer for the public key defaulting to the DefaultPublicKeyFooter
	 *
	 * @return the instance of the public key.
	 */
	PublicKey getPublicKey(String text,
			String publicKeyHeader = DefaultPublicKeyHeader,
			String publicKeyFooter = DefaultPublicKeyFooter) {
		text = text.replace(publicKeyHeader, '')
		text = text.replace(publicKeyFooter, '')

		X509EncodedKeySpec publicSpec = new X509EncodedKeySpec(text.decodeBase64())
		KeyFactory keyFactory = KeyFactory.getInstance(RSAAlgorithm)

		return keyFactory.generatePublic(publicSpec)
	}

	/**
	 * Gets a private key instance from a string in pkcs8(PKCS8EncodedKeySpec) format, striping out the header and footer.
	 *
	 * @param text The test of the private key.
	 * @param privateKeyHeader the header for the public key defaulting to the DefaultPrivateKeyHeader
	 * @param privateKeyFooter the footer for the public key defaulting to the DefaultPrivateKeyFooter
	 *
	 * @return the instance of the private key.
	 */
	PrivateKey getPrivateKey(String text,
			String privateKeyHeader = DefaultPrivateKeyHeader,
			String privateKeyFooter = DefaultPrivateKeyFooter) {
		text = text.replace(privateKeyHeader, '')
		text = text.replace(privateKeyFooter, '')

		PKCS8EncodedKeySpec privateSpec = new PKCS8EncodedKeySpec(text.decodeBase64())
		KeyFactory keyFactory = KeyFactory.getInstance(RSAAlgorithm)

		return keyFactory.generatePrivate(privateSpec)
	}


	/**
	 * Encrypts text using an RSA key with the a cipher instance of RSA/ECB/OAEPWithSHA1AndMGF1Padding.
	 *
	 * @param key The key to use for encryption, this is either a public or private RSA key in pkcs8 format.
	 * @param text The text to encrypt.
	 * @param instance the string param needed to set up the instance of the RSA cipher defaulted to RSA/ECB/OAEPWithSHA1AndMGF1Padding.
	 *
	 * @return The encrypted text.
	 */
	String encrypt(Key key, String text, String instance = cypherInstance) {
		if (!text) {
			throw new InvalidParamException('Null/Blank Strings can not be encrypted.')
		}

		Cipher cipher = Cipher.getInstance(instance)
		cipher.init(Cipher.ENCRYPT_MODE, key)

		return cipher.doFinal(text.bytes).encodeBase64()
	}

	/**
	 * Decrypts text using an RSA key with the a cipher instance of RSA/ECB/OAEPWithSHA1AndMGF1Padding.
	 *
	 * @param key The key to use for decryption, this is either a public or private RSA key in pkcs8 format.
	 * @param text The text to decrypt.
	 * @param instance the string param needed to set up the instance of the RSA cipher defaulted to RSA/ECB/OAEPWithSHA1AndMGF1Padding.
	 *
	 * @return The decrypted text.
	 */
	String decrypt(Key key, String text, String instance = cypherInstance) {
		Cipher cipher = Cipher.getInstance(instance)
		cipher.init(Cipher.DECRYPT_MODE, key)

		return new String(cipher.doFinal(text.decodeBase64()))
	}
}
