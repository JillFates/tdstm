package net.transitionmanager.domain

import com.tds.asset.AssetEntity
import com.tdssrc.grails.HtmlUtil
import com.tdssrc.grails.TimeUtil

class Room {

	String roomName
	String location
	Integer roomWidth = 24
	Integer roomDepth = 24
	Project project
	String address
	String city
	String stateProv
	String postalCode
	String country
	Date dateCreated
	Date lastUpdated

	// Value of 1 indicates that the room is a source location otherwise it is a target room
	Integer source = 1

	static String alternateKey = 'roomName'

	static hasMany = [racks: Rack, sourceAssets: AssetEntity, targetAssets: AssetEntity]
	static mappedBy = [sourceAssets: 'roomSource', targetAssets: 'roomTarget']

	static constraints = {
		address nullable: true
		city nullable: true
		country nullable: true
		location blank: false
		postalCode nullable: true, size: 0..12
		roomDepth nullable: true
		roomName blank: false
		roomWidth nullable: true
		source range: 0..1
		stateProv nullable: true
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
		HtmlUtil.escape(location) + ' / ' + HtmlUtil.escape(roomName)
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
				where (roomSource=? and rackSource is not null)
				   or (roomTarget=? and rackTarget is not null)
		''', [this, this])[0]
	}

	String getRoomAddress(String forWhom) {
		String safeAddress = address ? HtmlUtil.escape(address) : ''
		return (safeAddress ? (forWhom == 'link' ? safeAddress : safeAddress + '<br/>') : '') +
				(city ? HtmlUtil.escape(city) : '') +
				(stateProv ? ', ' + stateProv : '') +
				(postalCode ? '  ' + postalCode : '') +
				(country ? ' ' + country : '')
	}

	private static final String[] BEFORE_DELETE_HQL = [
			'update AssetEntity set roomSource=null, sourceRoom=null, sourceLocation=null where roomSource=?',
			'update AssetEntity set roomTarget=null, targetRoom=null, targetLocation=null where roomTarget=?',
			'update MoveBundle set sourceRoom=null where sourceRoom=?',
			'update MoveBundle set targetRoom=null where targetRoom=?']

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
