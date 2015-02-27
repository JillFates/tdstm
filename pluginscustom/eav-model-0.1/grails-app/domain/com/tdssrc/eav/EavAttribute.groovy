package com.tdssrc.eav

class EavAttribute {

	String	attributeCode
	String	backendType
	String	frontendInput
	String	frontendLabel
	String	defaultValue
	String	validation
	Integer	isRequired
	Integer	isUnique
	Integer	sortOrder
	String	note

	static hasMany = [ 
		entityDatatypes  : EavEntityDatatype,
		entityAttributes : EavEntityAttribute,
		attributeOptions : EavAttributeOption
	]

	static belongsTo = [ entityType:EavEntityType ]
	
	static mapping = {
		version false
		columns {
			id column:'attribute_id'
			isRequired sqlType:'tinyint'
			isUnique sqlType:'tinyint'
			sortOrder sqlType:'smallint'
		}
	}

	static constraints = {
		attributeCode( unique:'entityType', blank:false )
		note(size:0..255)
		backendType( blank:false, inList:['datetime', 'decimal', 'int', 'text', 'varchar','String'])
		frontendLabel(size:0..255)
		frontendInput( inList:['text', 'textarea', 'autocomplete', 'checkbox', 'select', 'multiselect', 'radio'])
		defaultValue( )
		validation( )
		isRequired(range:0..1)
		isUnique(range:0..1)
		sortOrder(min:0, max:32767)
	}
}
