package net.transitionmanager.agent

import groovy.transform.CompileStatic

/*
 * Represents the Agent Class/Party that an Agent works with have been created for. It is used to
 * help keep ApiCredential, ApiAction and Agent classes logically associated together.
 */
@CompileStatic
enum ContextType {
	APPLICATION,
	ASSET,	// Any class of asset (App, DB, Device, Storage, etc)
	DATABASE,
	DEVICE,
	STORAGE,
	TASK,
	USER_DEF,

	// Future contexts but not for now..
	// BUNDLE,
	// EVENT,
	// PROJECT,

	static ContextType lookup(Object value) {
		return ContextType.valueOf(value as String) as ContextType
	}

}