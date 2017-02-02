package net.transitionmanager.service.license.prefs

import net.nicholaswilliams.java.licensing.encryption.PasswordProvider


class TDSPasswordProvider implements PasswordProvider
{
	private String pass

	TDSPasswordProvider(String pass){
		this.pass = new String(pass) //copy String
	}

	char[] getPassword() {
		println("pass : $pass")
		return pass.toCharArray()
	}
}