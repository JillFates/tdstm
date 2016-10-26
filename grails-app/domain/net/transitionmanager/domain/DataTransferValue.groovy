package net.transitionmanager.domain

import com.tdssrc.eav.EavAttribute

class DataTransferValue {

	String importValue
	String correctedValue
	String errorText
	Integer rowId
	Integer assetEntityId
	Integer hasError
	EavAttribute eavAttribute

	static belongsTo = [dataTransferBatch: DataTransferBatch]

	static constraints = {
		assetEntityId nullable: true
		correctedValue nullable: true
		correctedValue nullable: true
		errorText nullable: true
		hasError nullable: true
		importValue nullable: true
	}

	static mapping = {
		version false
		columns {
			id column: 'value_id'
			hasError sqlType: 'tinyint'
		}
	}
}
