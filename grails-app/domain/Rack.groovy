class Rack {
	Project project
	Integer source
	String location
	String room
	String tag
	
	static hasMany = [sourceAssets:AssetEntity, targetAssets:AssetEntity]
	static mappedBy = [sourceAssets:"rackSource", targetAssets:"rackTarget"]
	
	static constraints = {
		project( nullable:false )
		source( blank:true, nullable:true )
		location( blank:true, nullable:true )
		room( blank:true, nullable:true )
		tag( blank:true, nullable:true )
	}

	static mapping  = {	
		id column:'rack_id'
		sort name:'tag'
		sourceAssets sort:'sourceRackPosition'
		targetAssets sort:'targetRackPosition'
	}
	
	static Rack findOrCreateWhere(params) {
		def r = createCriteria()
		def results = r.list {
			eq('source', params.source.toInteger() ? 1 : 0)
			eq('project.id', params['project.id'])
			if(params.location == null)
				isNull('location')
			else
				eq('location', params.location)
			if(params.room == null)
				isNull('room')
			else
				eq('room', params.room)
			eq('tag', params.tag)
		}

		// Create a new rack if it doesn't exist
		def rack = results[0]
		if(rack == null)
			rack = new Rack(params).save(flush:true)
		
		return rack
	}
	
	def getAssets() {
		return(source == 1 ? sourceAssets : targetAssets)
	}
}
