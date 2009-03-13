class ProjectTeam extends PartyGroup{

	Project project
	String teamCode
	String isDisbanded = "N"

	static constraints = {
		name( ) // related party Group
		teamCode( blank:false, nullable:false,unique:'project' )
		project( blank:false, nullable:false)
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
		}
	}
	
	String toString() {
		"$teamCode : $name"
	}
}
