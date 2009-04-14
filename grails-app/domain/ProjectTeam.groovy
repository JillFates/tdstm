class ProjectTeam extends PartyGroup{

	MoveBundle moveBundle
	String teamCode
	String currentLocation = ""
	Integer isIdle=1
	String isDisbanded = "N"

	static constraints = {
		name( ) // related party Group
		teamCode( blank:false, nullable:false,unique:'moveBundle' )
		moveBundle( blank:false, nullable:false)
		isDisbanded( blank:true, nullable:true, inList:['Y', 'N'] )
		dateCreated( ) // related to party
		lastUpdated( ) // related to party
	}

	static mapping  = {
		version true
		id column: 'project_team_id'
		columns {
			isDisbanded sqlType: 'char(1)'
			teamCode sqlType: 'varchar(20)'
			isIdle sqlType: 'tinyint(1)'
		}
	}
	
	String toString() {
		"$teamCode : $name"
	}
}
