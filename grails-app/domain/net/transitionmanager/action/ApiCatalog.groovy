package net.transitionmanager.action

import com.tdssrc.grails.ApiCatalogUtil
import com.tdssrc.grails.JsonUtil
import net.transitionmanager.project.Project
import net.transitionmanager.i18n.Message
import net.transitionmanager.exception.InvalidParamException

class ApiCatalog {
	// The project this Api Catalog belongs to
	Project project

	// The provider this Api Catalog belongs to
	Provider provider

	// The name of the Api Catalog
	String name

	// Contains the JSON dictionary data
	String dictionary

	// Contains the JSON dictionary transformed data
	String dictionaryTransformed

	Long version
	Date dateCreated
	Date lastUpdated

	static belongsTo = [
			project: Project,
			provider: Provider
	]

	static constraints = {
		name size: 1..255, unique: ['project', 'provider']
		dictionary size: 1..65535, blank: false, validator: dictionaryJsonValidator()
		dictionaryTransformed size: 1..65535, blank: false

		provider ofSameProject: true
	}

	static mapping = {
		id column: 'api_catalog_id'
		dictionary sqltype: 'text'  // should be JSON but not sure if it is supported yet
		name sqltype: 'varchar(255)'
	}

	/**
	 * Custom validator for the dictionaryJson that evaluates that:
	 * - The string is a valid JSON.
	 */
	static Closure dictionaryJsonValidator() {
		return { String dictionaryJsonString, ApiCatalog domainObject ->
			String dictionaryJson = null

			try {
				dictionaryJson = JsonUtil.validateJson(dictionaryJsonString)

				// Add additional validations on dictionaryJson
				ApiCatalogUtil.validateDictionary(dictionaryJson)
			} catch(InvalidParamException e) {
				return Message.InvalidFieldForDomain
			}

			// Set to true, otherwise the validation fails.
			return true
		}
	}

	/**
	 * Converts this api catalog object to a map
	 * @return
	 */
	Map toMap() {
		Map data = [
				id						: id,
				project					: [id: project.id, name: project.name],
				provider				: [id: provider.id, name: provider.name],
				name					: name,
				dictionary              : dictionary,
				dictionaryTransformed   : dictionaryTransformed,
				dateCreated     		: dateCreated,
				lastUpdated     		: lastUpdated,
				version                 : version
		]
		return data.asImmutable()
	}
}
