package net.transitionmanager.project

import net.transitionmanager.asset.AssetEntity
import net.transitionmanager.party.PartyGroup
import net.transitionmanager.security.RoleType

class ProjectTeam extends PartyGroup {

	String teamCode
	String currentLocation = ''
	Integer isIdle = 1
	String isDisbanded = 'N'
	String role = 'MOVE_TECH'

	static belongsTo = [moveBundle: MoveBundle, latestAsset: AssetEntity]

	static constraints = {
		teamCode blank: false, unique: 'moveBundle'
		latestAsset nullable: true
		isDisbanded nullable: true, inList: ['Y', 'N']
		role nullable: true, inList: [RoleType.CODE_TEAM_MOVE_TECH, RoleType.CODE_TEAM_CLEANER, RoleType.CODE_TEAM_SYS_ADMIN, RoleType.CODE_TEAM_DB_ADMIN]
	}

	static mapping = {
		autoTimestamp false
		id column: 'project_team_id'
		columns {
			isDisbanded sqlType: 'char(1)'
			isIdle sqlType: 'tinyint(1)'
			teamCode sqlType: 'varchar(20)'
		}
	}

	String toString() {
		"$teamCode : $name"
	}
}
