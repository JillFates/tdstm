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
		Number matches = ApiCatalog.where {
			name == name
			project == project
			provider == provider
			if (id) {
				id != id
			}
		}.count()

		return matches == 0
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
		// remove unused elements in the transformed dictionary
		ApiCatalogUtil.removeUpUnusedDictionaryTransformedEntries(jsonDictionary)

		Project currentProject = securityService.userCurrentProject
		Provider provider = providerService.findOrCreateProvider(jsonDictionary.dictionary.info.provider, currentProject)
		ApiCatalog apiCatalog

		if (command.id) {
			apiCatalog = ApiCatalog.load(command.id)
			GormUtil.optimisticLockCheck(apiCatalog, command.version, 'Api Catalog')
		} else {
			apiCatalog = new ApiCatalog()
			apiCatalog.project = currentProject
		}

		// populate api catalog properties
		apiCatalog.provider = provider
		apiCatalog.name = jsonDictionary.dictionary.info.name
		apiCatalog.dictionary = unPrettyDictionaryJson
		apiCatalog.dictionaryTransformed = JsonUtil.validateJsonAndConvertToString(jsonDictionary)

		// validate is unique api catalog, and if not, let's update current
		if (!validateUnique(jsonDictionary.dictionary.info.name, command.id, currentProject, provider)) {
			throw new DomainUpdateException("Cannot update or create Api Catalog because the name is not unique for this project and provider.")
		}

		// save or update api catalog
		apiCatalog.save(failOnError: true)

		return apiCatalog
	}

	/**
	 * Find an api catalog by id and current user project
	 * @param id - the api catalog id
	 * @return and instance of api catalog
	 */
	ApiCatalog findById(Long id) {
		return GormUtil.findInProject(securityService.userCurrentProject, ApiCatalog, id, false)
	}

	/**
	 * Delete an api catalog by id and current user project
	 * @param id - the api catalog id
	 */
	void deleteById(Long id) {
      // TODO : JPM 10/2018 : Revert commit to commit where{...}.deleteAll() post the Grails Upgrade (TM-12575)
		ApiCatalog foundApiCatalog = ApiCatalog.where {
			project == securityService.userCurrentProject
			id == id
		}.get()
		if (foundApiCatalog) {
			foundApiCatalog.delete(flush: true)
		}
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
		ApiCatalog apiCatalog = GormUtil.findInProject(securityService.userCurrentProject, ApiCatalog, catalogId, true)
		return ApiCatalogUtil.getCatalogMethods(apiCatalog.dictionaryTransformed)
	}

	/**
	 * Fetch a ApiCatalog from database by name, project and provider
	 * @param name - api catalog name
	 * @param project - project
	 * @param provider - provider
	 * @param throwException - whether to throw or not an exception if api catalog is not found
	 * @return a api catalog instance
	 */
	ApiCatalog getApiCatalog(String name, Project project, Provider provider, boolean throwException = false) {
		// Find an api catalog with the given name for this project and provider.
		ApiCatalog apiCatalog = ApiCatalog.where {
			name == name
			project == project
			provider == provider
		}.get()

		if (! apiCatalog && throwException) {
			throw new EmptyResultException("No ApiCatalog exists with the name $name for the Project $project and Provider $provider.")
		}

		return apiCatalog
	}

	/**
	 * Clone any existing api catalogs and providers associated to sourceProject (if any),
	 * then associate those newly created api catalogs to targetProject.
	 *
	 * @param sourceProject  The project from which the existing api catalogs will be cloned.
	 * @param targetProject  The project to which the new api catalogs will be associated.
	 */
	void cloneProjectApiCatalogs(Project sourceProject, Project targetProject) {
		List<ApiCatalog> apiCatalogs = ApiCatalog.where {
			project == sourceProject
		}.list()

		if (!apiCatalogs.isEmpty()) {
			apiCatalogs.each { ApiCatalog sourceApiCatalog ->
				Provider targetProvider = providerService.getProvider(sourceApiCatalog.provider.name, targetProject, false)
				ApiCatalog newApiCatalog = (ApiCatalog)GormUtil.cloneDomainAndSave(sourceApiCatalog,
						[
							project: targetProject,
							provider: targetProvider
						], false, false)
				log.debug "Cloned api catalog ${newApiCatalog.name} for project ${targetProject.toString()}"
			}
		}
	}
}
