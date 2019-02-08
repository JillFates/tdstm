package com.tdsops.tm.enums.domain

import groovy.transform.CompileStatic

@CompileStatic
enum RemoteCredentialMethod {
	OS_KEY('OS Keystore (e.g. PSCredential/SSH Keys)'),
	PROMPTED('User prompted'),
	SUPPLIED('Supplied by TransitionManager'),
	USER_PRIV('User OS privilege')
	// AWS_SECRETS('AWS Secrets Manager')
	// HASHI_VAULT('HashiCorp Vault')

	final String credentialMethod;

	RemoteCredentialMethod(String credentialMethod) {
		this.credentialMethod = credentialMethod
	}

	static toMap() {
		values().collectEntries { e ->
			[(e.name()): e.credentialMethod]
		}
	}

	@Override
	public String toString() {
		return this.credentialMethod
	}
}
