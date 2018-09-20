package net.transitionmanager.service

import com.tds.asset.AssetEntity
import grails.transaction.Transactional
import net.transitionmanager.command.bulk.BulkChangeCommand
import net.transitionmanager.command.bulk.EditCommand
import net.transitionmanager.domain.Project

/**
 * This handles taking in a bulk change json and delegating the bulk change to the appropriate service.
 */
@Transactional
class BulkAssetChangeService implements ServiceMethods {
	TagAssetService tagAssetService
	DataviewService dataviewService

	/**
	 * A list of valid field names
	 */
	//TODO Should be removed, once changes are made to the ui
	@Deprecated
	static final List<String> fields = ['tagAssets']

	/**
	 * A map of field control types to actions, and to the methods that support them.
	 */
	//TODO Should be removed, once changes are made to the ui
	@Deprecated
	static final Map actions = [
		'tagAssetService': [
			add    : 'bulkAdd',
			clear  : 'bulkClear',
			replace: 'bulkReplace',
			remove : 'bulkRemove'
		]
	]

	/**
	 * Handles the bulk change json, and delegates the bulk change to the appropriate service.
	 *
	 * @param currentProject used fot checking security.
	 * @param bulkChange the command object that holds the bulk change json.
	 */
	void bulkChange(Project currentProject, BulkChangeCommand bulkChange) {
		List assetIds = []
		Map assetQueryFilter = [:]

		//For some reason adding the customDomainService causes a dependency loop, and crashed the app so I'm accessing it through the dataviewService
		Map<String,Map> filedMapping = dataviewService.projectService.customDomainService.fieldToBulkChangeMapping(currentProject)
		def service
		def value
		String action

		//Maps field control types to services.
		Map bulkServiceMapping = [
			'tagAssetService': tagAssetService
		]

		if (bulkChange.allAssets) {
			assetQueryFilter = dataviewService.getAssetIdsHql(currentProject, bulkChange.dataViewId, bulkChange.userParams)
		} else {
			assetIds = bulkChange.assetIds

			int validAssetCount = AssetEntity.where{id in assetIds && project == currentProject}.count()

			if(validAssetCount != assetIds.size() ){
				throw new InvalidParamException('Some asset ids, are not part of your project, and may have been deleted.')
			}
		}

		//Looks up and runs all the edits for a bulk change call.
		bulkChange.edits.each { EditCommand edit ->
			service = getService(edit.fieldName, filedMapping, bulkServiceMapping)
			action = getAction(edit.fieldName, edit.action, filedMapping)
			value = service.coerceBulkValue(currentProject, edit.value)

			service."$action"(value, edit.fieldName, assetIds, assetQueryFilter)
		}
	}

	/**
	 * Looks up the bulkChangeService, for a field, based on the bulkServiceMapping mappings.
	 * If no service is found, an InvalidParamException is thrown.
	 *
	 * @param fieldName The name to get the service for.
	 * @param fieldMapping the field mapping settings.
	 * @param bulkServiceMapping the mapping of the name of the service to the wired instance
	 *
	 * @return The wired instance of the bulkChangeService, for the field.
	 */
	private def getService(String fieldName, Map<String, Map> fieldMapping, Map bulkServiceMapping) {
		String serviceName = fieldMapping[fieldName]?.bulkChangeService

		if (!serviceName) {
			throw new InvalidParamException("Bulk update is not configured for $fieldName")
		}

		def service = bulkServiceMapping[serviceName]

		if (!service) {
			throw new InvalidParamException("Bulk update is not configured for $fieldName")
		}

		return service
	}

	/**
	 * Looks up the bulk action to run against the bulk service looked up.
	 * If no valid action is found, an InvalidParamException is thrown.
	 *
	 * @param fieldName The name of the field to look up the bulk action for.
	 * @param action The name of the action to look up/verify.
	 * @param fieldMapping The field settings with will have the actions.
	 *
	 * @return the action to execute
	 */
	private String getAction(String fieldName, String action, Map<String, Map> fieldMapping){
		Map actions = fieldMapping[fieldName]?.bulkChangeActions

		if(!actions){
			throw new InvalidParamException("Bulk update action $action, is not configured for $fieldName")
		}
		String lookedUpAction = actions[action]

		if(!lookedUpAction){
			throw new InvalidParamException("Bulk update action $action, is not configured for $fieldName")
		}

		return lookedUpAction
	}
}
