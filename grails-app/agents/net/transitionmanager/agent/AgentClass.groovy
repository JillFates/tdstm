package net.transitionmanager.agent

/*
 * Represents the Agent Class/Party that an Agent works with have been created for. It is used to
 * help keep ApiCredential, ApiAction and Agent classes logically associated together.
 */
enum AgentClass {
	AWS(1),
	RIVERMEADOW(2),
	RISKNETWORKS(3),
	RACIME(4),
	CUSTOM(5)

	int id
	AgentClass(int id) {
		this.id = id
	}

	static AgentClass forId(int id) {
		values().find { it.id == id }
	}
}