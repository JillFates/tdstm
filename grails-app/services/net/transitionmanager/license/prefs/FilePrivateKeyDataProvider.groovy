package net.transitionmanager.license.prefs

import net.nicholaswilliams.java.licensing.encryption.PrivateKeyDataProvider
import net.nicholaswilliams.java.licensing.exception.KeyNotFoundException
import org.apache.commons.io.IOUtils

/**
 * Created by octavio on 9/7/16.
 */
class FilePrivateKeyDataProvider implements PrivateKeyDataProvider{
	private File path

	FilePrivateKeyDataProvider(File path){
		this.path = path
	}

	@Override
	byte[] getEncryptedPrivateKeyData() throws KeyNotFoundException {
		return IOUtils.toByteArray(new FileInputStream(path))
	}
}
