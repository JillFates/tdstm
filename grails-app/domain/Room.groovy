import com.tds.asset.AssetEntity;
import com.tdssrc.grails.GormUtil
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
	
	// for temp use
	Integer source = 1
	
	static hasMany = [racks:Rack, sourceAssets:AssetEntity, targetAssets:AssetEntity]
	static mappedBy = [sourceAssets:"roomSource", targetAssets:"roomTarget"]
	
	static constraints = {
		project( nullable:false )
		roomName( blank:false, nullable:false )
		location( blank:false, nullable:false )
		roomWidth( nullable:true )
		roomDepth( nullable:true )
		source( nullable:true )
		address( blank:true, nullable:true )
		city( blank:true, nullable:true )
		stateProv( blank:true, nullable:true )
		postalCode ( blank:true, nullable:true )
		country( blank:true, nullable:true )
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
	
	static Room findOrCreateWhere(params) {
		def r = createCriteria()
		def results
		try{
			results = r.list {
				eq('source', params.source.toInteger() ? 1 : 0)
				eq('project.id', params['project.id'])
				eq('location', "${params.location}")
				eq('roomName', "${params.roomName}")
			}
		} catch( Exception ex ){
			println"$ex"
		}
		// Create a new room if it doesn't exist
		def room = results[0]
		if( !room ){
			room = new Room(params)
			if ( !room.validate() || !room.save() ) {
				def etext = "Unable to create Room" +
                GormUtil.allErrorsString( room )
				println etext
			}
		}
		return room
	}
	/*
	 * Date to insert in GMT
	 */
	def beforeInsert = {
		dateCreated = GormUtil.convertInToGMT( "now", "EDT" )
		lastUpdated = GormUtil.convertInToGMT( "now", "EDT" )
	}
	def beforeUpdate = {
		lastUpdated = GormUtil.convertInToGMT( "now", "EDT" )
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
