package net.transitionmanager.integration

import grails.validation.Validateable
import net.transitionmanager.command.CommandObject

@Validateable
class ApiActionScriptCommand implements CommandObject {

	String code
	String script
	ReactionScriptCode reactionScriptCode

	static constraints = {
		code nullable: false, blank: false, validator: { val, obj ->
			if (val) {
				obj.reactionScriptCode = ReactionScriptCode.lookup(val)
				if (!obj.reactionScriptCode) {
					false
				} else {
					true
				}
			}
		}
		script nullable: false, blank: false
		reactionScriptCode nullable: true
	}
}