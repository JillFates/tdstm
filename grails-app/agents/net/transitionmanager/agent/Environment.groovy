package net.transitionmanager.agent

/*
 * Represents the environment that a set of credentials is used for
 */
enum Environment {
	PRODUCTION(1),
	TEST(2)	// a.k.a. sandbox

	int id
	Environment(int id) {
		this.id = id
	}

	static Environment forId(int id) {
		values().find { it.id == id }
	}
}