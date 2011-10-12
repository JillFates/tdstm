import com.tds.asset.AssetEntity
import com.tds.asset.AssetTransition

class ProjectTeam extends PartyGroup{
	String teamCode
	String currentLocation = ""
	Integer isIdle=1
	String isDisbanded = "N"
	String role = "MOVE_TECH"

	static belongsTo = [ 
	                    moveBundle : MoveBundle,
	                    latestAsset : AssetEntity
	                    ]
	
	static hasMany = [
		assetTransitions : AssetTransition
	]
	
	static constraints = {
		name( ) // related party Group
		teamCode( blank:false, nullable:false,unique:'moveBundle' )
		moveBundle( nullable:false )
		latestAsset( nullable:true )
		isDisbanded( blank:true, nullable:true, inList:['Y', 'N'] )
		role( blank:true, nullable:true, inList:['MOVE_TECH', 'CLEANER','SYS_ADMIN',"DB_ADMIN"] )
		dateCreated( ) // related to party
		lastUpdated( ) // related to party
	}
	
	static mapping  = {
		version true
		autoTimestamp false
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
