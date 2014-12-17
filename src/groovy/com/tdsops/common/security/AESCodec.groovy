package com.tdsops.common.security

import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;

class AESCodec {

	private static salt_key_postfix = "tr4ns4c"
	private static salt_key_template = "1a2b3c4d5e6f7g8h9i0j"

	static encode(target, salt_key_prefix=null) {
		def cipher = getCipher(Cipher.ENCRYPT_MODE, salt_key_prefix)
		return cipher.doFinal(target.bytes).encodeBase64().toString()
	}

	static decode(target, salt_key_prefix=null) {
		def cipher = getCipher(Cipher.DECRYPT_MODE, salt_key_prefix)
		return new String(cipher.doFinal(target.decodeBase64()))
	}

	private static getCipher(mode, salt_key_prefix) {
		def keySpec = new SecretKeySpec(getPassword(salt_key_prefix), "AES")
		def cipher = Cipher.getInstance("AES")
		cipher.init(mode, keySpec)
		return cipher
	}

	private static getSystemSaltKey() {
		def skp = System.properties["com.tdsops.tm.skp"]?:""
	}

	private static getPassword(salt_key_prefix) {
		if (!salt_key_prefix) {
			salt_key_prefix = getSystemSaltKey()
		}
		def salt_key = salt_key_prefix + salt_key_postfix
		if (salt_key.size() < 16) {
			salt_key = salt_key + salt_key_template[0..(16 - salt_key.size() - 1)]
		} else if (salt_key.size() > 16) {
			salt_key = salt_key[0..15]
		}
		salt_key.bytes
	}

}