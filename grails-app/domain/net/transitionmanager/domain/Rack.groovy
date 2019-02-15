package net.transitionmanager.domain

import com.tds.asset.AssetEntity

class Rack {

	Project project
	Integer source
	String location
	Room room
	String tag
	Integer roomX = 0
	Integer roomY = 180
	Integer powerA = 3300
	Integer powerB = 3300
	Integer powerC = 0
	String rackType = 'Rack'
	String front = 'L'

	static String alternateKey = 'tag'

	static hasMany = [sourceAssets: AssetEntity, targetAssets: AssetEntity]
	static mappedBy = [sourceAssets: 'rackSource', targetAssets: 'rackTarget']

	static belongsTo = [manufacturer: Manufacturer, model: Model, room: Room]

	static constraints = {
		front blank: true, nullable: true, inList: ['L', 'R', 'T', 'B']
		location blank: true, nullable: true, size: 0..255
		manufacturer nullable: true
		model nullable: true
		powerA nullable: true
		powerB nullable: true
		powerC nullable: true
		rackType nullable: true, inList: ['Rack', 'CRAC', 'DoorL', 'DoorR', 'UPS', 'Object', 'block1x1',
		                                  'block1x2', 'block1x3', 'block1x4', 'block1x5', 'block2x2',
		                                  'block2x3', 'block2x4', 'block2x5', 'block3x3', 'block3x4',
		                                  'block3x5', 'block4x4', 'block4x5', 'block5x5']
		room nullable: true
		roomX nullable: true
		roomY nullable: true
		source nullable: true
		tag blank: true, nullable: true
	}

	static mapping = {
		id column: 'rack_id'
		sourceAssets sort: 'sourceRackPosition'
		targetAssets sort: 'targetRackPosition'
		columns {
			rackType sqlType: 'varchar(20)'
		}
	}

	static transients = ['assets']

	Set<AssetEntity> getAssets() {
		source == 1 ? sourceAssets : targetAssets
	}

	boolean hasBelongsToMoveBundle(moveBundleId) {
		boolean returnVal = false
		Collection<Long> moveBundleIds = getAssets()*.moveBundleId
		if (!moveBundleId.contains('all')) {
			for (id in moveBundleId) {
				returnVal = returnVal ?: moveBundleIds.contains(id)
			}
		}
		return returnVal
	}

	/**
	 * Updating reference of assetentity's  rackSource and sourceRack to null while deleting rack
	 */
	def beforeDelete = {
		withNewSession {
			executeUpdate('update AssetEntity set rackSource=null where rackSource=?', [this])
			executeUpdate('update AssetEntity set rackTarget=null where rackTarget=?', [this])
		}
	}

	String toString() {
		tag
	}
}
