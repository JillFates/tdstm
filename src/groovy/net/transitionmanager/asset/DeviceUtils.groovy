package net.transitionmanager.asset

import com.tds.asset.AssetEntity
import com.tds.asset.AssetType
import com.tdsops.common.sql.SqlUtil
import com.tdsops.tm.enums.domain.AssetClass
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.NumberUtil
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Rack
import net.transitionmanager.domain.Room

/**
 * DeviceUtils
 *
 * A collection of methods for the purpose of assisting with the Device Asset Domain
 */
class DeviceUtils {

	/**
	 * Use to get the list of the device RailType Options
	 * @return List of RailTypes
	 */
	static List<String> getAssetRailTypeOptions() {
		AssetEntity.RAIL_TYPES
	}

	/**
	 * Generates a Map of the various Select Control options that is used for Device Asset CRUDs
	 * @param project - the project object
	 * @param asset - the device that is being created/editted
	 * @return map with rail type, rooms, racks, and chassis selectors
	 */
    static Map deviceModelOptions(Project project, AssetEntity asset=null) {
        return [
			railTypeOption: getAssetRailTypeOptions(),
			sourceRoomSelect: getRoomSelectOptions(project, true, true),
			targetRoomSelect: getRoomSelectOptions(project, false, true),
			sourceRackSelect: getRackSelectOptions(project, asset?.roomSourceId, true),
			targetRackSelect: getRackSelectOptions(project, asset?.roomTargetId, true),
			sourceChassisSelect: getChassisSelectOptions(project, asset?.roomSourceId),
			targetChassisSelect: getChassisSelectOptions(project, asset?.roomTargetId)
		]
    }

	/**
	 * Used to get an Select Option list of the Rooms for the source or target of the specified Project
	 * @param project
	 * @param isSource - flag to indicate that it should return Source rooms if true
	 * @param allowAdd - flag if true will include a Add Room... option (-1) default true
	 * @return List<Map<id,value>>
	 */
	static List getRoomSelectOptions(Project project, boolean isSource, boolean allowAdd = true) {
		def rsl = []
		if (allowAdd) {
			rsl << [id: -1, value: 'Add Room...']
		}

		Room.executeQuery(
			'from Room where project=:p and source=:s order by location, roomName',
			[p: project, s: isSource ? 1 : 0]
		)
		.each { Room room ->
			rsl << [id: room.id, value: room.location + ' / ' + room.roomName]
		}

		return rsl
	}

	/**
	 * Used to get a list of the options available for a Rack
	 * @param project
	 * @param room - the room  idto find associated racks for
	 * @param allowAdd - flag if true will include a Add Room... option (-1) default true
	 * @return List<Map<id,value>>
	 */
	static List getRackSelectOptions(Project project, roomId, allowAdd) {
		def rsl = []
		if (allowAdd) {
			rsl << [id: -1, value: 'Add Rack...']
		}

		roomId = NumberUtil.toLong(roomId)
		if (roomId) {
			def racks = Rack.executeQuery('''
				from Rack r inner join r.model m
				where r.project=:p and r.room.id=:r and m.assetType=:t
				order by r.tag
			''', [p: project, r: roomId, t: 'Rack'])
			racks.each { rack ->
					rsl << [id: rack[0].id, value: rack[0].tag]
			}
		}

		return rsl
	}

	/**
	 * Used to get an Select Option list of the chassis located in the specified Room
	 * @param project
	 * @param roomId - the id of the room to find chassis in
	 * @return List<Map<id, value>>
	 */
	static List getChassisSelectOptions(Project project, roomId) {
		def rsl = [ ]
		roomId = NumberUtil.toLong(roomId)
		if (roomId) {
			Room room = GormUtil.findInProject(project, Room, roomId, true)
			def roomProp = room.source ? 'roomSource' : 'roomTarget'
			List chassisList = getAssetsWithCriteriaMap(project, AssetClass.DEVICE, [(roomProp): room, assetType: AssetType.bladeChassisTypes])
			chassisList.each { rsl << [id: it.id, value: it.assetName + '/' + it.assetTag] }
		}

		return rsl
	}

	/**
	 * Used to retrieve a list of assets based on with a criteria map
	 * @param project - the project object for the associated assets
	 * @param ac - the AssetClass to retrieve
	 * @param criteriaMap - the property names to be associated
	 * @param includeJoinData - a flag that when true, if there is a join in the query (e.g. referencing assetType) will return a multi-dimensional array of the dataset
	 * @return the list of assets found
	 */
	static List getAssetsWithCriteriaMap(Project project, AssetClass ac, Map criteriaMap, boolean includeJoinData=false) {
		def params = [project: project, ac: ac]
		String domainName = AssetClass.domainNameFor(ac)
		Object domainClass = AssetClass.domainClassFor(ac)
		StringBuilder from = new StringBuilder("from $domainName a ")
		StringBuilder where = new StringBuilder("where a.project=:project and a.assetClass=:ac ")
		def map
		boolean hasJoin = false

		// Go through the params and construct the query appropriately
		criteriaMap.each { propName, value ->

			if (propName == 'assetType') {
				from.append('inner join a.model m ')
				map = SqlUtil.whereExpression('m.assetType', value, 'assetType')
				hasJoin = true
			} else {
				map = SqlUtil.whereExpression('a.' + propName, value, propName)
			}

			if (map) {
				where.append(' and ' + map.sql)
				params[propName] = map.param
			} else {
				log.error "getAssetsWithCriteriaMap() SqlUtil.whereExpression() returned no value property $propName, criteria $value"
				return null
			}
		}

		String hql = from.toString() + where
		// log.debug "getAssetsWithCriteriaMap() HQL=$hql, params=$params"
		def assets = domainClass.findAll(hql, params)
		// log.debug "getAssetsWithCriteriaMap() found ${assets.size()} : $assets"

		if (assets && hasJoin) {
			if (!includeJoinData) {
				// Just get the Asset objects and exclude the joined domains
				assets = assets.collect { it[0] }
			}
		}
		return assets
	}

}