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
	static final List<String> fields = ['tagAssets']

	/**
	 * A map of field names to actions, and to the methods that support them.
	 */
	static final Map actions = [
		tagAssets: [
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
		def service
		def value
		String action

		//Maps field names to services.
		Map fieldToService = [
			tagAssets: tagAssetService
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

		bulkChange.edits.each { EditCommand edit ->
			service = fieldToService[edit.fieldName]
			action = actions[edit.fieldName][edit.action]
			value = service.coerceBulkValue(currentProject, edit.value)

			service."$action"(value, assetIds, assetQueryFilter)
		}
	}
}
