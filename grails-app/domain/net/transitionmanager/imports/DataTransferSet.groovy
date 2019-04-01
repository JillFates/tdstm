package net.transitionmanager.imports

class DataTransferSet {

	String title
	String transferMode
	String templateFilename
	String setCode
	static hasMany = [dataTransferBatch: DataTransferBatch, dataTransferAttributeMap: DataTransferAttributeMap]

	static constraints = {
		setCode nullable: true
		templateFilename nullable: true
		title blank: false, size: 0..64
		transferMode blank: false, inList: ['I', 'E', 'B']
	}

	static mapping = {
		version false
		columns {
			id column: 'data_transfer_id'
			setCode sqlType: 'VARCHAR(20)'
		}
	}

	String toString() {
		"$id : $title"
	}
}
