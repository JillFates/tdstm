package com.tdsops.common.security

import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;

class AESCodec {

	private static password = "tr4ns4ct10n4l12!"

	static encode(target) {
		def cipher = getCipher(Cipher.ENCRYPT_MODE)
		return cipher.doFinal(target.bytes).encodeBase64().toString()
	}

	static decode(target) {
		def cipher = getCipher(Cipher.DECRYPT_MODE)
		return new String(cipher.doFinal(target.decodeBase64()))
	}

	private static getCipher(mode) {
		def keySpec = new SecretKeySpec(getPassword(), "AES")
		def cipher = Cipher.getInstance("AES")
		cipher.init(mode, keySpec)
		return cipher
	}

	private static getPassword() {
		password.bytes
	}

}