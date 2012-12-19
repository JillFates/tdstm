import com.tds.asset.AssetEntity
import com.tdssrc.grails.TimeUtil

class Manufacturer {
	String name
	String description
	Date dateCreated
	Date lastModified

	static hasMany = [ 
		models:Model, 
		racks:Rack
	]
	
	static constraints = {
		name( blank:false, nullable:false, unique:true )
		description( blank:true, nullable:true )
		lastModified( nullable:true )
	}
	
	static mapping  = {	
		autoTimestamp false
		id column:'manufacturer_id'
	}
	
	String toString() {
		name
	}
	
	def beforeInsert = {
		dateCreated = lastModified = TimeUtil.nowGMT()
	}
	def beforeUpdate = {
		lastModified = TimeUtil.nowGMT()
	}
	
	/*
	 * Handle cascading delete logic that is not implemented through constraints
	 *   1. Set all AssetEntity.manufacturer to null
	 *   2. Delete all manufacturer Aliases
	 *   3. TODO - handle Room?
	 *   4. TODO - What about the sync tables?
	 */
	def beforeDelete = {
        AssetEntity.withNewSession{ 
            AssetEntity.executeUpdate("Update AssetEntity set manufacturer=null where manufacturer = :manufacturer",[manufacturer:this])
        }
        ManufacturerAlias.withNewSession { aliases*.delete() }        
	}
	
	/*
	 * @return: Number of Models associated with this Manufacturer 
	 */
	def getModelsCount(){
		return Model.countByManufacturer(this)
	}

	/** 
	 * Get list of alias records for the manufacturer
	 * @Return Set of ManufacturerAlias records for the current manufacturer
	 */
	def getAliases() {
		ManufacturerAlias.findAllByManufacturer(this, [sort:'name'])
	}
	
	/**
	 * Used to get a ManufacturerAlias object by name and create one (optionally) if it doesn't exist 
	 * @param String name - name of the manufacturer alias
	 * @param Boolean createIfNotFound - optional flag to indicating if record should be created (default false)
	 * @return ManufacturerAlias - a ManufacturerAlias object if found or was successfully created , or null if not found or not created 
	 */
	def findOrCreateAliasByName(name, def createIfNotFound = false){
		def alias = ManufacturerAlias.findByNameAndManufacturer(name, this)
		if ( !alias && createIfNotFound) {
			alias = new ManufacturerAlias(name:name.trim(), manufacturer:this)
			if (! alias.save(flush:true)) {
				alias.errors.allErrors.each { log.error it}
				alias = null
			}
		}
        return alias
	}
}
