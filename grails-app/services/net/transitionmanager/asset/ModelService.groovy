package net.transitionmanager.asset

import com.tdssrc.grails.NumberUtil
import net.transitionmanager.asset.AssetCableMap
import net.transitionmanager.asset.AssetEntity
import net.transitionmanager.command.ModelCommand
import net.transitionmanager.exception.ServiceException
import com.tdsops.common.sql.SqlUtil
import com.tdsops.tm.enums.domain.AssetCableStatus
import com.tdsops.tm.enums.domain.UserPreferenceEnum as PREF
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.StringUtil
import com.tdssrc.grails.WebUtil
import grails.gorm.transactions.Transactional
import net.transitionmanager.controller.PaginationObject
import grails.web.servlet.mvc.GrailsParameterMap
import net.transitionmanager.manufacturer.Manufacturer
import net.transitionmanager.model.Model
import net.transitionmanager.model.ModelAlias
import net.transitionmanager.model.ModelConnector
import net.transitionmanager.person.Person
import net.transitionmanager.project.Project
import net.transitionmanager.security.UserLogin
import net.transitionmanager.service.ServiceMethods
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

class ModelService implements ServiceMethods {

	private static final List<String> notToUpdate = ['beforeDelete', 'beforeInsert', 'beforeUpdate', 'id',
	                                                 'modelName', 'modelConnectors', 'racks']

	AssetEntityAttributeLoaderService assetEntityAttributeLoaderService
	AssetEntityService assetEntityService
	JdbcTemplate jdbcTemplate
	NamedParameterJdbcTemplate namedParameterJdbcTemplate

	/**
	 * @param fromModel : instance of the model that is being merged
	 * @param toModel : instance of toModel
	 * @return : updated assetCount
	 */
	@Transactional
	def merge(Model fromModel, Model toModel){
		//	Revise Asset, and any other records that may point to this model
		int assetUpdated = 0 // assetUpdated flag to count the assets updated by merging models .

		for (AssetEntity assetEntity in AssetEntity.findAllByModel(fromModel)) {
			assetEntity.model = toModel
			assetEntity.assetType = toModel.assetType
			if (assetEntity.save(failOnError: false)) {
				assetUpdated++
			}
			assetEntityAttributeLoaderService.updateModelConnectors(assetEntity)
		}

		// Delete model associated record
		AssetCableMap.executeUpdate('''
				DELETE AssetCableMap
				WHERE assetFromPort IN (FROM ModelConnector
				                        WHERE model=:fromModel)''',
				[fromModel: fromModel])
		AssetCableMap.executeUpdate('''
				UPDATE AssetCableMap
				SET cableStatus=:status, assetTo=null, assetToPort=null
				WHERE assetToPort IN (FROM ModelConnector
				                      WHERE model=:fromModel)''',
				[status: AssetCableStatus.UNKNOWN, fromModel: fromModel])
		ModelConnector.executeUpdate('DELETE ModelConnector WHERE model=?0', [fromModel])

		GormUtil.copyUnsetValues(toModel, fromModel, notToUpdate)
		save toModel

		// Add to the AKA field list in the target record
		def toModelAlias = ModelAlias.findAllByModel(toModel).name
		if (!toModelAlias.contains(fromModel.modelName)){
			def fromModelAlias = ModelAlias.findAllByModel(fromModel)
			ModelAlias.executeUpdate('DELETE ModelAlias WHERE model=?0', [fromModel])

			// Delete model record
			fromModel.delete(flush: true)

			fromModelAlias.each {
				//toModel.findOrCreateAliasByName(it.name, true)
				findOrCreateAliasByName(toModel, it.name, true)
			}
			//merging fromModel as AKA of toModel
			//toModel.findOrCreateAliasByName(fromModel.modelName, true)
			findOrCreateAliasByName(toModel, fromModel.modelName, true)

			/**/
		} else {
			//	Delete model record
			fromModel.delete()
		}
		// Return to model list view with the flash message "Merge completed."
		return assetUpdated
	}

