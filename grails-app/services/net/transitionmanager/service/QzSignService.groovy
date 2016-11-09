package net.transitionmanager.service

import grails.transaction.Transactional
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.PEMReader
import org.bouncycastle.openssl.PasswordFinder
import org.bouncycastle.util.encoders.Base64

import java.security.KeyPair
import java.security.PrivateKey
import java.security.Security
import java.security.Signature

@Transactional
class QzSignService {
	def grailsApplication

	def sign(String message) {
		def passphrase = grailsApplication.config.tdstm.qztray.passphrase
		def keyPath = grailsApplication.config.tdstm.qztray.keypath

		char[] passph = passphrase.toCharArray()
		Security.addProvider(new BouncyCastleProvider())

		File privateKey = new File(keyPath)
		KeyPair keyPair = readKeyPair(privateKey, passph)
		PrivateKey privKey = keyPair.getPrivate()

		Signature signature = Signature.getInstance("SHA1withRSA") //Encryption Algorithm
		signature.initSign(privKey)
		signature.update(message.getBytes())
		byte [] signatureBytes = signature.sign()

		return new String(Base64.encode(signatureBytes))
	}

	private static KeyPair readKeyPair(File privateKey, char [] keyPassword) throws IOException {
		FileReader fileReader = new FileReader(privateKey)
		PEMReader r = new PEMReader(fileReader, new DefaultPasswordFinder(keyPassword))
		try {
			return (KeyPair) r.readObject()
		} catch (IOException ex) {
			throw new IOException("The private key could not be decrypted", ex)
		} finally {
			r.close()
			fileReader.close()
		}
	}

	private static class DefaultPasswordFinder implements PasswordFinder {

		private final char [] password

		private DefaultPasswordFinder(char [] password) {
			this.password = password
		}

		@Override
		public char[] getPassword() {
			return Arrays.copyOf(password, password.length)
		}
	}
}
