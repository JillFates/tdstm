package com.tdsops.tm.enums.domain

import groovy.transform.CompileStatic

@CompileStatic
class ApiCatalogDictionaryKey {
	public static final String DICTIONARY 		= 'dictionary'
	public static final String INFO				= 'info'
	public static final String VARIABLE 		= 'variable'
	public static final String CREDENTIAL 		= 'credential'
	public static final String PARAM_DEF 		= 'paramDef'
	public static final String PARAM_GROUP 		= 'paramGroup'
	public static final String SCRIPT_DEF 		= 'scriptDef'
	public static final String SCRIPT 			= 'script'
	public static final String METHOD 			= 'method'

	// invalid keys
	public static final String AGENT			= 'agent'

	public static final String DICTIONARY_ROOT_ELEMENT = DICTIONARY
	public static final String[] METHOD_KEY = [METHOD]
	public static final String[] COMMON_DICTIONARY_KEYS = [
			INFO,
			VARIABLE,
			CREDENTIAL,
			PARAM_DEF,
			PARAM_GROUP]
	public static final String[] DICTIONARY_PRIMARY_KEYS = COMMON_DICTIONARY_KEYS + METHOD_KEY
	public static final String[] REACTION_SCRIPTS_KEYS = [
			SCRIPT_DEF,
			SCRIPT]
	public static final String[] DICTIONARY_KEYS = COMMON_DICTIONARY_KEYS + REACTION_SCRIPTS_KEYS + METHOD_KEY
	public static final String[] TRANSFORMED_DICTIONARY_UNUSED_KEYS = [
			PARAM_DEF,
			PARAM_GROUP,
			SCRIPT_DEF,
			SCRIPT]
}