	/* Used generate the content used to populate the list view
	 * @param Map filterParams a map of all the aliases the user can filter by, and the value the user has entered in each field
	 * @param sortColumn  the alias of the field to sort by
	 * @param sortOrder  the order to sort by. Should be either 'asc' or 'desc'
	 * @return List results the list of rows selected by the query
	 */
	List listOfFilteredModels(Map<String, String> filterParams, PaginationObject paginationObj) {

		String sortOrder = paginationObj.paginationSortOrder('sord')

		// Cut the list of fields to filter by down to only the fields the user has entered text into
		Map queryParams = [:]
		filterParams.each { k, v -> if (v?.trim()) queryParams[k] = v }

		// These values are mapped to real columns in the database, so they can be used in the WHERE clause
		Map<String, String> aliasValuesBase = [
			modelName: 'm.name', manufacturer: 'man.name',  sourceTDSVersion: 'm.sourcetdsversion',
			sourceTDS: 'm.sourcetds', modelStatus:'m.model_status', modelId: 'm.model_id']
		Map modelPref = assetEntityService.getExistingPref(PREF.Model_Columns)
		def modelPrefVal = modelPref.collect { it.value }

		modelPrefVal.each {
			def dbValue = WebUtil.splitCamelCase(it)
			if (!(it in [ 'modelConnectors' , 'createdBy', 'updatedBy', 'validatedBy','modelScope','sourceURL'])) {
				aliasValuesBase[it] = 'm.' + dbValue
			}
			if (it == 'createdBy') {
				aliasValuesBase[it] = 'CONCAT(CONCAT(p.first_name, " "), IFNULL(p.last_name,""))'
			}
			else if (it == 'updatedBy') {
				aliasValuesBase[it] = 'CONCAT(CONCAT(p1.first_name, " "), IFNULL(p1.last_name,""))'
			}
			else if (it == 'validatedBy') {
				aliasValuesBase[it] = 'CONCAT(CONCAT(p2.first_name, " "), IFNULL(p2.last_name,""))'
			}
			else if (it == 'modelScope') {
				aliasValuesBase[it] = 'pr.project_code'
			}
			else if (it == 'sourceURL') {
				aliasValuesBase[it] = 'm.sourceurl'
			}
		}

		// These values are mapped to derived columns, so they will be used in the HAVING clause if included in the filter
		Map aliasValuesAggregate = [
			modelConnectors: 'COUNT(DISTINCT mc.model_connectors_id)',
			assetsCount: 'COUNT(DISTINCT ae.asset_entity_id)'
		]

		// If the user is sorting by a valid column, order by that one instead of the default
		// Grab the potential ORDER BY property or default to modelName
		String sortColumn = paginationObj.params.sidx ?: null
		if (sortColumn) {
			if (! (filterParams.containsKey(sortColumn) || aliasValuesAggregate.containsKey(sortColumn)) ) {
				throw paginationObj.PAGINATION_INVALID_ORDER_BY_EXCEPTION
			}
		} else {
			sortColumn = 'man.name, m.name'
		}

		StringBuilder query = new StringBuilder("SELECT ")

		String columnList = (aliasValuesBase + aliasValuesAggregate)
			.collect { "$it.value AS $it.key" }
			.join(', ')
		query << columnList

		// Perform all the needed table joins
		query.append("""
			FROM model m
			LEFT OUTER JOIN model_connector mc on mc.model_id = m.model_id
			LEFT OUTER JOIN model_sync ms on ms.model_id = m.model_id
			LEFT OUTER JOIN manufacturer man on man.manufacturer_id = m.manufacturer_id
			LEFT OUTER JOIN asset_entity ae ON ae.model_id = m.model_id
			LEFT OUTER JOIN person p ON p.person_id = m.created_by
			LEFT OUTER JOIN person p1 ON p1.person_id = m.updated_by
			LEFT OUTER JOIN person p2 ON p2.person_id = m.validated_by
			LEFT OUTER JOIN project pr ON pr.project_id = m.model_scope_id
		""")

		// Handle the filtering by each column's text field for base columns
		Boolean firstWhere = true
		aliasValuesBase.each { k, v ->
			if (queryParams.containsKey(k)) {
				firstWhere = SqlUtil.addWhereOrAndToQuery(query, firstWhere)
				query.append("$v ")

				def aggVal = queryParams[k]
				def expr = 'LIKE'
				(aggVal, expr) = SqlUtil.parseExpression(aggVal, expr)
				if (expr.contains('LIKE')) {
					query.append("$expr CONCAT('%',:$k,'%')")
				} else {
					query.append("$expr :$k")
				}
				queryParams[k] = aggVal
			}
		}

		// Group the models by
		query.append("\nGROUP BY modelId ")

		// Handle the filtering by each column's text field for aggregate columns
		boolean firstHaving = true
		aliasValuesAggregate.each { k, v ->
			if (queryParams.containsKey(k)) {
				// Handle <, >, <= or >= options on the numeric filter
				def aggVal = queryParams[k]
				String expr = '='
				(aggVal, expr) = SqlUtil.parseExpression(aggVal, expr)
				if (aggVal.isNumber()) {
					// Need to save the query param without the expression
					queryParams[k] = aggVal
					query.append(" ${firstHaving ? ' HAVING' : ' AND'} $v $expr :$k")
					firstHaving = false
				}
			}
		}

		query << "\nORDER BY $sortColumn $sortOrder"

		if (queryParams) {
			namedParameterJdbcTemplate.queryForList(query.toString(), queryParams)
		}
		else {
			jdbcTemplate.queryForList(query.toString())
		}
	}

