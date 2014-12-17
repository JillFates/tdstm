package com.tdsops.common.security

import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;

class DESCodec {

	private static salt_key_postfix = "tr4ns4ct10n4l"

	static encode(target, salt_key_prefix=null) {
		def cipher = getCipher(Cipher.ENCRYPT_MODE, salt_key_prefix)
		return cipher.doFinal(target.bytes).encodeBase64().toString()
	}

	static decode(target, salt_key_prefix=null) {
		def cipher = getCipher(Cipher.DECRYPT_MODE, salt_key_prefix)
		return new String(cipher.doFinal(target.decodeBase64()))
	}

	private static getCipher(mode, salt_key_prefix) {
		def keySpec = new DESKeySpec(getPassword(salt_key_prefix))
		def cipher = Cipher.getInstance("DES")
		def keyFactory = SecretKeyFactory.getInstance("DES")
		cipher.init(mode, keyFactory.generateSecret(keySpec))
		return cipher
	}

	private static getSystemSaltKey() {
		def skp = System.properties["com.tdsops.tm.skp"]?:""
	}

	private static getPassword(salt_key_prefix) {
		if (!salt_key_prefix) {
			salt_key_prefix = getSystemSaltKey()
		}
		return (salt_key_prefix + salt_key_postfix).bytes
	}

}