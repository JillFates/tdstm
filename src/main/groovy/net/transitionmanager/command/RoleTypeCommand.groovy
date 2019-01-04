package net.transitionmanager.command

import grails.validation.Validateable
import net.transitionmanager.domain.RoleType

@Validateable
class RoleTypeCommand implements CommandObject {

	String id
	String description
	String help
	String type
	Integer level

	static constraints = {
		type inList: [RoleType.SECURITY, RoleType.TEAM, RoleType.PROJECT, RoleType.PARTY, RoleType.APP]
	}
}
