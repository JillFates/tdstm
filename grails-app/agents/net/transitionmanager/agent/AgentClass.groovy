package net.transitionmanager.agent

/*
 * Represents the Agent Class/Party that an Agent works with have been created for. It is used to
 * help keep ApiCredential, ApiAction and Agent classes logically associated together.
 */
enum AgentClass {
	AWS ('AWS'),
	RIVER_MEADOW ('RIVER_MEADOW'),
	RISK_NETWORKS ('RISK_NETWORKS'),
	RACIME ('RACIME'),
	CUSTOM ('CUSTOM'),
	TRANSITION_MANAGER ('TRANSITION_MANAGER'),
	LICENSE_MANAGER ('LICENSE_MANAGER')

	String id
	AgentClass (String id) {
		this.id = id
	}

	static AgentClass forId(String id) {
		values().find { it.id == id }
	}
}