
class ModelConnectorSync {
	// Declare propertied
	long connectorTempId
	String connector
	String label
	String type
	String labelPosition
	Integer connectorPosX
	Integer connectorPosY
	String status
	String option
	String modelName
	String importStatus
	ModelSyncBatch batch
	long modelTempId
	
	static belongsTo = [ model : ModelSync ]
	
    static constraints = {
		connector( blank:false, nullable:false )
		label( blank:true, nullable:true, unique:'model' )
		type( blank:true, nullable:true, inList: ["Ether", "Serial", "Power", "Fiber", "SCSI", "USB", "KVM","ILO","Management","SAS","Other"] )
		labelPosition( blank:true, nullable:true )
		connectorPosX( nullable:true )
		connectorPosY( nullable:true )
		status( blank:false, nullable:false, inList: ['missing','empty','cabled','cabledDetails'] )
		option( blank:true, nullable:true )
		importStatus(blank:true, nullable:true )
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
