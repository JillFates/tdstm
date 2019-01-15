package net.transitionmanager.domain

import com.tds.asset.AssetEntity

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
		role nullable: true, inList: ['ROLE_MOVE_TECH', 'ROLE_CLEANER', 'ROLE_SYS_ADMIN', 'ROLE_DB_ADMIN']
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
