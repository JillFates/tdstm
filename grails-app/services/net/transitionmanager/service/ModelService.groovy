package net.transitionmanager.service

import com.tds.asset.AssetCableMap
import com.tds.asset.AssetEntity
import com.tdsops.common.exceptions.ServiceException
import com.tdsops.common.sql.SqlUtil
import com.tdsops.tm.enums.domain.AssetCableStatus
import com.tdsops.tm.enums.domain.UserPreferenceEnum as PREF
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.StringUtil
import com.tdssrc.grails.WebUtil
import grails.transaction.Transactional
import net.transitionmanager.domain.Manufacturer
import net.transitionmanager.domain.Model
import net.transitionmanager.domain.ModelAlias
import net.transitionmanager.domain.ModelConnector
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.UserLogin
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap
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
	def mergeModel(Model fromModel, Model toModel){
		//	Revise Asset, and any other records that may point to this model
		int assetUpdated = 0 // assetUpdated flag to count the assets updated by merging models .

		for (AssetEntity assetEntity in AssetEntity.findAllByModel(fromModel)) {
			assetEntity.model = toModel
			assetEntity.assetType = toModel.assetType
			if (assetEntity.save()) {
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
		ModelConnector.executeUpdate('DELETE ModelConnector WHERE model=?', [fromModel])

		GormUtil.copyUnsetValues(toModel, fromModel, notToUpdate)
		save toModel

		// Add to the AKA field list in the target record
		def toModelAlias = ModelAlias.findAllByModel(toModel).name
		if (!toModelAlias.contains(fromModel.modelName)){
			def fromModelAlias = ModelAlias.findAllByModel(fromModel)
			ModelAlias.executeUpdate('DELETE ModelAlias WHERE model=?', [fromModel])

			// Delete model record
			fromModel.delete(flush: true)

			fromModelAlias.each {
				//toModel.findOrCreateAliasByName(it.name, true)
				findOrCreateAliasByName(toModel, it.name, true)
			}
			//merging fromModel as AKA of toModel
			//toModel.findOrCreateAliasByName(fromModel.modelName, true)
			findOrCreateAliasByName(toModel, fromModel.modelName, true)


			String principal = securityService.currentUsername
			if (principal) {
				def user = UserLogin.findByUsername(principal)
				def person = user.person
				int bonusScore = person.modelScoreBonus ?: 0
				if (user) {
					person.modelScoreBonus = bonusScore+10
					person.modelScore = person.modelScoreBonus + person.modelScore
					person.save(flush:true)
				}
			}
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
	def listOfFilteredModels(Map filterParams, String sortColumn, String sortOrder) {

		// Cut the list of fields to filter by down to only the fields the user has entered text into
		def queryParams = [:]
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
		def aliasValuesAggregate = [noOfConnectors: 'COUNT(DISTINCT mc.model_connectors_id)',
		                            assetsCount: 'COUNT(DISTINCT ae.asset_entity_id)']

		// If the user is sorting by a valid column, order by that one instead of the default
		sortColumn = sortColumn && filterParams.containsKey(sortColumn) ? sortColumn : "man.name, m.name"

		def query = new StringBuilder("SELECT ")

		// Add all the columns to the query
		def comma = false
		(aliasValuesBase + aliasValuesAggregate).each {
			query.append("${comma ? ', ' : ''}$it.value AS $it.key")
			comma = true
		}

		// Perform all the needed table joins
		query.append(""" FROM model m
			LEFT OUTER JOIN model_connector mc on mc.model_id = m.model_id
			LEFT OUTER JOIN model_sync ms on ms.model_id = m.model_id
			LEFT OUTER JOIN manufacturer man on man.manufacturer_id = m.manufacturer_id
			LEFT OUTER JOIN asset_entity ae ON ae.model_id = m.model_id
			LEFT OUTER JOIN person p ON p.person_id = m.created_by
			LEFT OUTER JOIN person p1 ON p1.person_id = m.updated_by
			LEFT OUTER JOIN person p2 ON p2.person_id = m.validated_by
			LEFT OUTER JOIN project pr ON pr.project_id = m.model_scope_id""")

		// Handle the filtering by each column's text field for base columns
		def firstWhere = true
		aliasValuesBase.each { k, v ->
			if (queryParams.containsKey(k)) {
				query.append(" ${firstWhere ? ' WHERE' : ' AND'} $v ")

				def aggVal = queryParams[k]
				def expr = 'LIKE'
				(aggVal, expr) = SqlUtil.parseExpression(aggVal, expr)
				if (expr.contains('LIKE')) {
					query.append("$expr CONCAT('%',:$k,'%')")
				} else {
					query.append("$expr :$k")
				}
				queryParams[k] = aggVal
				firstWhere = false
			}
		}

		// Group the models by
		query.append(" GROUP BY modelId ")

		// Handle the filtering by each column's text field for aggregate columns
		def firstHaving = true
		aliasValuesAggregate.each { k, v ->
			if (queryParams.containsKey(k)) {

				// TODO : refactor the query expression parsing <,> into reusable function as it could be used in a number of places

				// Handle <, >, <= or >= options on the numeric filter
				def aggVal = queryParams[k]
				def expr = '='
				(aggVal, expr) = SqlUtil.parseExpression(aggVal, expr)
				if (aggVal.isNumber()) {
					// Need to save the query param without the expression
					queryParams[k] = aggVal
					query.append(" ${firstHaving ? ' HAVING' : ' AND'} $v $expr :$k")
					firstHaving = false
				}
			}
		}

		query << ' ORDER BY ' << sortColumn << ' ' << sortOrder

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
		if (!model && (!modelName || !manufacturer)) {
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
		if (isValidName(model.modelName, model.id, model.manufacturerId) && model.save(flush: true)) {
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

					if (!modelConnector.hasErrors())
						modelConnector.save(flush: true)
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

				if (!powerConnector.save(flush: true)) {
					def errText = "Unable to create Power Connectors for ${model} " +
							GormUtil.allErrorsString(powerConnector)
					log.warn(errText)
				}
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
				if (!modelAlias.save()) {
					modelAlias.errors.allErrors.each { log.error it }
				}
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
					if (!assetCableMap.validate() || !assetCableMap.save()) {
						def errText = "Unable to create assetCableMap for assetEntity $assetEntity" +
								GormUtil.allErrorsString(assetCableMap)
						log.error(errText)
					}
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

				// <SL> Could this be a function?
				if (user) {
					int bonusScore = person?.modelScoreBonus ? person?.modelScoreBonus : 0
					person.modelScoreBonus = bonusScore + 1
					int score = person.modelScore ?: 0
					person.modelScore = score+bonusScore
				}

				if (!person.save(flush:true)) {
					person.errors.allErrors.each { log.error it }
				}

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
			if (!isValid || !alias.save()) {
				throw new ServiceException("AKA or Model with same name already exists: ${name}")
			}
		}
		alias
	}

}
