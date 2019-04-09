package net.transitionmanager.license.prefs

import net.nicholaswilliams.java.licensing.encryption.PublicKeyDataProvider
import net.nicholaswilliams.java.licensing.exception.KeyNotFoundException
import org.apache.commons.io.IOUtils

/**
 * Created by octavio on 9/7/16.
 */
class FilePublicKeyDataProvider implements PublicKeyDataProvider {
	private File path

	FilePublicKeyDataProvider(File path){
		this.path = path
	}

	@Override
	byte[] getEncryptedPublicKeyData() throws KeyNotFoundException {
		return IOUtils.toByteArray(new FileInputStream(path))
	}
}
