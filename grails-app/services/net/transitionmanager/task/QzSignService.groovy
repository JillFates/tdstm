package net.transitionmanager.task

import grails.gorm.transactions.Transactional
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.PasswordFinder
import org.bouncycastle.util.encoders.Base64

import java.security.KeyPair
import java.security.PrivateKey
import java.security.Security
import java.security.Signature

@Transactional
class QzSignService {
	static final DEFAULT_KEYPATH = 'bin/certs/qztray.transitionmanager.net.key'
	static final DEFAULT_CERTPATH = 'bin/certs/qztray.digital-certificate'

	def grailsApplication

	/**
	 *
	 * First we look for the private key relative to the WEB-INF
	 * If it can't be found there, fallback to the Application's Home directory
	 * @return File object representing the configuration File (use exists() to check if its there)
	 */
	File findPrivateKeyFile() {
		if(!grailsApplication.config.tdstm.qztray.keypath){
			log.warn("Application configuration file is missing for the QZ Tray key file property ('qztray.keyPath')")
			grailsApplication.config.tdstm.qztray.keypath = DEFAULT_KEYPATH
		}

		findFileInternal( grailsApplication.config.tdstm.qztray.keypath )
	}

	/**
	 * Gets the Certificate file of the QZTray
	 * @return Certificate File
	 */
	File findCertificateFile() {
		if(!grailsApplication.config.tdstm.qztray.cert){
			log.warn("Application configuration file is missing for the QZ Tray key file property ('qztray.cert')")
			grailsApplication.config.tdstm.qztray.cert = DEFAULT_CERTPATH
		}

		findFileInternal( grailsApplication.config.tdstm.qztray.cert )
	}

	/**
	 * Search for a file in the resource path or the application path (Tomcat)
	 * @return the File
	 */
	private File findFileInternal(String path) {
		def keyFileRes = grailsApplication.parentContext.getResource("/WEB-INF/${path}")

		def file
		if(keyFileRes.exists()){
			file = keyFileRes.file
		}else{
			file = new File(path)
		}

		if(!file.exists()){
			log.warn("QZ Tray file '${file}' was not found")
		}

		return file
	}

	String getPassphrase(){
		if(!grailsApplication.config.tdstm.qztray.passphrase) {
			log.warn("'qztray.passphrase' not defined on Config.groovy, using default")
			grailsApplication.config.tdstm.qztray.passphrase = "3#AKk3XHTc"
		}

		return grailsApplication.config.tdstm.qztray.passphrase
	}

	def sign(String message) {
		String passphrase = getPassphrase()

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
		PEMParser r = new PEMParser(fileReader, new DefaultPasswordFinder(keyPassword))
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
