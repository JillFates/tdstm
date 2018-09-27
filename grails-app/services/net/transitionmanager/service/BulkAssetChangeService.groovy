package net.transitionmanager.service

import com.tds.asset.AssetEntity
import com.tdsops.tm.enums.domain.AssetClass
import grails.transaction.Transactional
import net.transitionmanager.bulk.change.BulkChangeDate
import net.transitionmanager.bulk.change.BulkChangeNumber
import net.transitionmanager.bulk.change.BulkChangePerson
import net.transitionmanager.bulk.change.BulkChangeString
import net.transitionmanager.bulk.change.BulkChangeTag
import net.transitionmanager.bulk.change.BulkChangeYesNo
import net.transitionmanager.command.bulk.BulkChangeCommand
import net.transitionmanager.command.bulk.EditCommand
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.TagAsset

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
	//TODO Should be removed, once changes are made to the ui
	@Deprecated
	static final Map actions = [
		'tagAssetService'   : [
			add    : 'bulkAdd',
			clear  : 'bulkClear',
			replace: 'bulkReplace',
			remove : 'bulkRemove'
		],
		'date-time-selector': [
			clear  : 'bulkClear',
			replace: 'bulkReplace'
		],
		'string-selector'   : [
			clear  : 'bulkClear',
			replace: 'bulkReplace'
		],
		'number-selector'   : [
			clear  : 'bulkClear',
			replace: 'bulkReplace'
		],
		'person-selector'   : [
			clear  : 'bulkClear',
			replace: 'bulkReplace'
		],
		'yes-no-selector'   : [
			clear  : 'bulkClear',
			replace: 'bulkReplace'
		]
	]

	//Maps field control types to services.
	static Map bulkServiceMapping = [
		(TagAsset.class.name): BulkChangeTag,
		(Date.class.name)    : BulkChangeDate,
		'String'             : BulkChangeString,
		(Integer.class.name) : BulkChangeNumber,
		(Person.class.name)  : BulkChangePerson,
		'YesNo'              : BulkChangeYesNo
	]

	/**
	 * Handles the bulk change json, and delegates the bulk change to the appropriate service.
	 *
	 * @param currentProject used fot checking security.
	 * @param bulkChange the command object that holds the bulk change json.
	 */
	void bulkChange(Project currentProject, BulkChangeCommand bulkChange) {
		List ids = []
		Map queryFilter = [:]
		Map<String, Map> filedMapping
		String action
		List<String> actions
		def service
		def value
		AssetClass assetClass = AssetClass.safeValueOf(bulkChange.type)
		def type = AssetClass.domainClassFor(assetClass)

		switch (type) {
			case AssetClass.domainClassFor(AssetClass.APPLICATION):
			case AssetClass.domainClassFor(AssetClass.DATABASE):
			case AssetClass.domainClassFor(AssetClass.DEVICE):
			case AssetClass.domainClassFor(AssetClass.STORAGE):
				//For some reason adding the customDomainService causes a dependency loop, and crashed the app so I'm accessing it through the dataviewService
				filedMapping = dataviewService.projectService.customDomainService.fieldToBulkChangeMapping(currentProject)

				if (bulkChange.allIds) {
					queryFilter = dataviewService.getAssetIdsHql(currentProject, bulkChange.dataViewId, bulkChange.userParams)
				} else {
					ids = bulkChange.ids
					int validAssetCount = AssetEntity.where { id in ids && project == currentProject }.count()

					if (validAssetCount != ids.size()) {
						throw new InvalidParamException('Some asset ids, are not part of your project, and may have been deleted.')
					}
				}

				break
			default:
				throw new InvalidParamException("Bulk change is not setup for $bulkChange.type")
		}

		//Looks up and runs all the edits for a bulk change call.
		bulkChange.edits.each { EditCommand edit ->
			service = getService(type, edit.fieldName, filedMapping, bulkServiceMapping)
			value = service.coerceBulkValue(currentProject, edit.value)
			action = edit.action
			actions = filedMapping[edit.fieldName].bulkChangeActions ?: []

			if (!service.ALLOWED_ACTIONS.contains(action) && !actions.contains(action)) {
				throw new InvalidParamException("Bulk update action $action, is not configured for $edit.fieldName")
			}

			service."$action"(type, value, edit.fieldName, ids, queryFilter)
		}
	}

	/**
	 * Looks up the bulkChangeService, for a field, based on the bulkServiceMapping mappings.
	 * If no service is found, an InvalidParamException is thrown.
	 *
	 * @param type the class to use to look up what bulk service to use.
	 * @param fieldName The name to get the service for.
	 * @param fieldMapping the field mapping settings.
	 * @param bulkServiceMapping the mapping of the name of the service to the wired instance
	 *
	 * @return The wired instance of the bulkChangeService, for the field.
	 */
	private def getService(Class type, String fieldName, Map<String, Map> fieldMapping, Map bulkServiceMapping) {
		def property = type.declaredFields.find{it.name == fieldName} ?: type.superclass.declaredFields.find{it.name == fieldName}

		if (!property) {
			throw new InvalidParamException("Bulk update for invalid field name: $fieldName")
		}

		String dataType = property.type.name

		if (dataType == String.class.name) {
			dataType = fieldMapping[fieldName]?.control
		}else if(dataType == Collection.class.name && fieldName == 'tagAssets'){
			dataType = TagAsset.class.name
		}

		def service = bulkServiceMapping[dataType]

		if (!service) {
			throw new InvalidParamException("Bulk update is not configured for $fieldName")
		}

		return service
	}
}
