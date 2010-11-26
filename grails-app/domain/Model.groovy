class Model {
	String modelName
	String description
	String assetType
	Integer poweruse
	String connectorLabel
	String type
	Integer connectorPosX
	Integer connectorPosY
	
	
	static belongsTo = [ manufacturer : Manufacturer]
	
	static constraints = {
		modelName( blank:false, nullable:false, unique:'manufacturer' )
		manufacturer( blank:true, nullable:true )
		description( blank:true, nullable:true )
		assetType( blank:true, nullable:true )
		poweruse( blank:true, nullable:true )
		connectorLabel( blank:true, nullable:true )
		type( blank:true, nullable:true, inList: ["Ether", "Serial", "Power", "Fiber", "SCSI", "USB", "KVM", "Other"] )
		connectorPosX( blank:true, nullable:true )
		connectorPosY( blank:true, nullable:true )
	}
	
	static mapping  = {	
		version false
		id column:'model_id'
	}
	
	String toString(){
		modelName
	}
}
