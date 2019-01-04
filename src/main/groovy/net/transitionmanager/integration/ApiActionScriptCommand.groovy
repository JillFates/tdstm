package net.transitionmanager.integration

import grails.validation.Validateable
import net.transitionmanager.command.CommandObject

@Validateable
class ApiActionScriptCommand implements CommandObject {

	ReactionScriptCode code
	String script

	static constraints = {
		code nullable: false, blank: false
		script nullable: false, blank: false
	}
}
