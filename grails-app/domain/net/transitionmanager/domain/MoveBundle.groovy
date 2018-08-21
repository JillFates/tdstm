package net.transitionmanager.domain

import com.tds.asset.AssetEntity
import com.tds.asset.AssetType

class MoveBundle extends Party {
	static final String USE_FOR_PLANNING = 'useForPlanning'

	String name
	String description
	Date startTime                   // Time that the MoveBundle Tasks will begin
	Date completionTime              // Planned Completion Time of the MoveBundle
	Integer operationalOrder = 1     // Order that the bundles are performed in (NOT BEING USED)
	MoveEvent moveEvent
	String workflowCode
	Boolean useForPlanning = true
	Room sourceRoom
	Room targetRoom
	Boolean tasksCreated = false

	static String alternateKey = 'name'

	static hasMany = [assets: AssetEntity, moveBundleSteps: MoveBundleStep, sourceRacks: Rack, targetRacks: Rack]

	static belongsTo = [project: Project]

	static mappedBy = [project: 'none'] // remove bidirectional mapping

	static constraints = {
		completionTime nullable: true
		description nullable: true
		moveEvent nullable: true
		name blank: false, unique: ['project']
		operationalOrder range: 1..25
		sourceRoom nullable: true
		startTime nullable: true
		targetRoom nullable: true
		tasksCreated nullable: true
		workflowCode blank: false
	}

	static mapping = {
		sort 'name' // Sorting moveBundle list by name.
		autoTimestamp false
		id column: 'move_bundle_id'
		columns {
			completionTime sqlType: 'DateTime'
			name           sqlType: 'varchar(30)'
			startTime      sqlType: 'DateTime'
		}
		sourceRacks joinTable: [name: 'asset_entity', key: 'move_bundle_id', column: 'rack_source_id']
		targetRacks joinTable: [name: 'asset_entity', key: 'move_bundle_id', column: 'rack_target_id']
		project column: 'project_id'
	}

	def beforeDelete() {
		/* Discarding current move bundle object from delete if
		   trying to delete project's default move bundle */
		if (id == project.projectDefaultBundle.id) {
			discard()
		}
	}

	String toString() { name }

	int getAssetQty() {
		AssetEntity.countByMoveBundle(this)
	}

	/**
	 * MoveBundles that are set true for UseForPlanning
	 */
	static List<MoveBundle> getUseForPlanningBundlesByProject(Project project) {
		project ? MoveBundle.findAllByProjectAndUseForPlanning(project, true) : []
	}

	static final List<String> dependecyBundlingAssetTypes = (AssetType.allServerTypes + ['Application', 'Files',
	     'Database', 'Storage', 'NAS', 'Array', 'SAN']).asImmutable()

	boolean belongsToClient(client) {
		project.clientId == client?.id
	}
}
