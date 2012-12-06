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
		return Rack.findAllByRoom(this).size()
	}
	def getAssetCount(){
		return AssetEntity.findAll("FROM AssetEntity where roomSource=? or roomTarget = ?",[this, this]).size()
	}
	
	def transient getRoomAddress(forWhom){
		def roomAddress = (this.address ? (forWhom == "link" ? this.address : this.address+"<br/>") : "") + (this.city ? this.city+"," : "" )+ (this.stateProv  ? this.stateProv +"," : "" )+
						   (this.postalCode ? this.postalCode+"," : "" )+(this.country  ? this.country: "" )
		return 	roomAddress			   
	}
}
