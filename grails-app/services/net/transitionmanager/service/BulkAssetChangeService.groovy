package net.transitionmanager.service

import com.tds.asset.AssetEntity
import com.tdsops.tm.enums.domain.AssetClass
import grails.transaction.NotTransactional
import com.tdssrc.grails.GormUtil
import grails.transaction.Transactional
import net.transitionmanager.bulk.change.BulkChangeDate
import net.transitionmanager.bulk.change.BulkChangeList
import net.transitionmanager.bulk.change.BulkChangeMoveBundle
import net.transitionmanager.bulk.change.BulkChangeInteger
import net.transitionmanager.bulk.change.BulkChangePerson
import net.transitionmanager.bulk.change.BulkChangeString
import net.transitionmanager.bulk.change.BulkChangeTag
import net.transitionmanager.bulk.change.BulkChangeYesNo
import net.transitionmanager.command.bulk.BulkChangeCommand
import net.transitionmanager.command.bulk.EditCommand
import net.transitionmanager.domain.MoveBundle
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
	static Map bulkClassMapping = [
		(TagAsset.class.name)  : BulkChangeTag.class,
		(Date.class.name)      : BulkChangeDate.class,
		'String'               : BulkChangeString.class,
		(Integer.class.name)   : BulkChangeInteger.class,
		(Person.class.name)    : BulkChangePerson.class,
		'YesNo'                : BulkChangeYesNo.class,
		'List'                 : BulkChangeList.class,
		'InList'               : BulkChangeList.class,
		(MoveBundle.class.name): BulkChangeMoveBundle.class
	].asImmutable()

	/**
	 * Handles the bulk change json, and delegates the bulk change to the appropriate service.
	 *
	 * @param currentProject used fot checking security.
	 * @param bulkChange the command object that holds the bulk change json.
	 */
	void bulkChange(Project currentProject, BulkChangeCommand bulkChange) {
		List ids = []
		Map queryFilter = [:]
		Map<String, Map<String, Map>> fieldMapping
		List<String> validActions
		AssetClass assetClass = AssetClass.safeValueOf(bulkChange.type)
		Class type = getType(assetClass)

		//For some reason adding the customDomainService causes a dependency loop, and crashed the app so I'm accessing it through the dataviewService
		fieldMapping = dataviewService.projectService.customDomainService.fieldToBulkChangeMapping(currentProject)

		if (bulkChange.allIds) {
			queryFilter = dataviewService.getAssetIdsHql(currentProject, bulkChange.dataViewId, bulkChange.userParams)
		} else {
			ids = bulkChange.ids
			int validAssetCount = AssetEntity.where { id in ids && project == currentProject }.count()

			if (validAssetCount != ids.size()) {
				throw new InvalidParamException("Only $validAssetCount of the ${ids.size()} records specified were found so the changes were not applied. Please repeat the bulk change process accordingly.")
			}
		}

		try {
			List<Map> actions = generateActions(type, bulkChange.edits, assetClass, fieldMapping, currentProject)

			actions.each { Map action ->
				validActions = fieldMapping[assetClass.name()][action.field].bulkChangeActions ?: []

				if (!action.service.ALLOWED_ACTIONS.contains(action.action) && !validActions.contains(action.action)) {
					throw new InvalidParamException("Bulk update action $action, is not configured for $action.field")
				}

				action.service."$action.action"(type, action.value, action.field, ids, queryFilter)
			}
		} catch (InvalidParamException e) {
			throw e
		} catch (Exception e) {
			log.error('An unexpected error occurred while invoking the bulk change action', e)
			throw new DomainUpdateException('An unexpected error occurred while invoking the bulk change action', e)
		}
	}

	/**
	 * Looks up the AssetClass domain, based on a string type
	 *
	 * @param assetClass to look up the domain for.
	 *
	 * @return the domain class for the name passed in
	 */
	@NotTransactional
	Class getType(AssetClass assetClass) {
		Class type = AssetClass.domainClassFor(assetClass)

		switch (type) {
			case AssetClass.domainClassFor(AssetClass.APPLICATION):
			case AssetClass.domainClassFor(AssetClass.DATABASE):
			case AssetClass.domainClassFor(AssetClass.DEVICE):
			case AssetClass.domainClassFor(AssetClass.STORAGE):
				return type
			default:
				throw new InvalidParamException("Bulk change is not setup for $name")
		}
	}

	/**
	 * Looks up the bulkChange Class, for a field, based on the bulkClassMapping mappings.
	 * If no service is found, an InvalidParamException is thrown.
	 *
	 * @param type the class to use to look up what bulk service to use.
	 * @param assetClass the asset class used for looking up the field settings.
	 * @param fieldName The name to get the service for.
	 * @param fieldMapping the field mapping settings.
	 * @param bulkClassMapping the mapping of the name of the service to the wired instance
	 *
	 * @return the class to use for bulk changes
	 */
	@NotTransactional
	private Class getBulkClass(Class type, AssetClass assetClass, String fieldName, Map<String, Map> fieldMapping, Map bulkClassMapping) {
		def property = GormUtil.getDomainPropertyType(type, fieldName)
		String dataType = property.typeName

		if (dataType == String.class.name) {
			dataType = fieldMapping[assetClass.name()][fieldName]?.control
		} else if (dataType == Collection.class.name && fieldName == 'tagAssets') {
			dataType = TagAsset.class.name
		}

		Class service = bulkClassMapping[dataType]

		if (!service) {
			throw new InvalidParamException("Bulk update is not configured for $fieldName")
		}

		return service
	}

	/**
	 * Gets the actions to be run, and validates the values.
	 *
	 * @param type the domain class used to look up the service.
	 * @param edits the edit commands to create actions for.
	 * @param assetClass The assetClass used for looking up the service.
	 * @param fieldMapping the field mapping used to look up the service.
	 * @param currentProject the current project, used to coerse the bulk value.
	 *
	 * @return A list of actions as a map.
	 */
	private List<Map> generateActions(Class type, List<EditCommand> edits, AssetClass assetClass, Map<String, Map> fieldMapping, Project currentProject) {
		def typeInstance = type.newInstance()
		def service
		def value
		String field
		List<Map> actions = []
		boolean hasCustomFields = false

		List<String> fields = edits.collect { EditCommand edit ->
			field = edit.fieldName
			service = getBulkClass(type, assetClass, field, fieldMapping, bulkClassMapping)
			value = service.coerceBulkValue(currentProject, edit.value)
			typeInstance[field] = value

			if (field.startsWith('custom')) {
				hasCustomFields = true
			}

			actions << [service: service, field: field, action: edit.action, value: value]

			return field
		}

		if (hasCustomFields) {
			fields << 'custom1'
		}

		if (!typeInstance.validate(fields)) {
			throw new InvalidParamException(GormUtil.allErrorsString(typeInstance))
		}

		typeInstance.discard()

		return actions
	}
}
