package net.transitionmanager.service

import com.tds.asset.AssetCableMap
import com.tds.asset.AssetEntity
import com.tdsops.common.exceptions.ServiceException
import com.tdsops.common.sql.SqlUtil
import com.tdsops.tm.enums.domain.AssetCableStatus
import com.tdsops.tm.enums.domain.UserPreferenceEnum as PREF
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.StringUtil
import com.tdssrc.grails.TimeUtil
import com.tdssrc.grails.WebUtil
import com.tdssrc.grails.WorkbookUtil
import grails.transaction.Transactional
import net.transitionmanager.domain.Manufacturer
import net.transitionmanager.domain.ManufacturerSync
import net.transitionmanager.domain.Model
import net.transitionmanager.domain.ModelAlias
import net.transitionmanager.domain.ModelConnector
import net.transitionmanager.domain.ModelSync
import net.transitionmanager.domain.ModelSyncBatch
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.UserLogin
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.codehaus.groovy.grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.web.multipart.commons.CommonsMultipartFile

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

    /**
     * Validates a {@code Model}.
     * Sets the modelStatus property to "valid" and validatedBy to the current Person.
     * If the model is not found, throws a {@code ServiceException}.
     * If saving the Model instance generates errors, they will be returned
     * in the Model instance.
     * @param modelId  The id of the model to validate
     * @return  The Model instance (it can contain errors if something went wrong)
     */
	@Transactional
	Model validateModel(Long modelId) {
		def modelInstance = Model.get(modelId)
		if (!modelInstance) {
			throw new ServiceException("ModelService.validateModel() - No Model found with id $modelId")
		}
        if (securityService.loggedIn) {
            modelInstance.validatedBy = securityService.loadCurrentPerson()
            modelInstance.modelStatus = "valid"
            if (!modelInstance.save(flush:true)) {
                modelInstance.errors.allErrors.each { println it }
            }
        }
        return modelInstance
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
        if (!toModel.save(flush:true)) {
            toModel.errors.allErrors.each {println it }
        } else {
			fromIds.each {
				def fromModel = Model.get(it)
				assetsUpdated += merge(fromModel, toModel)
				mergedModels << fromModel
			}
		}
        return [toModel: toModel, mergedModels: mergedModels, assetsUpdated: assetsUpdated]
    }

	/**
	 *1. On upload the system should put the data into temporary tables and then perform validation to make sure the data is proper and ready.
	 *2. Step through each imported model:
	 *2a if it's SourceTDSVersion is higher than the one in the database, update the database with the new model and connector data.
	 *2b If it is lower, skip it.
	 *3. Report the number of Model records updated.
	 *
	 * @param file
	 * @param userLogin
	 * @param importCheckbox
	 * @return  A Map with the process result:
	 * 		{@code manuAdded}, {@code manuSkipped}  The results of the manufacturer sheet upload process.
	 * 		{@code modelAdded}, {@code modelSkipped}   The results of the model sheet upload process.
	 * 		{@code connectorAdded}, {@code connectorSkipped}   The results of the connector sheet upload process.
	 */
	Map upload(CommonsMultipartFile file, UserLogin userLogin, def importCheckbox) {
		Map results = [
					   manuAdded: 0, manuSkipped: 0,
					   modelAdded: 0, modelSkipped: 0,
					   connectorAdded: 0, connectorSkipped: 0,
					   error:''
		]
		def date = new Date()
		def modelSyncBatch = new ModelSyncBatch(changesSince:date,createdBy:userLogin,source:"TDS")
		if (modelSyncBatch.hasErrors() || !modelSyncBatch.save()) {
			log.error "Unable to create ModelSyncBatch for $modelSyncBatch : ${GormUtil.allErrorsString(modelSyncBatch)}"
		}
		// create workbook
		def workbook
		def sheetNameMap = [:]
		//get column name and sheets
		sheetNameMap.manufacturer = ["manufacturer_id", "name", "aka", "description"]
		sheetNameMap.model = ["model_id", "name","aka", "description", "manufacturer_id", "manufacturer_name",
							  "asset_type", "blade_count", "blade_label_count", "blade_rows", "sourcetds",
							  "power_nameplate", "power_design", "power_use", "sourcetdsversion", "use_image",
							  "usize", "height", "weight", "depth", "width", "layout_style", "product_line",
							  "model_family", "end_of_life_date", "end_of_life_status", "created_by", "updated_by",
							  "validated_by", "sourceurl", "model_status", "model_scope"]
		sheetNameMap.connector = ["model_connector_id", "connector", "connector_posx", "connector_posy", "label",
								  "label_position", "model_id", "model_name", "connector_option", "status", "type"]

		workbook = new HSSFWorkbook(file.inputStream)
		def sheetNames = WorkbookUtil.getSheetNames(workbook)
		def sheets = sheetNameMap.keySet()
		def missingSheets = []
		def flag = 1
		def sheetsLength = sheets.size()

		sheets.each {
			if (!sheetNames.contains(it)) {
				flag = 0
				missingSheets<< it
			}
		}
		if (flag == 0) {
			results.error = "$missingSheets sheets not found, aborting upload process."
			return results
		} else {
			def sheetColumnNames = [:]
			//check for column
			def manuSheet = workbook.getSheet("manufacturer")
			def manuTotalCols = WorkbookUtil.getColumnsCount(manuSheet)
			for (int c = 0; c < manuTotalCols; c++) {
				def cellContent = WorkbookUtil.getStringCellValue(manuSheet, c, 0)
				sheetColumnNames.put(cellContent, c)
			}
			def missingHeader = checkHeader(sheetNameMap.get("manufacturer"), sheetColumnNames)
			// Statement to check Headers if header are not found it will return Error message
			if (missingHeader != "") {
				results.error = " Column Headers : $missingHeader not found, Please check it."
				return results
			} else {
				def sheetRows = manuSheet.getLastRowNum()
				for (int r = 1; r < sheetRows ; r++) {
					def valueList = new StringBuilder("(")
					for(int cols = 0; cols < manuTotalCols; cols++) {
						valueList.append("'"+WorkbookUtil.getStringCellValue(manuSheet, cols, r, "").replace("'","\\'")+"',")
					}
					try{
						jdbcTemplate.update("insert into manufacturer_sync(manufacturer_temp_id, name,aka, description, batch_id) values " +
								valueList + modelSyncBatch.id + ')')
						results.manuAdded = r
					} catch (e) {
						log.error "Can't insert into manufacturer_sync: $e.message"
						results.manuSkipped += 1
					}
				}
			}

			/*
			 * Import Model Information
			 */
			def modelSheetColumnNames = [:]
			//check for column
			def modelSheet = workbook.getSheet("model")
			def modelTotalCols = WorkbookUtil.getColumnsCount(modelSheet)
			//def colContain = modelCol.
			for (int c = 0; c < modelTotalCols; c++) {
				def cellContent = WorkbookUtil.getStringCellValue(modelSheet, c, 0)
				modelSheetColumnNames.put(cellContent, c)
			}
			missingHeader = checkHeader(sheetNameMap.get("model"), modelSheetColumnNames)
			def onlyTds
			// Statement to check Headers if header are not found it will return Error message
			if (missingHeader != "") {
				results.error = " Column Headers : $missingHeader not found, Please check it."
				return results
			} else {
				def sheetRows = modelSheet.getLastRowNum()
				for (int r = 1; r < sheetRows ; r++) {
					onlyTds = false
					def valueList = new StringBuilder("(")
					def manuId
					def createdPersonId
					def updatedPersonId
					def validatedPersonId
					def projectId
					for(int cols = 0; cols < modelTotalCols; cols++) {
						switch(WorkbookUtil.getStringCellValue(modelSheet, cols, 0)) {
							case "manufacturer_name" :
								def manuName = WorkbookUtil.getStringCellValue(modelSheet, cols, r)
								manuId = ManufacturerSync.findByNameAndBatch(manuName,modelSyncBatch)?.id
								valueList.append("'"+WorkbookUtil.getStringCellValue(modelSheet, cols, r, "").replace("'","\\'")+"',")
								break
							case "blade_count" :
								valueList.append((WorkbookUtil.getStringCellValue(modelSheet, cols, r) ? WorkbookUtil.getStringCellValue(modelSheet, cols, r) : null)+",")
								break
							case "blade_label_count" :
								valueList.append((WorkbookUtil.getStringCellValue(modelSheet, cols, r) ? WorkbookUtil.getStringCellValue(modelSheet, cols, r) : null)+",")
								break
							case "blade_rows" :
								valueList.append((WorkbookUtil.getStringCellValue(modelSheet, cols, r) ? WorkbookUtil.getStringCellValue(modelSheet, cols, r) : null)+",")
								break
							case "use_image" :
								int useImage = 0
								if (WorkbookUtil.getStringCellValue(modelSheet, cols, r).toLowerCase() != "no") {
									useImage = 1
								}
								valueList.append(useImage+",")
								break
							case "power_nameplate" :
								valueList.append((WorkbookUtil.getStringCellValue(modelSheet, cols, r) ? WorkbookUtil.getStringCellValue(modelSheet, cols, r) : null)+",")
								break
							case "power_design" :
								valueList.append((WorkbookUtil.getStringCellValue(modelSheet, cols, r) ? WorkbookUtil.getStringCellValue(modelSheet, cols, r) : null)+",")
								break
							case "power_use" :
								valueList.append((WorkbookUtil.getStringCellValue(modelSheet, cols, r) ? WorkbookUtil.getStringCellValue(modelSheet, cols, r) : null)+",")
								break
							case "usize" :
								valueList.append((WorkbookUtil.getStringCellValue(modelSheet, cols, r) ? WorkbookUtil.getStringCellValue(modelSheet, cols, r) : null)+",")
								break
							case "sourcetds" :
								int isTDS = 0
								if (WorkbookUtil.getStringCellValue(modelSheet, cols, r).toLowerCase() == "tds") {
									isTDS = 1
									onlyTds = true
								}
								valueList.append(isTDS+",")
								break
							case "sourcetdsversion" :
								valueList.append((WorkbookUtil.getStringCellValue(modelSheet, cols, r) ? WorkbookUtil.getStringCellValue(modelSheet, cols, r) : null)+",")
								break
							case "height" :
								valueList.append((WorkbookUtil.getStringCellValue(modelSheet, cols, r) ? WorkbookUtil.getStringCellValue(modelSheet, cols, r) : null)+",")
								break
							case "weight" :
								valueList.append((WorkbookUtil.getStringCellValue(modelSheet, cols, r) ? WorkbookUtil.getStringCellValue(modelSheet, cols, r) : null)+",")
								break
							case "depth" :
								valueList.append((WorkbookUtil.getStringCellValue(modelSheet, cols, r) ? WorkbookUtil.getStringCellValue(modelSheet, cols, r) : null)+",")
								break
							case "width" :
								valueList.append((WorkbookUtil.getStringCellValue(modelSheet, cols, r) ? WorkbookUtil.getStringCellValue(modelSheet, cols, r) : null)+",")
								break
							case "model_scope" :
								def modelScope = WorkbookUtil.getStringCellValue(modelSheet, cols, r)
								projectId = Project.findByProjectCode(modelScope)?.id
								//valueList.append((WorkbookUtil.getStringCellValue(modelSheet, cols, r) ? WorkbookUtil.getStringCellValue(modelSheet, cols, r) : null)+",")
								break
							case "end_of_life_date" :
								def endOfLifeDate = WorkbookUtil.getStringCellValue(modelSheet, cols, r)
								if (endOfLifeDate) {
									valueList.append("'"+(WorkbookUtil.getStringCellValue(modelSheet, cols, r) ? WorkbookUtil.getStringCellValue(modelSheet, cols, r) : null)+"',")
								}else{
									valueList.append((WorkbookUtil.getStringCellValue(modelSheet, cols, r) ? WorkbookUtil.getStringCellValue(modelSheet, cols, r) : null)+",")
								}
								break
						/*case "end_of_life_status" :
							valueList.append((WorkbookUtil.getStringCellValue(modelSheet, cols, r) ? WorkbookUtil.getStringCellValue(modelSheet, cols, r) : null)+",")
							break;*/
							case "created_by" :
								def createdByName = WorkbookUtil.getStringCellValue(modelSheet, cols, r)
								createdPersonId = Person.findByFirstName(createdByName)?.id
								break
							case "updated_by" :
								def updatedByName = WorkbookUtil.getStringCellValue(modelSheet, cols, r)
								updatedPersonId = Person.findByFirstName(updatedByName)?.id
								//valueList.append((WorkbookUtil.getStringCellValue(modelSheet, cols, r) ? WorkbookUtil.getStringCellValue(modelSheet, cols, r) : null)+",")
								break
							case "validated_by" :
								def validatedByName = WorkbookUtil.getStringCellValue(modelSheet, cols, r)
								validatedPersonId = Person.findByFirstName(validatedByName)?.id
								//valueList.append((WorkbookUtil.getStringCellValue(modelSheet, cols, r) ? WorkbookUtil.getStringCellValue(modelSheet, cols, r) : null)+",")
								break
							case "room_object" :
								int roomObject = 0
								if (WorkbookUtil.getStringCellValue(modelSheet, cols, r).toLowerCase() != "False") {
									roomObject = 1
								}
								valueList.append(roomObject+",")
								break
							case "date_created":
							case "last_modified":
								break
							default :
								valueList.append("'"+WorkbookUtil.getStringCellValue(modelSheet, cols, r, "").replace("'","\\'")+"',")
								break
						}

					}
					try{
						if (manuId) {
							if (importCheckbox) {
								if (onlyTds == true) {
									jdbcTemplate.update("insert into model_sync(model_temp_id, name,aka, description,manufacturer_temp_id,manufacturer_name,asset_type,blade_count,blade_label_count,blade_rows,sourcetds,power_nameplate,power_design,power_use,room_object,sourcetdsversion,use_image,usize,height,weight,depth,width,layout_style,product_line,model_family,end_of_life_date,end_of_life_status,sourceurl,model_status,batch_id,manufacturer_id,created_by_id,updated_by_id,validated_by_id, model_scope_id) values " + valueList + "$modelSyncBatch.id, $manuId, $createdPersonId, $updatedPersonId, $validatedPersonId, $projectId)")
									results.modelAdded = r
								} else {
									// TODO : getting ArrayIndexOutOfbound exception, need to fix
									//modelSkipped += r + 1
								}
							} else {
								jdbcTemplate.update("insert into model_sync(model_temp_id, name,aka, description,manufacturer_temp_id,manufacturer_name,asset_type,blade_count,blade_label_count,blade_rows,sourcetds,power_nameplate,power_design,power_use,room_object,sourcetdsversion,use_image,usize,height,weight,depth,width,layout_style,product_line,model_family,end_of_life_date,end_of_life_status,sourceurl,model_status,batch_id,manufacturer_id,created_by_id,updated_by_id,validated_by_id, model_scope_id) values " + valueList + "$modelSyncBatch.id, $manuId, $createdPersonId, $updatedPersonId, $validatedPersonId, $projectId) ")
								results.modelAdded = r
							}
						} else {
							//modelSkipped += r + 1
						}
					} catch (Exception e) {
						log.error "Can't insert into model_sync: $e.message"
						e.printStackTrace()
						results.modelSkipped += 1
					}
				}
			}
			/*
			 * Import Model Information
			 */

			//check for column
			def connectorSheet = workbook.getSheet("connector")
			def connectorCol = WorkbookUtil.getColumnsCount(connectorSheet)
			for (int c = 0; c < connectorCol; c++) {
				def cellContent = WorkbookUtil.getStringCellValue(connectorSheet, c, 0)
				connectorSheetColumnNames.put(cellContent, c)
			}
			missingHeader = checkHeader(sheetNameMap.get("connector"), connectorSheetColumnNames)
			// Statement to check Headers if header are not found it will return Error message
			if (missingHeader != "") {
				results.error = " Column Headers : $missingHeader not found, Please check it."
				return results
			} else {
				def sheetrows = connectorSheet.getLastRowNum()
				for (int r = 1; r < sheetrows ; r++) {
					def valueList = new StringBuilder("(")
					def modelId
					for(int cols = 0; cols < connectorCol; cols++) {
						switch(WorkbookUtil.getStringCellValue(connectorSheet, cols, 0)) {
							case "model_name" :
								def modelName = WorkbookUtil.getStringCellValue(connectorSheet, cols, r)
								modelId = ModelSync.findByModelNameAndBatch(modelName,modelSyncBatch)?.id
								valueList.append("'"+WorkbookUtil.getStringCellValue(connectorSheet, cols, r, "").replace("'","\\'")+"',")
								break
							case "connector_posx" :
								valueList.append((WorkbookUtil.getStringCellValue(connectorSheet, cols, r) ? WorkbookUtil.getStringCellValue(connectorSheet, cols, r) : null)+",")
								break
							case "connector_posy" :
								valueList.append((WorkbookUtil.getStringCellValue(connectorSheet, cols, r) ? WorkbookUtil.getStringCellValue(connectorSheet, cols, r) : null)+",")
								break
							default :
								valueList.append("'"+WorkbookUtil.getStringCellValue(connectorSheet, cols, r, "").replace("'","\\'")+"',")
								break
						}

					}
					try{
						if (modelId) {
							jdbcTemplate.update("insert into model_connector_sync(connector_temp_id,connector,connector_posx,connector_posy,label,label_position,model_temp_id,model_name,connector_option,status,type,batch_id,model_id) values " + valueList + "$modelSyncBatch.id, $modelId)")
							results.connectorAdded = r
						} else {
							results.connectorSkipped += r + 1
						}
					} catch (Exception e) {
						log.error "Can't insert into model_connector_sync: $e.message"
						results.connectorSkipped += r + 1
					}
				}
			}
		return results
	}
	}

}
