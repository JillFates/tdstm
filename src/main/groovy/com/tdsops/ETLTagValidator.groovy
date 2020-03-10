package com.tdsops

import com.tdsops.etl.ETLProcessorException
import groovy.transform.CompileStatic

/**
 * Validates Tags and works with as an internal cache for an instance of {@code ETLProcessor}
 * <pre>
 * 	ETLTagValidator tagValidator = new ETLTagValidator(5l, 'GDPR')
 * 	tagValidator.addTag('HIPPA', 6l)
 * 	tagValidator.addTag('PCI', 7l)
 * 	tagValidator.addTag('SOX', 8l)
 *
 * 	...
 * 	tagValidator.validate('HIPPA')
 * 	tagValidator.validate('PCI')
 *
 * 	tagValidator.validate('Unknown tag') // --> Throws an ETLProcessorException
 * </pre>
 */
@CompileStatic
class ETLTagValidator {

	private Map<String, Long> tags

	ETLTagValidator(Map<String, Long> tags) {
		this.tags = tags
	}

	ETLTagValidator(String name, Long id) {
		tags = [:]
		addTag(name, id)
	}
	/**
	 * Add a list of Tags  {@code ETLTagValidator#tags} Map
	 * @param tags List of Map with Tag name and id
	 * @return Current instance of {@code ETLTagValidator}
	 * @see ETLTagValidator#addTag(java.lang.String, java.lang.Long)
	 */
	ETLTagValidator addTags(List<Map<String, ?>> tags) {
		tags.each { addTag(it.name as String, it.id as Long) }
		return this
	}
	/**
	 * Add A new Tag in {@code ETLTagValidator#tags} Map
	 * @param id a Long value represented a Tag Id
	 * @param name a Tag name
	 * @return Current instance of {@code ETLTagValidator}
	 */
	ETLTagValidator addTag(String name, Long id) {
		tags[name] = id
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

