package net.transitionmanager.connector

import groovy.transform.CompileStatic

/*
 * Represents the Connector Class/Party that an Connector works with have been created for. It is used to
 * help keep ApiCredential, ApiAction and Connector classes logically associated together.
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
