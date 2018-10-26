package net.transitionmanager.agent

import groovy.transform.ToString
import groovy.transform.CompileStatic

/**
 * Represents the properties of a method available in the Dictionary of an agent class
 */
@ToString(includes='name,method,description', includeNames=true, includeFields=true)
@CompileStatic
class DictionaryItem {
	String apiMethod
	String name
	String description=''
	String endpointUrl
	String httpMethod
	String docUrl
	String method
	Integer producesData
	List<LinkedHashMap> params = []
	// List<DictionaryItemParameter> params = []
	Map results = [:]

}

// TODO : JPM 3/2018 : Switch from using Map to DictionaryItemParameter class
class DictionaryItemParameter {
	// Property name of the parameter
	// TODO : JPM 3/2018 : Rename property to name
	String paramName

	// Description of the parameter
	String desc

	// Data type that the parameter should be (TBD) - was the class name
	String type

	// The context object that the mapping of value or param comes from
	ContextType context

	// The name of the context field/property name
	// TODO : JPM 3/2018 : Rename param to fieldName
	String fieldName

	// The value used for ContextType.USER_DEF
	String value

	// Flag to indicated that the parameter is required - block user from deleting from CRUD
	Integer required = 0

	// Flag to indicate that the value/param can not be changed in CRUD
	Integer readonly = 0

	// Flag to indicate that the value entered in CRUD is already encoded (options 0|1, default 0)
	Integer encoded = 0

}