package net.transitionmanager.agent

/*
 * Represents the Agent Class/Party that an Agent works with have been created for. It is used to
 * help keep ApiCredential, ApiAction and Agent classes logically associated together.
 */
enum ContextType {
	APPLICATION ('APPLICATION'),
	ASSET ('ASSET'),	// Any class of asset (App, DB, Server, Storage, etc)
	BUNDLE ('BUNDLE'),
	DATABASE ('DATABASE'),
	DEVICE ('DEVICE'),
	EVENT ('EVENT'),
	PROJECT ('PROJECT'),
	STORAGE ('STORAGE'),
	SERVER ('SERVER'),
	TASK ('TASK'),
	USER_DEF ('USER_DEF')	// User defined

	String id
	ContextType (String id) {
		this.id = id
	}

	static ContextType forId(String id) {
		values().find { it.id == id }
	}

}