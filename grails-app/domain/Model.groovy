class Model {
	String name
	String description
	
	static belongsTo = [ deviceType : RefCode,
	                     manufacturer : Manufacturer]
	
	static constraints = {
		name( blank:false, nullable:false, unique:true )
		deviceType( blank:true, nullable:true )
		manufacturer( blank:true, nullable:true )
		description( blank:true, nullable:true )
	}
	
	static mapping  = {	
		version false
		id column:'model_id'
	}
	
	String toString(){
		name
	}
}
