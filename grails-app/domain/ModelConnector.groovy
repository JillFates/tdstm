
class ModelConnector {
	// Declare propertied
	String connector
	String label
	String type
	String labelPosition
	Integer connectorPosX
	Integer connectorPosY
	String status
	String option
	
	static belongsTo = [ model : Model ]
	
    static constraints = {
		connector( blank:false, nullable:false, unique:'model' )
		label( blank:true, nullable:true )
		type( blank:true, nullable:true, inList: ["Ether", "Serial", "Power", "Fiber", "SCSI", "USB", "KVM", "Other"] )
		labelPosition( blank:true, nullable:true )
		connectorPosX( blank:true, nullable:true )
		connectorPosY( blank:true, nullable:true )
		status( blank:false, nullable:false, inList: ['missing','empty','cabled','cabledDetails'] )
		option( blank:true, nullable:true )
    }
	
	static mapping  = {	
		version false
		id column:'model_connectors_id'
		option column:'connector_option'
	}
	
	String toString(){
		"${model?.modelName} : ${connector}"
	}
}
