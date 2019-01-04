package com.tdsops.etl

import com.tdsops.tm.enums.ControlType
import net.transitionmanager.domain.Person
import grails.core.GrailsDomainClassProperty

class ETLFieldDefinition {

	private String name
	private String label
	private Class<?> type

	/**
	 * Creates an instance of ETLFieldDefinition base on this structure:
	 * <pre>
	 * [
	 *  constraints: [
	 *      required: required
	 *  ],
	 *  control: String,
	 *  default: '',
	 *  field: 'name',
	 *  imp: 'U',
	 *  label: 'Name',
	 *  order: 0,
	 *  shared: 0,
	 *  show: 0,
	 *  tip: "",
	 *  udf: 0
	 *  ]
	 * </pre>
	 * @param fieldSpec
	 * @see ETLFieldsValidator#lookup(com.tdsops.etl.ETLDomain, java.lang.String)
	 */
	ETLFieldDefinition(Map<String, ?> fieldSpec) {
		this.name = fieldSpec.field
		this.label = fieldSpec.label
		this.type = classForControlType(fieldSpec.control)
	}

	/**
	 * Creates an instance of ETLFieldDefinition using GrailsDomainClassProperty definition.
	 * @param domainProperty an instance of GrailsDomainClassProperty
	 * @see ETLFieldsValidator#lookup(com.tdsops.etl.ETLDomain, java.lang.String)
	 */
	ETLFieldDefinition(GrailsDomainClassProperty domainProperty) {
		this.name = domainProperty.getName()
		// The label will be the same as the field name until the domain has a field specification like that of assets
		this.label = this.name
		this.type = domainProperty.getType()
	}

	/**
	 * Converts ControlType in the correct Class to validate later fields Specs
	 * @return
	 */
	private Class<?> classForControlType(String controlType) {
		if(ControlType.NUMBER.toString() == controlType){
			return Long
		} else if(ControlType.STRING.toString() == controlType){
			return String
		} else if(ControlType.PERSON.toString() == controlType){
			return Person
		} else{
			//throw ETLProcessorException.unknownAssetControlType(controlType)
			return String
		}
	}

	String getName() {
		return name
	}

	void setName(String name) {
		this.name = name
	}

	String getLabel() {
		return label
	}

	void setLabel(String label) {
		this.label = label
	}

	Class<?> getType() {
		return type
	}

	void setType(Class<?> type) {
		this.type = type
	}
}
