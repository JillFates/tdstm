package net.transitionmanager.service.license.prefs

import net.nicholaswilliams.java.licensing.encryption.PasswordProvider


public class TDSPasswordProvider implements PasswordProvider
{
	private String pass;

	public TDSPasswordProvider(String pass){
		this.pass = new String(pass); //copy String
	}

	public char[] getPassword() {
		println("pass : $pass")
		return pass.toCharArray();
	}
}