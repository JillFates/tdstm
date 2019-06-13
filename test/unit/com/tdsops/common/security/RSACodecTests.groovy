package com.tdsops.common.security

import net.transitionmanager.service.InvalidParamException
import org.bouncycastle.jce.provider.BouncyCastleProvider
import spock.lang.Shared
import spock.lang.Specification

import java.security.PrivateKey
import java.security.PublicKey
import java.security.Security

class RSACodecTests extends Specification {
	//An instance of the RSACodec, to use in testing RSA encryption.
	@Shared
	RSACodec instance

	//A public key to test the RSACodec. Generated from node-rsa.
	@Shared
	String privateKey = '-----BEGIN PRIVATE KEY-----\n' +
						'MIICdQIBADANBgkqhkiG9w0BAQEFAASCAl8wggJbAgEAAoGBAIq/XbTimlLLnceg\n' +
						'fVPveEECbmEp80BP4xhPr+yYuxOnSLkwCN7qMZEOgOEVfHMxE0d7FuWAtYmlkSef\n' +
						'Yrn6FAtRc1FV1k0SF7rA3JwZD6xb0CCJZ/Ky0e0Z9wa/pTLP5Jntc6Y9HxKv52Do\n' +
						'/uaO0Y/bYDrMaZJL2/8AZeZzyzSXAgMBAAECgYAnTCA8VVokTSt5GQmmNlM3BntA\n' +
						'5eLRuk612gwX1QJXhK/iU6qPiIFVdmgmWDGRZrrxKfnIMvY38D72m/wYdK4sP7eP\n' +
						'yRE+7uMR+9yfkPweBgfYN2JEWmyGMaVKXQICsEZJcxhvFNyM2SIMC4skIiZPuWZI\n' +
						'U5yRADNbK7fqw9AiUQJBAPel7C2jhWBgmj+iv69EyePpDw/KGadXBFbvmrDpBfFh\n' +
						'2AtZshxx4lzXNsOpDEyPTUo/lBXKYJeJnnZDdnCfMSsCQQCPbT8xnhs98s4Zzt+e\n' +
						'Sau9vsQIMArn4rlYCm21XDemecvumSMxwXBZeknmWLK22NaxX0vpK2s7gJZzMqZa\n' +
						'5txFAkBpNEqGBN6HV+KPQBNQYNeng32Lhp6pUTUdvYIov4VvytId2Efq/5JbQG4a\n' +
						'Zrk72PDLpwDA1Q19ss/ni7dIFsirAkBZ2ScwTwpjyVohQr5FK4OBzyKAuo/qadaP\n' +
						'96B4b3eRO1Vsd9RbVAGsm/X1bfUupDICVV95hzCaytf0/HLIGT09AkBSngn2zQ1H\n' +
						'+l/Wnli7C2PpzBI5qruQX+4g4DY5qE82ObK5+XwQsJSKgVH8ApvLGsuMlmpULjFC\n' +
						'xjGWs6IVVMvh\n' +
						'-----END PRIVATE KEY-----'

	//A private key to test the RSACodec. Generated from node-rsa.
	@Shared
	String publicKey = '-----BEGIN PUBLIC KEY-----\n' +
					   'MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCKv1204ppSy53HoH1T73hBAm5h\n' +
					   'KfNAT+MYT6/smLsTp0i5MAje6jGRDoDhFXxzMRNHexblgLWJpZEnn2K5+hQLUXNR\n' +
					   'VdZNEhe6wNycGQ+sW9AgiWfystHtGfcGv6Uyz+SZ7XOmPR8Sr+dg6P7mjtGP22A6\n' +
					   'zGmSS9v/AGXmc8s0lwIDAQAB\n' +
					   '-----END PUBLIC KEY-----'

	void setupSpec() {
		Security.addProvider(new BouncyCastleProvider());
		instance = new RSACodec()
	}

	/*
	 * Tests encryption works, by encrypting and decrypting, going from a public key to a private key.
	 */
	void 'test encrypt public to private'() {
		setup:'Given pre generated public/private keys and some original text'
			String originalText = 'Some test text.'
			PublicKey keyPub = instance.getPublicKey(publicKey)
			PrivateKey keyPri = instance.getPrivateKey(privateKey)
		when: 'test encrypting and decrypting text with a public/private key'
			String encryptedText = instance.encrypt(keyPub, originalText)
			String decryptedText = instance.decrypt(keyPri, encryptedText)
		then: 'The original text is not the same as the encrypted text, and the decrypted text is the same as the original text.'
			originalText != encryptedText
			originalText == decryptedText
	}

	/*
	 * Tests encryption works, by encrypting and decrypting, going from a private key to a public key.
	 */
	void 'test encrypt private to public'() {
		setup:'Given pre generated public/private keys and some original text'
			String originalText = 'Some test text.'
			PublicKey keyPub = instance.getPublicKey(publicKey)
			PrivateKey keyPri = instance.getPrivateKey(privateKey)
		when: 'test encrypting and decrypting text with a private/public key'
			String encryptedText = instance.encrypt(keyPri, originalText)
			String decryptedText = instance.decrypt(keyPub, encryptedText)
		then: 'The original text is not the same as the encrypted text, and the decrypted text is the same as the original text.'
			originalText != encryptedText
			originalText == decryptedText
	}

	/*
	 * Tests encryption fails for null strings.
	 */
	void 'test null encryption fails with InvalidParamException'() {
		setup:'Given pre generated public/private keys and a null string'
			String originalText = null
			PrivateKey keyPri = instance.getPrivateKey(privateKey)
		when: 'test trying to encrypt a null string'
			instance.encrypt(keyPri, originalText)
		then: 'an invalid parameter exception is thrown'
			thrown InvalidParamException
	}

	/**
	 * Tests decrypting an encrypted text
	 */
	void 'test decrypt'() {
		when: 'test decrypting text with a private key'
			PrivateKey key = instance.getPrivateKey(privateKey)
			String encryptedText = instance.decrypt(key, "g+L6uiYbkkOGWt0IThb5YeyrtQRofVZ3Y8mDiFLykqLXXb/7jUGVmcndw0XOYtnvTPNUNfaJlFXKmf1nbiUy9SGgCN8EgacsFg7sZWR2m5XW51EfS/ihS0vCu6+IK/sKBeptDodgg0al1fNeYe5GSNmrw23kmJUTx9rz+Yg5smM=")
		then: 'the text is decrypted properly'
			'Hello RSA!' == encryptedText
	}

	/**
	 * Testing key generation works then, encrypting and decrypting text with them.
	 */
	void 'test key generation'() {
		setup: 'Given generated keys, and some original text.'
			Map keys = instance.generateKeys()
			String originalText = 'RSA testing...'
		when: 'encrypting and decrypting with a generated key pair'
			PublicKey keyPub = instance.getPublicKey(keys.public)
			String encryptedText = instance.encrypt(keyPub, originalText)
			PrivateKey keyPri = instance.getPrivateKey(keys.private)
			String decryptedText = instance.decrypt(keyPri, encryptedText)
		then: 'The original text is not the same as the encrypted text, and the decrypted text is the same as the original text.'
			originalText != encryptedText
			originalText == decryptedText
	}

}
