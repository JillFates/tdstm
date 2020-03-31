package net.transitionmanager.asset

import com.tdssrc.grails.HtmlUtil
import com.tdssrc.grails.TimeUtil
import net.transitionmanager.project.Project

class Room {

	String  roomName
	String  location
	Integer roomWidth = 24
	Integer roomDepth = 24
	Project project
	String  address
	String  city
	String  stateProv
	String  postalCode
	String  country
	Date    dateCreated
	Date    lastUpdated

	// Value of 1 indicates that the room is a source location otherwise it is a target room
	Integer source = 1

	static String alternateKey = 'roomName'

	static hasMany = [racks: Rack, sourceAssets: AssetEntity, targetAssets: AssetEntity]
	static mappedBy = [sourceAssets: 'roomSource', targetAssets: 'roomTarget']

	static constraints = {
		address nullable: true, size: 0..255
		city nullable: true, size: 0..255
		country nullable: true, size: 0..255
		location blank: false, size: 0..255
		postalCode nullable: true, size: 0..12
		roomDepth nullable: true
		roomName blank: false, size: 0..255
		roomWidth nullable: true
		source range: 0..1
		stateProv nullable: true, size: 0..255
	}

	static mapping = {
		id column: 'room_id'
		postalCode sqlType: 'varchar(12)'
		autoTimestamp false
		columns {
			source sqltype: 'tinyint(1)'
		}
	}

	String toString() {
		location + ' / ' + roomName
	}

	Map toMap() {
		[
			id         : id,
			roomName   : roomName,
			location   : location,
			roomWidth  : roomWidth,
			roomDepth  : roomDepth,
			project    : project.toMap(),
			address    : address,
			city       : city,
			stateProv  : stateProv,
			postalCode : postalCode,
			country    : country,
			dateCreated: dateCreated,
			lastUpdated: lastUpdated
		]
	}

	int getRackCount() {
		Rack.countByRoom(this)
	}

	int getRackCountByType(type) {
		Rack.countByRoomAndRackType(this, type)
	}

	/**
	 * The number of assets assiged to racks in the room
	 */
	int getAssetCount() {
		executeQuery('''
				SELECT COUNT(*) FROM AssetEntity
				where (roomSource=?0 and rackSource is not null)
				   or (roomTarget=?1 and rackTarget is not null)
		''', [this, this])[0]
	}

	String getRoomAddress(String forWhom) {
		String safeAddress = HtmlUtil.escape(address)
		return (safeAddress ? (forWhom == 'link' ? safeAddress : safeAddress + '<br/>') : '') +
				(HtmlUtil.escape(city)) +
				(stateProv ? ", ${HtmlUtil.escape(stateProv)}" : '') +
				(postalCode ? '  ' + HtmlUtil.escape(postalCode) : '') +
				(country ? ' ' + HtmlUtil.escape(country) : '')
	}

	private static final String[] BEFORE_DELETE_HQL = [
			'update AssetEntity set roomSource=null, sourceRoom=null, sourceLocation=null where roomSource=?0',
			'update AssetEntity set roomTarget=null, targetRoom=null, targetLocation=null where roomTarget=?0',
			'update MoveBundle set sourceRoom=null where sourceRoom=?0',
			'update MoveBundle set targetRoom=null where targetRoom=?0']

	def beforeInsert = {
		dateCreated = lastUpdated = TimeUtil.nowGMT()
	}
	def beforeUpdate = {
		lastUpdated = TimeUtil.nowGMT()
	}
	/**
	 * Updating all Room reference as null
	 */
	def beforeDelete = {
		withNewSession {
			List args = [this]
			for (String hql in BEFORE_DELETE_HQL) {
				executeUpdate(hql, args)
			}
		}
	}
}
