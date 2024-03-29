package net.transitionmanager.project

import net.transitionmanager.asset.AssetEntity
import net.transitionmanager.asset.Rack
import net.transitionmanager.asset.Room
import net.transitionmanager.party.Party

class MoveBundle extends Party {
	static final String USE_FOR_PLANNING = 'useForPlanning'

	String     name
	String     description
	Date       startTime                   // Time that the MoveBundle Tasks will begin
	Date       completionTime              // Planned Completion Time of the MoveBundle
	Integer    operationalOrder = 1     // Order that the bundles are performed in (NOT BEING USED)
	MoveEvent  moveEvent
	Boolean    useForPlanning = true
	Room       sourceRoom
	Room       targetRoom
	Boolean    tasksCreated = false
	Collection assets

	static String alternateKey = 'name'

	static hasMany = [assets: AssetEntity, sourceRacks: Rack, targetRacks: Rack]

	static belongsTo = [project: Project]

	static mappedBy = [project: 'none'] // remove bidirectional mapping

	static constraints = {
		completionTime nullable: true
		description nullable: true, size: 0..255
		moveEvent nullable: true
		name blank: false, unique: ['project'], size: 0..255
		operationalOrder range: 1..25
		sourceRoom nullable: true
		startTime nullable: true
		targetRoom nullable: true
		tasksCreated nullable: true
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

	String toString() { name }

	int getAssetQty() {
		AssetEntity.countByMoveBundle(this)
	}

	/**
	 * MoveBundles that are set true for UseForPlanning
	 */
	static List<MoveBundle> getUseForPlanningBundlesByProject(Project project) {
		project ? findAllByProjectAndUseForPlanning(project, true) : []
	}

	boolean belongsToClient(client) {
		project.clientId == client?.id
	}

	Map toMap() {
		Map moveEventMap
		if (moveEvent) {
			moveEventMap = [
				id: moveEvent.id,
				name: moveEvent.name
			]
		}
		[
			id: id,
			name: name,
			description: description,
			startTime: startTime,
			completionTime: completionTime,
			operationalOrder: operationalOrder,
			moveEvent: moveEventMap,
			useForPlanning: useForPlanning,
			sourceRoom: sourceRoom.toString(),
			targetRoom: targetRoom.toString(),
			tasksCreated: tasksCreated
		]
	}
}
