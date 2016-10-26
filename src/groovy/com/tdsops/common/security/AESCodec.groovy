package com.tdsops.common.security

import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import java.security.Key

class AESCodec {

	private static final String salt_key_postfix = "tr4ns4c"
	private static final String salt_key_template = "1a2b3c4d5e6f7g8h9i0j"

	static String encode(String target, String salt_key_prefix=null) {
		def cipher = getCipher(Cipher.ENCRYPT_MODE, salt_key_prefix)
		return cipher.doFinal(target.bytes).encodeBase64().toString()
	}

	static String decode(String target, String salt_key_prefix=null) {
		def cipher = getCipher(Cipher.DECRYPT_MODE, salt_key_prefix)
		return new String(cipher.doFinal(target.decodeBase64()))
	}

	private static Cipher getCipher(int mode, String salt_key_prefix) {
		Key keySpec = new SecretKeySpec(getPassword(salt_key_prefix), "AES")
		Cipher cipher = Cipher.getInstance("AES")
		cipher.init(mode, keySpec)
		return cipher
	}

	private static String getSystemSaltKey() {
		System.getProperty 'com.tdsops.tm.skp', ''
	}

	private static byte[] getPassword(String salt_key_prefix) {
		if (!salt_key_prefix) {
			salt_key_prefix = getSystemSaltKey()
		}
		String salt_key = salt_key_prefix + salt_key_postfix
		if (salt_key.size() < 16) {
			salt_key += salt_key_template[0..(16 - salt_key.size() - 1)]
		} else if (salt_key.size() > 16) {
			salt_key = salt_key[0..15]
		}
		salt_key.bytes
	}
}