	/**
	 * 1. Model name must be unique within same manufacturer
	 * 2. Model name must not duplicate any AKA name of the same manufacturer
	 *
	 * @param modelName
	 * @param modelId
	 * @param manufacturerId
	 * @return
	 */
	boolean isValidName(String modelName, Long modelId, Long manufacturerId) {
		// rule #1
		int count = Model.where {
			modelName == modelName
			manufacturer.id == manufacturerId
			if (modelId) {
				id != modelId
			}
		}.count()

		if (count == 0) {
			// rule #2
			count = ModelAlias.where {
				name == modelName
				manufacturer.id == manufacturerId
			}.count()

			if (count == 0) {
				return true
			} else {
				String error = "Model name (${modelName}) duplicates an existing AKA."
				throw new ServiceException(error)
			}

		} else {
			String error = "Model name (${modelName}) is not unique within the same manufacturer."
			throw new ServiceException(error)
		}
	}

	/**
	 * Validates whether the given alias is valid for the given model
	 * @param newAlias, the alias to be added
	 * @param model, the model this alias is being applied to
	 * @param allowLocalDuplicates, if true, the alias will not be checked against this model's current aliases
	 * @param manufacturer, if given, the alias will validated using this manufacturer instead of the given model's current manufacturer
	 * @param modelName, if given, the alias will validated using this name instead of the given model's current name
	 * @return true if the alias is valid for the given parameters
	 */
	@Transactional
	boolean isValidAlias (String newAlias, Model model, boolean allowLocalDuplicates = false, Manufacturer manufacturer = null, String modelName = null) {

		// if there wasn't enough information supplied
		if (!model && (!modelName && !manufacturer)) {
			return false
		}

		modelName = modelName ?: model.modelName
		manufacturer = manufacturer ?: model.manufacturer

		// check if the alias matches the model name
		if (newAlias == modelName) {
			return false
		}

		// check if there is another model from the same manufacturer with this alias as their name
		def modelsWithName = Model.createCriteria().list {
			eq('modelName', newAlias)
			eq('manufacturer', manufacturer)
			if (model)
				ne('modelName', model.modelName)
		}

		if (modelsWithName.size() > 0) {
			return false
		}

		// check if there is a model from this manufacturer already using this alias
		def modelsWithAlias = ModelAlias.createCriteria().list {
			eq('name', newAlias)
			eq('manufacturer', manufacturer)
			if (allowLocalDuplicates && model)
				ne('model', model)
		}

		if (modelsWithAlias) {
			return false
		}

		// if all the tests were passes, this is a valid alias
		return true
	}

