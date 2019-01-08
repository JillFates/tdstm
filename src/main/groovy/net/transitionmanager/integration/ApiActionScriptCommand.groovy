package net.transitionmanager.integration


import net.transitionmanager.command.CommandObject

class ApiActionScriptCommand implements CommandObject {

	ReactionScriptCode code
	String script

	static constraints = {
		code nullable: false, blank: false
		script nullable: false, blank: false
	}
}
