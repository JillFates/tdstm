package com.tdsops.common.security.shiro

import org.apache.shiro.authc.SimpleAccount

class UserLoginAccount extends SimpleAccount {

	def String saltPrefix

	public UserLoginAccount(Object principal, Object credentials, String realmName) {
		super(principal, credentials, realmName)
	}

}
