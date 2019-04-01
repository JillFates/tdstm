package net.transitionmanager.action

import com.tdsops.common.security.spring.HasPermission
import com.tdssrc.grails.ApiCatalogUtil
import com.tdssrc.grails.JsonUtil
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.command.ApiCatalogCommand
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.security.Permission
import net.transitionmanager.service.ApiCatalogService

@Secured('isAuthenticated()')
class ApiCatalogController implements ControllerMethods {
	ApiCatalogService apiCatalogService

	/**
	 * Api Catalog manager landing page
	 */
	@HasPermission(Permission.AdminUtilitiesAccess)
	def manager() {
	}

	/**
	 * It accepts an unsaved Api Catalog for transformation and pretty viewing without saving.
	 * @return
	 */
	@HasPermission(Permission.AdminUtilitiesAccess)
	def viewMerged() {
		ApiCatalogCommand command = populateCommandObject(ApiCatalogCommand.class)
		validateCommandObject(command)

		ApiCatalogUtil.validateDictionary(command.dictionary)
		String transformedJson = ApiCatalogUtil.transformDictionary(command)
		renderAsJson([model: [dictionaryTransformed: transformedJson]])
	}

	/**
	 * Save or update an Api Catalog
	 * @return
	 */
	@HasPermission(Permission.AdminUtilitiesAccess)
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
	@HasPermission(Permission.AdminUtilitiesAccess)
	def viewPretty(Long id) {
		ApiCatalog apiCatalog = apiCatalogService.findById(id)

		if (!apiCatalog) {
			return renderErrorJson('ApiCatalog does not exist')
		}

		renderAsJson([model: [
				id: apiCatalog.id,
				version: apiCatalog.version,
				dictionary: JsonUtil.toPrettyJson(JsonUtil.convertJsonToMap(apiCatalog.dictionary)),
				dictionaryTransformed: apiCatalog.dictionaryTransformed
		]].asImmutable())
	}

	/**
	 * Delete an Api Catalog by id
	 * @param id
	 * @return
	 */
	@HasPermission(Permission.AdminUtilitiesAccess)
	def delete(Long id) {
		apiCatalogService.deleteById(id)
		renderSuccessJson()
	}

	/**
	 * List all api catalogs filtered by current user project sorted by name ascending
	 *
	 * @return a collection of api catalogs
	 */
	@HasPermission(Permission.AdminUtilitiesAccess)
	def list() {
		renderAsJson([model: apiCatalogService.list()*.toMap()])
	}

}

