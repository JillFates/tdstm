package com.tdsops.common.security.shiro

import org.apache.shiro.authc.credential.SimpleCredentialsMatcher
import org.apache.shiro.authc.AuthenticationInfo
import org.apache.shiro.authc.AuthenticationToken
import com.tdsops.common.security.SecurityUtil

class SHA2CredentialsMatcher extends SimpleCredentialsMatcher {

	protected Object getSHA2Credentials(AuthenticationToken token, AuthenticationInfo info) {
		def	encryptedToken = SecurityUtil.encrypt(new String(token.password), info.saltPrefix)
		return encryptedToken
	}

	public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
		Object tokenCredentials = getSHA2Credentials(token, info);
		Object accountCredentials = getCredentials(info);
		return equals(tokenCredentials, accountCredentials);
	}

}