	@Transactional
	boolean save(Model model, GrailsParameterMap params) {
		if (isValidName(model.modelName, model.id, model.manufacturerId) && model.save(flush: true, failOnError: false)) {
			int connectorCount = params.int("connectorCount", 0)
			if (connectorCount > 0) {
				for (int i = 1; i <= connectorCount; i++) {
					def modelConnector = new ModelConnector(model: model,
							connector: params['connector' + i],
							label: params['label' + i],
							type: params['type' + i],
							labelPosition: params['labelPosition' + i],
							connectorPosX: params.int("connectorPosX${i}", 0),
							connectorPosY: params.int("connectorPosY${i}", 0),
							status: params['status' + i])

					if (!modelConnector.hasErrors()) {
						modelConnector.save(flush: true)
					}
				}
			} else {
				def powerConnector = new ModelConnector(model: model,
						connector: 1,
						label: "Pwr1",
						type: "Power",
						labelPosition: "Right",
						connectorPosX: 0,
						connectorPosY: 0,
						status: "missing"
				)

				powerConnector.save(flush: true)
			}

			model.sourceTDSVersion = 1
			model.save(flush: true)
			List<String> akaNames = params.list('aka')
			akaNames.each { aka ->
				if (!StringUtil.isBlank(aka)) {
					findOrCreateAliasByName(model, aka, true)
				}
			}
			return true
		} else {
			return false
		}
	}

	@Transactional
	boolean update(Model model, GrailsParameterMap params) {
		if (isValidName(model.modelName, model.id, model.manufacturer.id) && model.save(flush: true)) {
			String deletedAka = params.deletedAka
			if (deletedAka) {
				List<Long> maIds = deletedAka.split(",").collect() { it as Long }
				ModelAlias.executeUpdate("delete ModelAlias where id in :maIds", [maIds: maIds])
			}

			def modelAliasList = ModelAlias.findAllByModel(model)
			modelAliasList.each { modelAlias ->
				modelAlias.name = params["aka_${modelAlias.id}"]
				modelAlias.save()
			}

			List<String> akaToSave = params.list('aka')
			akaToSave.each { String aka ->
				findOrCreateAliasByName(model, aka, true)
			}

			def connectorCount = params.int("connectorCount", 0)
			if (connectorCount > 0) {
				for(int i=1; i<=connectorCount; i++) {
					def connector = params["connector${i}"]
					ModelConnector modelConnector = ModelConnector.findByModelAndConnector(model, connector ?: i)
					if (!connector && modelConnector) {
						modelConnector.delete(flush:true)
					} else {
						if (modelConnector) {
							modelConnector.connector = connector
							modelConnector.label = params["label${i}"]
							modelConnector.type = params["type${i}"]
							modelConnector.labelPosition = params["labelPosition${i}"]
							modelConnector.connectorPosX = params.int("connectorPosX${i}", 0)
							modelConnector.connectorPosY = params.int("connectorPosY${i}", 0)
							modelConnector.status = params["status${i}"]
						} else if (connector) {
							modelConnector = new ModelConnector(
									model: model,
									connector: connector,
									label: params["label${i}"],
									type: params["type${i}"],
									labelPosition: params["labelPosition${i}"],
									connectorPosX: params.int("connectorPosX${i}", 0),
									connectorPosY: params.int("connectorPosY${i}", 0),
									status: params["status${i}"])
						}
						if (modelConnector && !modelConnector.hasErrors()) {
							modelConnector.save(flush: true)
						}
					}
				}
			}

			def assetEntitiesByModel = AssetEntity.findAllByModel(model)
			def assetConnectors = ModelConnector.findAllByModel(model)
			assetEntitiesByModel.each { assetEntity ->
				assetConnectors.each { connector ->
					def assetCableMap = AssetCableMap.findByAssetFromAndAssetFromPort(assetEntity, connector)
					if (!assetCableMap) {
						assetCableMap = new AssetCableMap(
								cable : "Cable"+connector.connector,
								assetFrom: assetEntity,
								assetFromPort : connector,
								cableStatus : connector.status,
								cableComment : "Cable"+connector.connector)
					}
					if (assetEntity?.rackTarget && connector.type == "Power" &&
							connector.label?.toLowerCase() == 'pwr1' && !assetCableMap.toPower) {
						assetCableMap.assetToPort = null
						assetCableMap.toPower = "A"
						assetCableMap.cableStatus= connector.status
						assetCableMap.cableComment= "Cable"
					}

					assetCableMap.save()
				}

				def assetCableMaps = AssetCableMap.findAllByAssetFrom(assetEntity)
				assetCableMaps.each {assetCableMap ->
					ModelConnector assetFromPort = assetCableMap.assetFromPort
					if (assetFromPort && !assetConnectors.id?.contains(assetFromPort.id)) {
						AssetCableMap.executeUpdate("""
								update AssetCableMap
								set cableStatus=:cableStatus, assetTo=null, assetToPort=null
								where assetToPort=:assetToPort
							""", [cableStatus: AssetCableStatus.UNKNOWN, assetToPort: assetCableMap.assetFromPort])

						AssetCableMap.executeUpdate("delete AssetCableMap where assetFromPort = :assetFromPort", [assetFromPort: assetFromPort])
					}
				}
			}

			// <SL> should we use AssetEntityService?
			AssetEntity.executeUpdate("update AssetEntity ae set ae.assetType = :at where ae.model.id = :mId", [at: model.assetType, mId: model.id])

			if (model.sourceTDSVersion) {
				model.sourceTDSVersion ++
			} else {
				model.sourceTDSVersion = 1
			}
			model.save(flush: true)

			return true
		} else {
			return false
		}
	}

