package com.tdssrc.grails

import groovy.transform.CompileStatic
import org.apache.commons.net.util.Base64

import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

@CompileStatic
class CryptoUtils {
	private static final String key = "aesEncryptionKey"
	private static final String initVector = "encryptionIntVec"
	private static final Cipher cipherEnc
	private static final Cipher cipherDec

	static {
		IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"))
		SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES")

		cipherEnc = Cipher.getInstance("AES/CBC/PKCS5PADDING")
		cipherEnc.init(Cipher.ENCRYPT_MODE, skeySpec, iv)

		cipherDec = Cipher.getInstance("AES/CBC/PKCS5PADDING")
		cipherDec.init(Cipher.DECRYPT_MODE, skeySpec, iv)
	}

	static String encrypt(String value) {
		byte[] encrypted = cipherEnc.doFinal(value.getBytes());
		return Base64.encodeBase64String(encrypted)
	}

	static String decrypt(String encrypted) {
		byte[] original = cipherDec.doFinal(Base64.decodeBase64(encrypted));
		return new String(original);
	}
}
