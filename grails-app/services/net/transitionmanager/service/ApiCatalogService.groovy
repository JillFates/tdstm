package net.transitionmanager.service

import com.tdssrc.grails.ApiCatalogUtil
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.JsonUtil
import grails.transaction.NotTransactional
import grails.transaction.Transactional
import groovy.util.logging.Slf4j
import net.transitionmanager.command.ApiCatalogCommand
import net.transitionmanager.domain.ApiCatalog
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.Provider
import org.codehaus.groovy.grails.web.json.JSONObject
import org.hibernate.criterion.CriteriaSpecification

@Slf4j
@Transactional
class ApiCatalogService implements ServiceMethods {
	ProviderService providerService

	/**
	 * Validates if an Api Catalog is unique within project and provider
	 * @param name - the name of the api catalog
	 * @param id - the id of the api catalog
	 * @param project - the project
	 * @param provider - the provider
	 * @return true if api catalog is unique or else false
	 */
	boolean validateUnique(String name, Long id, Project project, Provider provider) {
		// Lookup a api catalog with the given name, project and provider
		ApiCatalog apiCatalog = ApiCatalog.where {
			name == name
			project == project
			provider == provider
		}.find()

		boolean unique = true
		if (apiCatalog) {
			// If an api catalog was found, check if the IDs match. If not, the name is not unique.
			if (apiCatalog) {
				if (apiCatalog.id != id) {
					unique = false
				}
			}
		}
		return unique
	}

	/**
	 * Save or update an api catalog
	 * @param command
	 * @return saved or update api catalog
	 * @throws InvalidParamException
	 */
	ApiCatalog saveOrUpdate(ApiCatalogCommand command) {
		String unPrettyDictionaryJson = JsonUtil.validateJson(command.dictionary)
		// do dictionary transformation, if there are error transforming, it will throw an InvalidParamException
		String jsonDictionaryTransformed = ApiCatalogUtil.transformDictionary(unPrettyDictionaryJson)
		// obtain a dictionary json object
		JSONObject jsonDictionary = JsonUtil.parseJson(jsonDictionaryTransformed)

		Project currentProject = securityService.userCurrentProject
		Provider provider = providerService.findOrCreateProvider(jsonDictionary.dictionary.info.provider, currentProject)
		ApiCatalog apiCatalog

		if (command.id) {
			apiCatalog = ApiCatalog.load(command.id)
			if (command.version != apiCatalog.version) {
				throw new DomainUpdateException("The version of the Api Catalog you are trying to update was already updated by another user")
			}
			apiCatalog.name = jsonDictionary.dictionary.info.name
			apiCatalog.dictionary = unPrettyDictionaryJson
		} else {
			apiCatalog = new ApiCatalog(project: currentProject, provider: provider, name: jsonDictionary.dictionary.info.name, dictionary: unPrettyDictionaryJson)
		}

		// validate is unique api catalog, and if not, let's update current
		if (!validateUnique(jsonDictionary.dictionary.info.name, command.id, currentProject, provider)) {
			throw new DomainUpdateException("Cannot update or create Api Catalog because the name is not unique for this project and provider.")
		}

		if (!apiCatalog.save()) {
			throw new DomainUpdateException("Error creating or updating ApiCatalog ${GormUtil.allErrorsString(apiCatalog)}")
		}

		return apiCatalog
	}

	/**
	 * Find an api catalog by id and current user project
	 * @param id - the api catalog id
	 * @return and instance of api catalog
	 */
	ApiCatalog findById(Long id) {
		ApiCatalog.where {
			project == securityService.userCurrentProject
			id == id
		}.get()
	}

	/**
	 * Delete an api catalog by id and current user project
	 * @param id - the api catalog id
	 */
	void deleteById(Long id) {
		ApiCatalog.where {
			project == securityService.userCurrentProject
			id == id
		}.deleteAll()
	}

	/**
	 * List all api catalogs for the current user project sorted by name ascending
	 * @return a list of ApiCatalog instances for the current user project
	 */
	@NotTransactional
	List<ApiCatalog> list() {
		ApiCatalog.where {
			project == securityService.userCurrentProject
		}.list([sort: 'name', order: 'asc'])
	}

	/**
	 * List all catalogs names and ids for the current user project sorted by name ascending.
	 * This is used by the ApiAction CRUD when creating ApiActions
	 *
	 * @return a List containing Maps of catalogs like <id, name>
	 */
	@NotTransactional
	List listCatalogNames() {
		def catalogs = ApiCatalog.withCriteria {
			resultTransformer(CriteriaSpecification.ALIAS_TO_ENTITY_MAP)
			eq('project', securityService.userCurrentProject)
			projections {
				property('id', 'id')
				property('name', 'name')
			}
			order('name', 'asc')
		}
		return catalogs
	}

	/**
	 * Get a list of catalog dictionary methods expected by the ApiAction CRUD
	 * @param catalogId a api catalog id
	 * @return a Map containing a dictionary methods where the key is the "apiMethod" and the value is the
	 * method definition details
	 * @throws InvalidParamException
	 */
	@NotTransactional
	Map getCatalogMethods(Long catalogId) {
		ApiCatalog apiCatalog = findById(catalogId)
		if (!apiCatalog) {
			throw new InvalidParamException("Api Catalog with ID $catalogId, not found")
		}

		return ApiCatalogUtil.getCatalogMethods(apiCatalog.dictionary)
	}
}
