package com.tdsops.common.security

import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.DESKeySpec
import java.security.spec.KeySpec

class DESCodec {

	private static final String salt_key_postfix = "tr4ns4ct10n4l"

	static String encode(String target, String salt_key_prefix=null) {
		def cipher = getCipher(Cipher.ENCRYPT_MODE, salt_key_prefix)
		return cipher.doFinal(target.bytes).encodeBase64().toString()
	}

	static String decode(String target, String salt_key_prefix=null) {
		def cipher = getCipher(Cipher.DECRYPT_MODE, salt_key_prefix)
		return new String(cipher.doFinal(target.decodeBase64()))
	}

	private static Cipher getCipher(int mode, String salt_key_prefix) {
		KeySpec keySpec = new DESKeySpec(getPassword(salt_key_prefix))
		Cipher cipher = Cipher.getInstance("DES")
		cipher.init(mode, SecretKeyFactory.getInstance("DES").generateSecret(keySpec))
		return cipher
	}

	private static String getSystemSaltKey() {
		System.getProperty 'com.tdsops.tm.skp', ''
	}

	private static byte[] getPassword(String salt_key_prefix) {
		if (!salt_key_prefix) {
			salt_key_prefix = getSystemSaltKey()
		}
		return (salt_key_prefix + salt_key_postfix).bytes
	}
}
