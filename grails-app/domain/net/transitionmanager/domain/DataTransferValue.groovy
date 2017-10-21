package net.transitionmanager.domain

import com.tdssrc.eav.EavAttribute

class DataTransferValue {

	String importValue
	String correctedValue
	String errorText
	Integer rowId
	Integer assetEntityId
	Integer hasError
	// <SL> TM-6585, TM-6586: remove this attribute when field specs are fully tested and in place
	EavAttribute eavAttribute
	String fieldName

	static belongsTo = [dataTransferBatch: DataTransferBatch]

	static constraints = {
		assetEntityId nullable: true
		correctedValue nullable: true
		correctedValue nullable: true
		errorText nullable: true
		hasError nullable: true
		importValue nullable: true
		eavAttribute nullable: true
	}

	static mapping = {
		version false
		columns {
			id column: 'value_id'
			hasError sqlType: 'tinyint'
		}
	}
}
