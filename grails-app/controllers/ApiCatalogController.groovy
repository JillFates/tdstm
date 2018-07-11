import com.tdssrc.grails.ApiCatalogUtil
import com.tdssrc.grails.JsonUtil
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.command.ApiCatalogCommand
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.ApiCatalog
import net.transitionmanager.service.ApiCatalogService

@Secured('isAuthenticated()')
class ApiCatalogController implements ControllerMethods {
	ApiCatalogService apiCatalogService

	/**
	 * Api Catalog manager landing page
	 */
	def manager() {
	}

	/**
	 * It accepts an unsaved Api Catalog for transformation and pretty viewing without saving.
	 * @return
	 */
	def viewMerged() {
		ApiCatalogCommand command = populateCommandObject(ApiCatalogCommand.class)
		validateCommandObject(command)

		ApiCatalogUtil.validateDictionaryHasPrimaryKeys(command.dictionary)
		String transformedJson = ApiCatalogUtil.transformDictionary(command)
		renderAsJson([dictionary: transformedJson])
	}

	/**
	 * Save or update an Api Catalog
	 * @return
	 */
	def save() {
		ApiCatalogCommand command = populateCommandObject(ApiCatalogCommand.class)
		validateCommandObject(command)

		ApiCatalog apiCatalog = apiCatalogService.saveOrUpdate(command)
		renderAsJson([model: apiCatalog.toMap()])
	}

	/**
	 * Find and transform an Api Catalog for pretty viewing
	 * @param id
	 * @return
	 */
	def viewPretty(Long id) {
		ApiCatalog apiCatalog = apiCatalogService.findById(id)

		if (!apiCatalog) {
			return renderErrorJson('ApiCatalog does not exist')
		}

		String transformedJson = ApiCatalogUtil.transformDictionary(apiCatalog.dictionary)
		renderAsJson([
				id: apiCatalog.id,
				version: apiCatalog.version,
				dictionary: JsonUtil.toPrettyJson(JsonUtil.convertJsonToMap(apiCatalog.dictionary)),
				jsonDictionaryTransformed: transformedJson
		].asImmutable())
	}

	/**
	 * Delete an Api Catalog by id
	 * @param id
	 * @return
	 */
	def delete(Long id) {
		apiCatalogService.deleteById(id)
		renderSuccessJson()
	}

	/**
	 * List all api catalogs filtered by current user project sorted by name ascending
	 *
	 * @return a collection of api catalogs
	 */
	def list() {
		renderAsJson([model: apiCatalogService.list()*.toMap()])
	}

}

