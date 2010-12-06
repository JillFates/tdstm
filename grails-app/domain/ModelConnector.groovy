
class ModelConnector {
	// Declare propertied
	String connector
	Integer exist = 0
	String label
	String type
	String labelPosition
	Integer connectorPosX
	Integer connectorPosY
	String status
	
	static belongsTo = [ model : Model ]
	
    static constraints = {
		connector( blank:false, nullable:false, unique:'model' )
		label( blank:true, nullable:true )
		type( blank:true, nullable:true, inList: ["Ether", "Serial", "Power", "Fiber", "SCSI", "USB", "KVM", "Other"] )
		labelPosition( blank:true, nullable:true )
		connectorPosX( blank:true, nullable:true )
		connectorPosY( blank:true, nullable:true )
		status( blank:false, nullable:false)
    }
	
	static mapping  = {	
		version false
		id column:'model_connectors_id'
	}
	
	String toString(){
		"${model.modelName} : ${connector}"
	}
}
