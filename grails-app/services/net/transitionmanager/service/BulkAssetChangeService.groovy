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
	BulkChangeDateService bulkChangeDateService
	BulkChangeStringService bulkChangeStringService
	BulkChangeNumberService bulkChangeNumberService
	BulkChangePersonService bulkChangePersonService
	BulkChangeYesNoService bulkChangeYesNoService

	/**
	 * A list of valid field names
	 */
	static final List<String> fields = [
			'tagAssets',
			'purchaseDate', 'maintExpDate', 'retireDate',
			'application', 'assetName', 'shortName', 'department', 'costCenter', 'maintContract', 'description', 'supportType', 'environment', 'serialNumber', 'assetTag', 'ipAddress', 'os', 'truck', 'cart', 'shelf', 'railType', 'appSme', 'externalRefId',
			'priority', 'purchasePrice', 'usize', 'sourceRackPosition', 'sourceBladePosition', 'targetRackPosition', 'targetBladePosition', 'dependencyBundle', 'size', 'rateOfChange',
			'appOwner', 'modifiedBy',
			'validation'
	]

	/**
	 * A map of field control types to actions, and to the methods that support them.
	 */
	static final Map actions = [
		'asset-tag-selector': [
			add    : 'bulkAdd',
			clear  : 'bulkClear',
			replace: 'bulkReplace',
			remove : 'bulkRemove'
		],
		'date-time-selector': [
			clear  : 'bulkClear',
			replace: 'bulkReplace'
		],
		'string-selector': [
			clear  : 'bulkClear',
			replace: 'bulkReplace'
		],
		'number-selector': [
			clear  : 'bulkClear',
			replace: 'bulkReplace'
		],
		'person-selector': [
			clear  : 'bulkClear',
			replace: 'bulkReplace'
		],
		'yes-no-selector': [
			clear  : 'bulkClear',
			replace: 'bulkReplace'
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
		Map<String,String> types = dataviewService.projectService.customDomainService.fieldToControlMapping(currentProject)
		def service
		String controlType
		def value
		String action

		//Maps field control types to services.
		Map fieldToService = [
			'asset-tag-selector': tagAssetService,
			'date-time-selector': bulkChangeDateService,
			'string-selector': bulkChangeStringService,
			'number-selector': bulkChangeNumberService,
			'person-selector': bulkChangePersonService,
			'yes-no-selector': bulkChangeYesNoService
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
			controlType = types[edit.fieldName]
			service = fieldToService[controlType]
			validateAction(edit.action, controlType)
			action = actions[controlType][edit.action]
			value = service.coerceBulkValue(currentProject, edit.value)

			if ('asset-tag-selector' == controlType) {
				service."$action"(value, assetIds, assetQueryFilter)
			} else {
				if ('clear' == edit.action) {
					service."$action"(edit.fieldName, assetIds, assetQueryFilter)
				} else {
					service."$action"(value, edit.fieldName, assetIds, assetQueryFilter)
				}
			}
		}
	}

	/**
	 * Validate an action based on the control type. If the control type doesn't have a mapping for the action an InvalidParamException is
	 * thrown.
	 *
	 * @param action the action to validate.
	 * @param controlType the control type to validate has the action
	 */
	void validateAction(String action, String controlType){
		if(!(action in BulkAssetChangeService.actions[controlType]?.keySet())){
			throw new InvalidParamException("Bulk update for action $action is invalid")
		}
	}
}
