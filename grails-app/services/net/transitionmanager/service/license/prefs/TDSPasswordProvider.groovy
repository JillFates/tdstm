package net.transitionmanager.service.license.prefs

import net.nicholaswilliams.java.licensing.encryption.PasswordProvider


class TDSPasswordProvider implements PasswordProvider
{
	private String pass

	TDSPasswordProvider(String pass){
		//copy String for security and avoiding future modification
		this.pass = new String(pass)
	}

	char[] getPassword() {
		return pass.toCharArray()
	}
}
