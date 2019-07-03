package net.transitionmanager.command

import grails.validation.Validateable
import net.transitionmanager.domain.RoleType

@Validateable
class TeamCommand implements CommandObject {

	String id
	String description
	String help
	String type = RoleType.TEAM
	Integer level

	static constraints = {
		type inList: [RoleType.TEAM]
	}
}
