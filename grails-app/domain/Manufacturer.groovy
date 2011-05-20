class Manufacturer {
	String name
	String description
	String aka

	static hasMany = [ models : Model, racks:Rack ]
	
	static constraints = {
		name( blank:false, nullable:false, unique:true )
		description( blank:true, nullable:true )
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
	}
	
	static mapping  = {	
		version false
		id column:'manufacturer_id'
	}
	
	String toString(){
		name
	}
	/*
	 * @return: Number of Models associated with this Manufacturer 
	 */
	def getModelsCount(){
		return Model.countByManufacturer(this)
	}
}
