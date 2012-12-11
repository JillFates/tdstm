import com.tdssrc.grails.TimeUtil

class Manufacturer {
	String name
	String description
	String aka		// TODO - DELETE aka
	Date dateCreated
	Date lastModified

	static hasMany = [ 
		models:Model, 
		racks:Rack
	]
	
	static constraints = {
		name( blank:false, nullable:false, unique:true )
		description( blank:true, nullable:true )
		// TODO - DELETE aka
		aka( blank:true, nullable:true, validator: { val, obj ->
			if(val){
				def isDuplicated = false
				def akaArray = val.split(",")
				def manufacturers = Manufacturer.findAllByAkaIsNotNull()
				manufacturers = obj.id ? manufacturers.findAll{it.id != obj.id } : manufacturers
				manufacturers?.aka?.each{ akaString->
					akaArray.each{
						if(akaString.toLowerCase().contains( it.toLowerCase() )){
							isDuplicated = true
						}
					}
				}
				if(isDuplicated)
	            return ['invalid.string']
			} else {
				return true
			}
        })
		userlogin( blank:false)
		lastModified( blank:true, nullable:true )
	}
	
	static mapping  = {	
		version false
		autoTimestamp false
		id column:'manufacturer_id'
	}
	
	String toString(){
		name
	}
	
	def beforeInsert = {
		dateCreated = TimeUtil.convertInToGMT( "now", "EDT" )
		lastModified = TimeUtil.convertInToGMT( "now", "EDT" )
	}
	def beforeUpdate = {
		lastModified = TimeUtil.convertInToGMT( "now", "EDT" )
	}
	
	/*
	 * @return: Number of Models associated with this Manufacturer 
	 */
	def getModelsCount(){
		return Model.countByManufacturer(this)
	}
	
	// Get list of alias records for the manufacturer
	def getAliases() {
		ManufacturerAlias.findAllByManufacturer(this, [sort:'name'])
	}
	
	/**
	 * Used to find or create manufacturer_alias based on flag .
	 * @param : name -> aka value
	 * @param : createIfNotFound -> flag to determine whether need to create ModelAlias or not
	 * @return : ManufacturerAlias instance 
	 *
	 */
	def findOrCreateByName(name, def createIfNotFound = false){
		def manuAlias = ManufacturerAlias.findByNameAndManufacturer(name,this)
		if(!manuAlias && createIfNotFound){
			manuAlias = new ManufacturerAlias(name:name.trim(), manufacturer:this)
			if(manuAlias.save(flush:true)){
				manuAlias.errors.allErrors.each { log.error it}
			}
		}
        return manuAlias
	}
}
