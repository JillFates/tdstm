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

	/**
	 *
	 * First we look for the private key relative to the WEB-INF
	 * If it can't be found there, fallback to the Application's Home directory
	 * @return File object representing the configuration File (use exists() to check if its there)
	 */
	File findPrivateKeyFile(){
		def keyPath = grailsApplication.config.tdstm.qztray.keypath
		def keyFileRes = grailsApplication.parentContext.getResource("/WEB-INF/${keyPath}")

		if(keyFileRes.exists()){
			return keyFileRes.file
		}else{
			return new File(keyPath)
		}
	}

	def sign(String message) {
		def passphrase = grailsApplication.config.tdstm.qztray.passphrase

		File privateKeyFile = findPrivateKeyFile()

		char[] passph = passphrase.toCharArray()
		Security.addProvider(new BouncyCastleProvider())

		KeyPair keyPair = readKeyPair(privateKeyFile, passph)
		PrivateKey privKey = keyPair.getPrivate()

		Signature signature = Signature.getInstance("SHA1withRSA") //Encryption Algorithm
		signature.initSign(privKey)
		signature.update(message.getBytes())
		byte [] signatureBytes = signature.sign()

		return new String(Base64.encode(signatureBytes))
	}

	private static KeyPair readKeyPair(File privateKeyFile, char [] keyPassword) throws IOException {
		FileReader fileReader = new FileReader(privateKeyFile)
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
