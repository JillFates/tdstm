class Rack {
	Project project
	Integer source
	String location
	Room room
	String tag
	Integer roomX
	Integer roomY
	Integer powerA
	Integer powerB
	Integer powerC 
	
	static hasMany = [sourceAssets:AssetEntity, targetAssets:AssetEntity]
	static mappedBy = [sourceAssets:"rackSource", targetAssets:"rackTarget"]
	
	static constraints = {
		project( nullable:false )
		source( blank:true, nullable:true )
		location( blank:true, nullable:true )
		room( blank:true, nullable:true )
		tag( blank:true, nullable:true )
		roomX( blank:true, nullable:true )
		roomY( blank:true, nullable:true )
		powerA( blank:true, nullable:true )
		powerB( blank:true, nullable:true )
		powerC( blank:true, nullable:true )
	}

	static mapping  = {	
		id column:'rack_id'
		sort name:'tag'
		sourceAssets sort:'sourceRackPosition'
		targetAssets sort:'targetRackPosition'
	}
	
	static Rack findOrCreateWhere(params) {
		def room = Room.findByRoomName(params.room)
		def r = createCriteria()
		def results
		try{
			results = r.list {
				eq('source', params.source.toInteger() ? 1 : 0)
				eq('project.id', params['project.id'])
				if( !params.location )
					isNull('location')
				else
					eq('location', params.location)
				if( !room)
					isNull('room')
				else
					eq('room', room )
				eq('tag', params.tag)
			}
		} catch( Exception ex ){
			println"$ex"
		}
		// Create a new rack if it doesn't exist
		def rack = results[0]
		if( !rack )
			rack = new Rack(params).save()
		
		return rack
	}
	
	def getAssets() {
		return(source == 1 ? sourceAssets : targetAssets)
	}
}
