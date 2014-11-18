import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;

class DESCodec {

	private static password = "tr4ns4ct10n4l"

	static encode(target) {
		def cipher = getCipher(Cipher.ENCRYPT_MODE)
		return cipher.doFinal(target.bytes).encodeBase64()
	}

	static decode(target) {
		def cipher = getCipher(Cipher.DECRYPT_MODE)
		return new String(cipher.doFinal(target.decodeBase64()))
	}

	private static getCipher(mode) {
		def keySpec = new DESKeySpec(getPassword())
		def cipher = Cipher.getInstance("DES")
		def keyFactory = SecretKeyFactory.getInstance("DES")
		cipher.init(mode, keyFactory.generateSecret(keySpec))
		return cipher
	}

	private static getPassword() {
		password.getBytes("UTF-8")
	}

}