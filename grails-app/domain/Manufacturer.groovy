class Manufacturer {
	String name
	String description
	
	static constraints = {
		name( blank:false, nullable:false, unique:true )
		description( blank:true, nullable:true )
	}
	
	static mapping  = {	
		version false
		id column:'manufacturer_id'
	}
	
	String toString(){
		name
	}
}