	@Transactional
	boolean delete(Model model) {
		AssetEntity modelRef = AssetEntity.findByModel(model)
		if (!modelRef) {
			if (model) {
				UserLogin user = securityService.userLogin
				Person person = user?.person

				// <SL> should we use AssetEntityService?
				AssetEntity.executeUpdate('update AssetEntity set model=null where model.id = :modelId', [modelId: model.id])
				ModelAlias.executeUpdate('delete ModelAlias where model.id = :modelId', [modelId: model.id])
				model.delete(flush: true)

				person.save(flush:true)

				return true
			} else {
				throw new ServiceException("Model not found with Id.")
			}
		} else{
			throw new ServiceException("Model ${model} can not be deleted, it is referenced.")
		}
	}

	/**
	 * Get a ModelAlias object by name and create one (optionally) if it doesn't exist
	 * @param name  name of the model alias
	 * @param createIfNotFound  optional flag to indicating if record should be created (default false)
	 */
	ModelAlias findOrCreateAliasByName(Model model, String name, boolean createIfNotFound = false) {
		name = name.trim()
		ModelAlias alias = ModelAlias.findByNameAndModel(name, model)
		if (!alias && createIfNotFound) {
			def isValid = isValidAlias(name, model)
			alias = new ModelAlias(name: name, model: model, manufacturer: model.manufacturer)
			if (!isValid || !alias.save(failOnError: false)) {
				throw new ServiceException("AKA or Model with same name already exists: ${name}")
			}
		}
		alias
	}

    /**
     * Merge a list of Models referenced by {@code fromIds} to the target Model referenced by {@code toId}.
	 * It also receives a {@code toModelProperties} param, containing new property values entered by the user
	 * in the merge process to be saved in the resulting Model (this may however have no new values to save).
	 *
     * @param fromIds  The list of Model ids of the Models to be merged in the target model.
     * @param toId  The id of the target Model where everything will be merged into.
     * @param toModelProperties  The list of modified properties to be saved into the target Model,
     * in case the user wants to modify any values before merging.
     * @return  A Map with the process result:
	 * 				{@code toModel}  The resulting merged Model instance. If the process failed, this contains the validation errors
	 * 				{@code mergedModels}  The list of Models that were merged into {@code toModel}
	 * 				{@code assetsUpdated}  The number of assets that were updated in the process
     */
    Map mergeModels(List fromIds, Long toId, toModelProperties) {
        Model toModel = Model.get(toId)
        if (!toModel) {
            throw new ServiceException("ModelService.mergeModels() - No Model found with id $toId")
        }
        if (!fromIds) {
            throw new ServiceException('ModelService.mergeModels() - fromIds list cannot be empty')
        }
        List mergedModels = []
        String msg = ""
        int assetsUpdated = 0
        //Saving toModel before merge
        toModel.properties = toModelProperties
		toModel.save(flush: true)

		fromIds.each {
			def fromModel = Model.get(it)
			assetsUpdated += merge(fromModel, toModel)
			mergedModels << fromModel
		}

		return [toModel: toModel, mergedModels: mergedModels, assetsUpdated: assetsUpdated]
    }

