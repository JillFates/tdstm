package net.transitionmanager.model

class ModelConnectorSync {

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

	static belongsTo = [model: ModelSync]

	static constraints = {
		connector blank: false
		connectorPosX nullable: true
		connectorPosY nullable: true
		importStatus nullable: true
		label nullable: true, unique: 'model'
		labelPosition nullable: true
		option nullable: true
		status blank: false, inList: ['missing', 'empty', 'cabled', 'cabledDetails']
		type nullable: true, inList: ['Ether', 'Serial', 'Power', 'Fiber', 'SCSI', 'USB',
		                              'KVM', 'ILO', 'Management', 'SAS', 'Other']
	}

	static mapping = {
		version false
		id column: 'model_connectors_id'
		option column: 'connector_option'
	}

	String toString() {
		"${model?.modelName} : $connector"
	}
}
