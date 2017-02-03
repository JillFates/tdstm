package net.transitionmanager.domain

import net.transitionmanager.agent.AgentClass
import net.transitionmanager.agent.CallbackMode

/*
 * The ApiAction domain represents the individual mapped API methods that can be
 * invoked by TransitionManager.
 */
class ApiAction {
	String name

	String description

	// Indicates the class to use for invoking an action
	AgentClass agentClass

	// The method on the agentClass to invoke
	String agentMethod

	// The credentials that should be used with the method
	// ApiCredential agentCredential

	//Map agentParams = {
	//	groupId: [context:ParamContext.ASSET, property:'custom12']
	//}

	// Determines how async API calls notify the completion of an action invocation
	CallbackMode callbackMode = CallbackMode.NA

	// The name of the queue that should be called back for CallbackMode.MESSAGE
	String callbackQueue = ''

	Date dateCreated
	Date lastModified

	static belongsTo = [project: Project]

	static constrants = {
		name size: 1..64
		agentMethod size: 1..64
		callbackQueue size: 0..64
	}

	static mapping = {
		columns {
			name 			sqlType: 'varchar(64)'
			agentMethod 	sqlType: 'varchar(64)'
			callbackQueue 	sqlType: 'varchar(64)'
		}
	}

	String toString() {
		name
	}
}