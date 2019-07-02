package net.transitionmanager.command


import net.transitionmanager.security.RoleType

/**
 * A Command object for holding params for Team CRUD.
 */
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
