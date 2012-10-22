import com.tds.asset.AssetEntity
import com.tds.asset.AssetTransition

class MoveBundle extends Party {	
    Project project
    String name
    String description
    Date startTime			// Time that the MoveBundle Tasks will begin
    Date completionTime				// Planned Completion Time of the MoveBundle
    Integer operationalOrder = 1  		// Order that the bundles are performed in (NOT BEING USED)
    MoveEvent moveEvent
	String workflowCode
	Boolean useOfPlanning = true
	Room sourceRoom
	Room targetRoom
	
    static constraints = {        
		name( blank:false, nullable:false )
		project( nullable:false )
		moveEvent( nullable:true )
		description( blank:true, nullable:true )		
		startTime( nullable:true )
		completionTime( nullable:true )
		operationalOrder( nullable:false, range:1..25 )
		workflowCode( blank:false, nullable:false )
		sourceRoom( nullable:true )
		targetRoom( nullable:true )
	}

	static hasMany = [
		assetTransitions : AssetTransition,
		moveBundleSteps  : MoveBundleStep,
		sourceRacks : Rack,
		targetRacks : Rack,
		assets : AssetEntity
	]
	static mapping  = {
		version true
		autoTimestamp false
		id column:'move_bundle_id'
        columns {
			name sqlType: 'varchar(30)'
		 	startTime sqlType: 'DateTime'
		 	completionTime sqlType: 'DateTime'
		}        
		sourceRacks joinTable:[name: 'asset_entity', key:'move_bundle_id', column:'rack_source_id']
		targetRacks joinTable:[name: 'asset_entity', key:'move_bundle_id', column:'rack_target_id']
	}

    String toString(){
		name
	}
    def getAssetQty(){
    	return AssetEntity.countByMoveBundle(this)
    }
	/** 
	 * @author: Lokanada Reddy
	 * @param : projectId, currentTime
	 * @return : List of move bundles
	 */
	static def getActiveBundlesByProject( projectId, timeNow ){
		def moveBundles = MoveBundle.createCriteria().list {
			and {
				le("startTime", timeNow)
				ge("completionTime", timeNow)
			}
			order("startTime", "desc")
		}
		
		return projectId ? moveBundles.findAll{it.project.id == projectId } : moveBundles
	}
}
