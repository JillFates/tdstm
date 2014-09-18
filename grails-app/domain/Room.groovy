import com.tds.asset.AssetEntity
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.StringUtil
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
	
	// Groovy time stime stamps
	Date dateCreated
	Date lastUpdated
	
	// Value of 1 indicates that the room is a source location otherwise it is a target room
	Integer source = 1
	
	static hasMany = [racks:Rack, sourceAssets:AssetEntity, targetAssets:AssetEntity]
	static mappedBy = [sourceAssets:"roomSource", targetAssets:"roomTarget"]
	
	static constraints = {
		project( nullable:false )
		roomName( blank:false, nullable:false, size:1..255 )
		location( blank:false, nullable:false, size:1..255 )
		roomWidth( nullable:true )
		roomDepth( nullable:true )
		source( nullable:false, range:0..1 )
		address( blank:true, nullable:true, size:0..255 )
		city( blank:true, nullable:true, size:0..255 )
		stateProv( blank:true, nullable:true, size:0..255 )
		postalCode ( blank:true, nullable:true, size:0..12 )
		country( blank:true, nullable:true, size:0..255 )
	}

	static mapping  = {	
		version true
		id column:'room_id'
		postalCode sqlType:"varchar(12)"
		autoTimestamp false
		columns {
			source sqltype: 'tinyint(1)'
		}
	}
	
	String toString(){
		"$location / $roomName"
	}
		
	/*
	 * Date to insert in GMT
	 */
	def beforeInsert = {
		dateCreated = TimeUtil.nowGMT()
		lastUpdated = TimeUtil.nowGMT()
	}
	def beforeUpdate = {
		lastUpdated = TimeUtil.nowGMT()
	}
	
	def getRackCount(){
		return Rack.countByRoom(this)
	}

	def getRackCountByType( type ){
		return Rack.countByRoomAndRackType(this, type)
	}

	/**
	 * Returned the number of assets assiged to racks in the room
	 * @return Integer count of assets
	 */	
	def getAssetCount(){
		// TODO - jpm 8/13 - I would like to see this be a criteria with .count() instead so that the whole recordset isn't returned just to call size()
		return AssetEntity.findAll(
			'FROM AssetEntity where (roomSource=? and rackSource is not null) or (roomTarget = ? and rackTarget is not null)',
			[this, this]
		).size()
	}
	
	def transient getRoomAddress(forWhom) {
		def roomAddress = 
			(this.address ? (forWhom == "link" ? this.address : this.address+"<br/>") : "") + 
			(this.city ?: '' ) + 
			(this.stateProv  ? ", ${this.stateProv}" : '' ) +
			(this.postalCode ? "  ${this.postalCode}" : '' ) +
			(this.country  ? " ${this.country}" : '' )
		return 	roomAddress			   
	}
	
	/**
	 * Updating all Room reference as null
	 */
	def beforeDelete = {
		AssetEntity.withNewSession{
			AssetEntity.executeUpdate("Update AssetEntity set roomSource = null, sourceRoom = null, sourceLocation = null where roomSource = :roomId",[roomId:this])
			AssetEntity.executeUpdate("Update AssetEntity set roomTarget = null, targetRoom = null, targetLocation = null where roomTarget = :roomId",[roomId:this])
			MoveBundle.executeUpdate("Update MoveBundle set sourceRoom = null where sourceRoom = :roomId",[roomId:this])
			MoveBundle.executeUpdate("Update MoveBundle set targetRoom = null where targetRoom = :roomId",[roomId:this])
		}
	}
}
