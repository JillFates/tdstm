package net.transitionmanager.agent
import groovy.transform.CompileStatic

/*
 * Represents the Agent Class/Party that an Agent works with have been created for. It is used to
 * help keep ApiCredential, ApiAction and Agent classes logically associated together.
 */
@CompileStatic
enum AgentClass {
	// AWS,
	HTTP,
	SERVICE_NOW,
	VCENTER

	// Other Agents to create
	// RIVER_MEADOW,
	// RISK_NETWORKS,
	// RACIME,
	// CUSTOM,
	// TRANSITION_MANAGER,
	// LICENSE_MANAGER,
}