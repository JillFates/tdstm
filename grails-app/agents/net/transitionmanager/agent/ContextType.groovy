package net.transitionmanager.agent

/*
 * Represents the Agent Class/Party that an Agent works with have been created for. It is used to
 * help keep ApiCredential, ApiAction and Agent classes logically associated together.
 */
enum ContextType {
	APPLICATION,
	ASSET,	// Any class of asset (App, DB, Server, Storage, etc)
	BUNDLE,
	DATABASE,
	DEVICE,
	EVENT,
	PROJECT,
	STORAGE,
	SERVER,
	TASK,
	USER_DEF
}