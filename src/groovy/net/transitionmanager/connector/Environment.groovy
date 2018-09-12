package net.transitionmanager.connector

/*
 * Represents the environment that a set of credentials is used for
 */
enum Environment {
	PRODUCTION('PRODUCTION'),
	TEST('TEST')	// a.k.a. sandbox

	String id
	Environment (String id) {
		this.id = id
	}

	static Environment forId(String id) {
		values().find { it.id == id }
	}
}