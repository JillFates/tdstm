import com.tds.asset.AssetEntity;
import com.tdssrc.grails.GormUtil
class Room {
	String roomName
	String location
	Integer roomWidth = 25
	Integer roomDepth = 25
	Project project
	
	// Groovy time stime stamps
	Date dateCreated
	Date lastUpdated
	
	// for temp use
	Integer source = 1
	
	static hasMany = [racks:Rack, sourceAssets:AssetEntity, targetAssets:AssetEntity]
	static mappedBy = [sourceAssets:"roomSource", targetAssets:"roomTarget"]
	
	static constraints = {
		project( blank:false, nullable:false )
		roomName( blank:false, nullable:false )
		location( blank:false, nullable:false )
		roomWidth( blank:true, nullable:true )
		roomDepth( blank:true, nullable:true )
		source( blank:true, nullable:true )
	}

	static mapping  = {	
		version true
		id column:'room_id'
		autoTimestamp false
		columns {
			source sqltype: 'tinyint(1)'
		}
	}
	
	String toString(){
		"$roomName / $location"
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
		return Rack.findAllByRoom(this).size()
	}
	def getAssetCount(){
		return AssetEntity.findAll("FROM AssetEntity where roomSource=? or roomTarget = ?",[this, this]).size()
	}
}
