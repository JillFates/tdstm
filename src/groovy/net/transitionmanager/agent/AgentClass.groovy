package net.transitionmanager.agent

/*
 * Represents the Agent Class/Party that an Agent works with have been created for. It is used to
 * help keep ApiCredential, ApiAction and Agent classes logically associated together.
 */
enum AgentClass {
	AWS,
	RIVER_MEADOW,
	RISK_NETWORKS,
	RACIME,
	CUSTOM,
	TRANSITION_MANAGER,
	LICENSE_MANAGER,
	SERVICE_NOW,
}