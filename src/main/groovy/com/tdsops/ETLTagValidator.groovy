package com.tdsops

import com.tdsops.etl.ETLProcessorException
import groovy.transform.CompileStatic

/**
 * Validates Tags and works with as an internal cache for an instance of {@code ETLProcessor}
 */
@CompileStatic
class ETLTagValidator {

	private Map<String, Map<String, ?>> tags

	ETLTagValidator() {
		tags = [:]
	}

	ETLTagValidator(Long id, String name, String description) {
		tags = [:]
		addTag(id, name, description)
	}

	ETLTagValidator addTags(List<Map<String, ?>> tags) {
		tags.each { addTag(it.id as Long, it.name as String, it.description as String) }
		return this
	}

	ETLTagValidator addTag(Long id, String name, String description = '') {
		tags[name] = [
			id         : id,
			name       : name,
			description: description
		]
		return this
	}

	/**
	 * Validates if a Tag name is defined in {@code ETLTagValidator#tags} Map.
	 * If it does not contain name value as a key,
	 * an ETLException is thrown.
	 * @param name
	 */
	void validate(String name) {
		if (!tags.containsKey(name)) {
			throw ETLProcessorException.unknownTag(name)
		}
	}
}

