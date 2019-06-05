package com.tdsops.common.security

import groovy.transform.CompileStatic
import net.transitionmanager.service.InvalidParamException

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

	private static final String privateKeyHeader = '-----BEGIN PRIVATE KEY-----\n'
	private static final String privateKeyFooter = '-----END PRIVATE KEY-----'

	private static final String publicKeyHeader = '-----BEGIN PUBLIC KEY-----\n'
	private static final String publicKeyFooter = '-----END PUBLIC KEY-----'

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
	 *
	 * @return a map containing both the public and private keys(in pkcs8 format):
	 * [
	 *     public : '-----BEGIN PUBLIC KEY-----\nu9aa$sy7*...f6hh598=\n-----END PRIVATE KEY-----',
	 *     private: '-----BEGIN PRIVATE KEY-----\nu489dfk5!...jfg83k=\n-----END PRIVATE KEY-----'
	 * ]
	 */
	Map generateKeys(int size = keySize) {
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
	 *
	 * @return the instance of the public key.
	 */
	PublicKey getPublicKey(String text) {
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
	 *
	 * @return the instance of the private key.
	 */
	PrivateKey getPrivateKey(String text) {
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
		if (text == null) {
			throw new InvalidParamException('Null Strings can not be encrypted.')
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
