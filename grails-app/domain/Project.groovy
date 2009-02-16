class Project extends PartyGroup {

	String projectCode
	String description
	String trackChanges
	Date startDate	// Date that the project will start
	Date completionDate	// Date that the project will finish

	static constraints = {
		name( ) // related party Group
		projectCode( blank:false, nullable:false, unique:true )
		description( blank:true, nullable:true )
		trackChanges( blank:false, nullable:false, inList:['Y', 'N'] )
		startDate( blank:true, nullable:true )
		completionDate( blank:true, nullable:true )
		dateCreated( ) // related to party
		lastUpdated( ) // related to party
	}

	static mapping  = {
		version true
		id column: 'project_id'
		columns {
			trackChanges sqlType: 'char(1)'
			projectCode sqlType: 'varchar(20)', index: 'project_projectcode_idx', unique: true
		}
	}
	
	String toString() {
		"$projectCode : $name"
	}
}
