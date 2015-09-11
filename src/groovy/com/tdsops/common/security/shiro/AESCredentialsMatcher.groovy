package com.tdsops.common.security.shiro

import org.apache.shiro.authc.credential.SimpleCredentialsMatcher
import org.apache.shiro.authc.AuthenticationInfo
import org.apache.shiro.authc.AuthenticationToken

class AESCredentialsMatcher extends SimpleCredentialsMatcher {

	protected Object getAESCredentials(AuthenticationToken info) {
		def encryptedToken = SecurityUtil.encrypt(new String(info.password))
		return encryptedToken
	}

	public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
		Object tokenCredentials = getAESCredentials(token);
		Object accountCredentials = getCredentials(info);
		return equals(tokenCredentials, accountCredentials);
	}

}