	/**
	 * Create or Update a Model instance based on the given command object.
	 * @param project - user's current project.
	 * @param modelCommand
	 * @return the model (created or updated)
	 */
	Model createOrUpdateModel(Project project, ModelCommand modelCommand) {
		Model model = (Model) GormUtil.populateDomainFromCommand(project, Model, modelCommand.id, modelCommand, null, true)

		if (modelCommand.powerType && modelCommand.powerType.equalsIgnoreCase('Amps')) {
			model.powerNameplate = NumberUtil.toInteger(model.powerNameplate, 0) * 120
			model.powerDesign = NumberUtil.toInteger(model.powerDesign, 0) * 120
			model.powerUse = NumberUtil.toInteger(model.powerUse, 0) * 120
		}

		if (modelCommand.modelStatus == 'valid') {
			model.validatedBy = securityService.loadCurrentPerson()
		}

		model.save()

		if (modelCommand.aka) {
			deleteAkas(model, modelCommand)
			createOrUpdateAkas(model, modelCommand)
		}
		if (modelCommand.connectors) {
			deleteConnectors(model, modelCommand)
			createOrUpdateConnectors(model, modelCommand)
		}
		
		return model
	}

	/**
	 *
	 * @param model
	 * @param modelCommand
	 */
	private void createOrUpdateConnectors(Model model, ModelCommand modelCommand) {
		List<Map> connectorsMap = modelCommand.connectors.added + modelCommand.connectors.edited
		for (Map connectorInfo in connectorsMap) {
			ModelConnector modelConnector
			if (connectorInfo.id > 0) {
				modelConnector = ModelConnector.where {
					id == connectorInfo.id
					model == model
				}.find()
			} else {
				modelConnector = new ModelConnector([model: model])
			}
			modelConnector.with {
				label = connectorInfo.label
				type = connectorInfo.type
				labelPosition = connectorInfo.labelPosition
				connectorPosX = connectorInfo.xPosition
				connectorPosY = connectorInfo.yPosition
				connector = connectorInfo.connector
			}

			if (connectorInfo.status) {
				modelConnector.status = connectorInfo.status
			}

			modelConnector.save()
		}
	}

	/**
	 * Delete all connectors of this model marked for deletion.
	 * @param model
	 * @param modelCommand
	 */
	private void deleteConnectors(Model model, ModelCommand modelCommand) {
		if (modelCommand.connectors?.deleted) {
			List<Long> connectorIds = modelCommand.connectors.deleted.collect { Map connectorInfo -> connectorInfo.id}
			ModelConnector.where {
				model == model
				id in connectorIds
			}.deleteAll()
		}
	}

	/**
	 * Create or update existing Model Aliases based on the information available
	 * in the Command Object instance.
	 * @param model
	 * @param modelCommand
	 */
	private void createOrUpdateAkas(Model model, ModelCommand modelCommand) {
		List<Map> akasMap = modelCommand.aka.added + modelCommand.aka.edited
		for (Map akaInfo in akasMap) {
			ModelAlias modelAlias
			if (akaInfo.id > 0) {
				modelAlias = ModelAlias.where {
					id == akaInfo.id
					model == model
				}.find()
			} else {
				modelAlias = new ModelAlias([model: model])
			}
			modelAlias.with {
				manufacturer = model.manufacturer
				name = akaInfo.name
			}
			modelAlias.save()
		}
	}

	/**
	 * Delete all the Model Aliases marked for deletion.
	 * @param model
	 * @param modelCommand
	 */
	private void deleteAkas(Model model, ModelCommand modelCommand) {
		if (modelCommand.aka?.deleted) {
			List<Long> akaIds = modelCommand.aka.deleted.collect { Map akaInfo -> akaInfo.id}
			ModelAlias.where {
				model == model
				id in akaIds
			}.deleteAll()
		}
	}
}
