/*
 * An AssetTransition is used to record the state change or transition of an asset from
 * one state to another at a particular time and by whom.  
 */
class AssetTransition {

	String stateFrom
	String stateTo
	Integer timeElapsed = 0		// Time it took from the last transition until this transtion (seconds)
	Integer wasOverridden = 0	// Indicates that the admin reset the state
	Integer wasSkippedTo = 0	// Indicates that a state was skipped prior to this transtion stateTo
	String comment
	Date dateCreated = new Date()

	static belongsTo = [ 
		assetEntity : AssetEntity,
		moveBundle  : MoveBundle,
		projectTeam : ProjectTeam,
		userLogin   : UserLogin
	]

	static constraints = {
		assetEntity( blank:false, nullable:false )
		stateFrom( blank:false, nullable:false )
		stateTo( blank:false, nullable:false )
		comment( blank:true, nullable:true, size:0..255 )
		moveBundle( blank:false, nullable:false )
		projectTeam( blank:true, nullable:true )		// Not all transitions are performed by teams
		userLogin( blank:false, nullable:false )		// Transitions are all by users
		timeElapsed( nullable:false )
		wasOverridden( range:0..1 )
		wasSkippedTo( range:0..1 )
	}	
	
	static mapping  = {
		version false
		id column:'asset_transition_id'
        columns {
		 	stateFrom sqlType: 'varchar(20)'
		 	stateTo sqlType: 'varchar(20)'
			wasOverridden sqlType: 'tinyint(1)'
			wasSkippedTo sqlType: 'tinyint(1)'
		}        
	}
}