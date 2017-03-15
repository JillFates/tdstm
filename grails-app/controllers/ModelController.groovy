import com.tds.asset.AssetCableMap
import com.tds.asset.AssetEntity
import com.tdsops.common.security.spring.HasPermission
import com.tdsops.tm.enums.domain.AssetCableStatus
import com.tdsops.tm.enums.domain.UserPreferenceEnum as PREF
import com.tdssrc.grails.ExportUtil
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.TimeUtil
import com.tdssrc.grails.WebUtil
import com.tdssrc.grails.WorkbookUtil
import grails.converters.JSON
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.domain.DataTransferBatch
import net.transitionmanager.domain.Manufacturer
import net.transitionmanager.domain.ManufacturerAlias
import net.transitionmanager.domain.ManufacturerSync
import net.transitionmanager.domain.Model
import net.transitionmanager.domain.ModelAlias
import net.transitionmanager.domain.ModelConnector
import net.transitionmanager.domain.ModelSync
import net.transitionmanager.domain.ModelSyncBatch
import net.transitionmanager.domain.Person
import net.transitionmanager.domain.Project
import net.transitionmanager.domain.UserLogin
import net.transitionmanager.service.AssetEntityAttributeLoaderService
import net.transitionmanager.service.AssetEntityService
import net.transitionmanager.service.ModelService
import net.transitionmanager.service.SecurityService
import net.transitionmanager.service.UserPreferenceService
import org.apache.commons.lang.math.NumberUtils
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.multipart.MultipartHttpServletRequest
import org.springframework.web.multipart.commons.CommonsMultipartFile

import grails.plugin.springsecurity.annotation.Secured
@Secured('isAuthenticated()') // TODO BB need more fine-grained rules here
class ModelController implements ControllerMethods {

	static allowedMethods = [save: 'POST', update: 'POST', delete: 'POST']
	static defaultAction = 'list'

	AssetEntityAttributeLoaderService assetEntityAttributeLoaderService
	AssetEntityService assetEntityService
	JdbcTemplate jdbcTemplate
	ModelService modelService
	SecurityService securityService
	UserPreferenceService userPreferenceService

	@HasPermission('ModelList')
	def list() {
		Map modelPref = assetEntityService.getExistingPref('Model_Columns')
		Map attributes = Model.getModelFieldsAndlabels()
		Map columnLabelpref = [:]
		modelPref.each { key, value -> columnLabelpref[key] = attributes[value] }
		[modelPref: modelPref, attributesList: attributes.keySet().sort(), columnLabelpref: columnLabelpref]
	}

	/**
	* This method is used by JQgrid to load modelList
	*/
	@HasPermission('ModelList')
	def listJson() {
		String sortOrder = params.sord in ['asc','desc'] ? params.sord : 'asc'
		int maxRows = params.int('rows')
		int currentPage = params.int('page', 1)
		int rowOffset = currentPage == 1 ? 0 : (currentPage - 1) * maxRows
		def modelInstanceList

		// This map contains all the possible fileds that the user could be sorting or filtering on
		Map<String, Object> filterParams = [
			modelName: params.modelName, manufacturer: params.manufacturer, description: params.description,
			assetType: params.assetType, powerUse: params.powerUse, noOfConnectors: params.modelConnectors,
			assetsCount: params.assetsCount, sourceTDSVersion: params.sourceTDSVersion, sourceTDS: params.sourceTDS,
			modelStatus: params.modelStatus]
		Map<String, Object> attributes = Model.modelFieldsAndlabels
		def modelPref= assetEntityService.getExistingPref('Model_Columns')
		def modelPrefVal = modelPref.collect{it.value}
		attributes.keySet().each { attribute ->
			if (attribute in modelPrefVal && attribute!='modelConnectors') {
				filterParams[attribute] = params[attribute]
			}
		}
		// Cut the list of fields to filter by down to only the fields the user has entered text into
		def usedFilters = filterParams.findAll { key, val -> val != null }

		// Get the actual list from the service
		modelInstanceList = modelService.listOfFilteredModels(filterParams, params.sidx, sortOrder)

		// TODO : this looks like a good utility function to refactor
		// Limit the returned results to the user's page size and number
		def totalRows = modelInstanceList.size()
		def numberOfPages = Math.ceil(totalRows / maxRows)

		// Get the subset of all records based on the pagination
		modelInstanceList = (totalRows > 0) ? modelInstanceList = modelInstanceList[rowOffset..Math.min(rowOffset+maxRows,totalRows-1)] : []

		// Reformat the list to allow jqgrid to use it
		def results = modelInstanceList?.collect {[
			id: it.modelId,
			cell: [it.modelName, it.manufacturer, displayModelValues(modelPref["1"], it),
			       displayModelValues(modelPref["2"], it), displayModelValues(modelPref["3"], it),
			       displayModelValues(modelPref["4"], it), it.assetsCount, it.sourceTDSVersion,
			       it.sourceTDS, it.modelStatus]
		]}

		renderAsJson(rows: results, page: currentPage, records: totalRows, total: numberOfPages)
	}

	@HasPermission('ModelCreate')
	def displayModelValues(value, model) {
		switch (value) {
			case ~/dateCreated|lastModified|endOfLifeDate/: TimeUtil.formatDate(model[value])
			case 'modelConnectors':                         model.noOfConnectors
			default:                                        model[value]
		}
	}

	@HasPermission('ModelCreate')
	def create(String modelId) {
		List<ModelConnector> modelConnectors
		Model modelTemplate
		if (modelId) {
			modelTemplate = Model.get(modelId)
			modelConnectors = ModelConnector.findAllByModel(modelTemplate)
		}

		List<Integer> otherConnectors = []
		int existingConnectors = modelConnectors ? modelConnectors.size() + 1 : 1
		for (int i = existingConnectors; i < 51; i++) {
			otherConnectors << i
		}

		[modelInstance: new Model(), modelConnectors: modelConnectors, otherConnectors: otherConnectors,
		 modelTemplate: modelTemplate, powerType: userPreferenceService.getPreference(PREF.CURR_POWER_TYPE)]
	}

	@HasPermission('ModelEdit')
	def save() {
		try {
			def modelId = params.modelId
			def powerNameplate = params.powerNameplate ? Float.parseFloat(params.powerNameplate) : 0
			def powerDesign = params.powerDesign ? Float.parseFloat(params.powerDesign) : 0
			def powerUsed = params.powerUse ? Float.parseFloat(params.powerUse) : 0
			def powerType = params.powerType
			def endOfLifeDate = params.endOfLifeDate
			if (securityService.loggedIn) {
				Person person = securityService.userLoginPerson
				if (person) {
					int score = person.modelScore ?: 0
					if (params.modelStatus == "new" || params.modelStatus == "full") {
						person.modelScore = score + 10
					} else {
						person.modelScore = score + 20
					}
					if (!person.save(flush: true)) {
						person.errors.allErrors.each { println it }
					}
				}
			}
			
			if (endOfLifeDate) {
				params.endOfLifeDate = TimeUtil.parseDate(endOfLifeDate)
			}
			
			if (powerType == "Amps") {
				powerNameplate *= 120
				powerDesign *= 120
				powerUsed *= 120
			}
			
			Model modelTemplate = modelId ? Model.get(modelId) : null
			params.useImage = params.useImage == 'on' ? 1 : 0
			params.sourceTDS = params.sourceTDS == 'on' ? 1 : 0
			params.roomObject = params.roomObject == 'on'
			params.powerUse = powerUsed
			
			def modelInstance = new Model(params)
			modelInstance.powerUse = powerUsed
			modelInstance.powerDesign = powerDesign
			modelInstance.powerNameplate = powerNameplate
			if (params.modelStatus == 'valid') {
				modelInstance.validatedBy = securityService.loadCurrentPerson()
			}
			def okcontents = ['image/png', 'image/x-png', 'image/jpeg', 'image/pjpeg', 'image/gif']
			def frontImage = request.getFile('frontImage')
			if (frontImage?.bytes?.size() > 0) {
				if (frontImage.getContentType() && frontImage.getContentType() != "application/octet-stream") {
					if (! okcontents.contains(frontImage.getContentType())) {
						flash.message = "Front Image must be one of: $okcontents"
						render(view: "create", model: [modelInstance: modelInstance])
						return
					}
				}
			} else if (modelTemplate) {
				modelInstance.frontImage = modelTemplate.frontImage
			} else {
				modelInstance.frontImage = null
			}
			def rearImage = request.getFile('rearImage')
			if (rearImage?.bytes?.size() > 0) {
				if (rearImage.getContentType() && rearImage.getContentType() != "application/octet-stream") {
					if (! okcontents.contains(rearImage.getContentType())) {
						flash.message = "Rear Image must be one of: $okcontents"
						render(view: "create", model: [modelInstance: modelInstance])
						return
					}
				}
			} else if (modelTemplate) {
				modelInstance.rearImage = modelTemplate.rearImage
			} else {
				modelInstance.rearImage = null
			}
			if (modelInstance.save(flush: true)) {
				def connectorCount = Integer.parseInt(params.connectorCount)
				if (connectorCount > 0) {
					for(int i=1; i<=connectorCount; i++) {
						def modelConnector = new ModelConnector(model : modelInstance,
							connector : params['connector' + i],
							label : params['label' + i],
							type :params['type' + i],
							labelPosition : params['labelPosition' + i],
							connectorPosX : Integer.parseInt(params['connectorPosX' + i]),
							connectorPosY : Integer.parseInt(params['connectorPosY' + i]),
							status:params['status' + i])
						
						if (!modelConnector.hasErrors())
							modelConnector.save(flush: true)
					}
				} else {
					def powerConnector = new ModelConnector(model : modelInstance,
						connector : 1,
						label : "Pwr1",
						type : "Power",
						labelPosition : "Right",
						connectorPosX : 0,
						connectorPosY : 0,
						status: "missing"
						)
					
					if (!powerConnector.save(flush: true)) {
						def etext = "Unable to create Power Connectors for $modelInstance" +
						GormUtil.allErrorsString(powerConnector)
						println etext
					}
				}
				
				modelInstance.sourceTDSVersion = 1
				modelInstance.save(flush: true)
				def akaNames = params.list('aka')
				akaNames.each { aka ->
					aka = aka.trim()
					if (aka) {
						def isValid = modelService.isValidAlias(aka, modelInstance, false)
						modelInstance.findOrCreateAliasByName(aka, true)
					}
				}
				flash.message = "$modelInstance.modelName created"
				redirect(action:"list" , id: modelInstance.id)
			} else {
				flash.message = modelInstance.errors.allErrors.each {  log.error it }
				def modelConnectors = modelTemplate ? ModelConnector.findAllByModel(modelTemplate) : null
				def otherConnectors = []
				def existingConnectors = modelConnectors ? modelConnectors.size()+1 : 1
				for(int i = existingConnectors ; i<51; i++) {
					otherConnectors << i
				}
				render(view: "list", model: [modelInstance: modelInstance, modelConnectors:modelConnectors,
											   otherConnectors:otherConnectors, modelTemplate:modelTemplate ] )
			}
		}catch(RuntimeException rte) {
			flash.message = rte.getMessage()
			redirect(controller:'model', action: 'list')
		}
		catch (RuntimeException rte) {
			flash.message = rte.message
			redirect(controller: 'project', action: 'list')
		}
	}
	
	@HasPermission('ModelView')
	def show() {
		def modelId = params.id
		if (modelId && modelId.isNumber()) {
			def model = Model.get(params.id)
			if (!model) {
				flash.message = "Model not found with Id $params.id"
				redirect(action: "list")
			} else {
				def modelConnectors = ModelConnector.findAllByModel(model,[sort:"id"])
				def modelAkas = WebUtil.listAsMultiValueString(ModelAlias.findAllByModel(model, [sort:'name']).name)
				def paramsMap = [modelInstance: model, modelConnectors: modelConnectors, modelAkas: modelAkas,
				                 modelHasPermission: securityService.hasPermission("ModelValidate"),
				                 redirectTo: params.redirectTo, modelRef: AssetEntity.findByModel(model)]

				def view = params.redirectTo == "assetAudit" ? "_modelAuditView" : (params.redirectTo == "modelDialog" ? "_show" : "show")
				render(view: view, model: paramsMap)
			}
		} else {
			if (params.redirectTo == "assetAudit") {
				render "<b>Model not found with Id $params.id</b>"
			} else {
				flash.message = "Model not found with Id $params.id"
				redirect(action: "list")
			}
		}
	}

	@HasPermission('ModelEdit')
	def edit() {
		def modelId = params.id
		if (modelId && modelId.isNumber()) {
			def model = Model.get(params.id)
			if (!model) {
				flash.message = "Model not found with Id $params.id"
				redirect(action: "list")
			} else {
				def modelConnectors = ModelConnector.findAllByModel(model,[sort:"id"])
				def nextConnector = 0
				try{
					nextConnector = modelConnectors.size() > 0 ? Integer.parseInt(modelConnectors[modelConnectors.size()-1]?.connector) : 0
				} catch(NumberFormatException ex) {
					nextConnector = modelConnectors.size()+1
				}
				def otherConnectors = []
				for(int i = nextConnector+1 ; i<51; i++) {
					otherConnectors << i
				}
				def modelAliases = ModelAlias.findAllByModel(model)
				def paramsMap = [modelInstance: model, modelConnectors : modelConnectors, otherConnectors : otherConnectors,
				                 nextConnector:nextConnector, modelAliases:modelAliases, redirectTo:params.redirectTo]

				def view = params.redirectTo== "modelDialog" ? "_edit" : "edit"
				render(view: view, model: paramsMap)

			}
		} else {
			flash.message = "Model id $params.id is not a valid Id "
			redirect(action: "list")
		}
	}

	@HasPermission('ModelEdit')
	def update() {
		try{
			def modelInstance = Model.get(params.id)
			def modelStatus = modelInstance?.modelStatus
			def endOfLifeDate = params.endOfLifeDate
			Person person
			if (securityService.loggedIn) {
			person = securityService.userLoginPerson
				if (person) {
					def score = person?.modelScore ?: 0
					if (params.modelStatus == "full" && modelStatus != params.modelStatus) {
						person.modelScore = score+20
					}else if (params.modelStatus == "valid" && modelStatus != params.modelStatus) {
						if (modelInstance?.validatedBy?.id == person?.id && modelInstance.updatedBy?.id != person?.id) {
							person.modelScore = score+20
						} else {
							person.modelScore = score+50
						}
					}
					if (!person.save(flush:true)) {
						person.errors.allErrors.each {println it }
					}
				}
			}
			if (endOfLifeDate) {
				params.endOfLifeDate = TimeUtil.parseDate(endOfLifeDate)
			}

			if (modelInstance) {
				def powerNameplate = params.powerNameplate ? Float.parseFloat(params.powerNameplate) : 0
				def powerDesign = params.powerDesign ? Float.parseFloat(params.powerDesign) : 0
				def powerUsed = params.powerUse ? Float.parseFloat(params.powerUse) : 0
				def powerType = params.powerType
				if (powerType == "Amps") {
					powerNameplate = powerNameplate * 120
					powerDesign = powerDesign * 120
					powerUsed = powerUsed * 120
				}
				params.useImage = params.useImage == 'on' ? 1 : 0
				params.sourceTDS = params.sourceTDS == 'on' ? 1 : 0
				params.powerNameplate = powerNameplate
				params.powerDesign = powerDesign
				params.powerUse = powerUsed
				def okcontents = ['image/png', 'image/x-png', 'image/jpeg', 'image/pjpeg', 'image/gif']
				def frontImage
				if (request?.getFile('frontImage')) {
					frontImage = request?.getFile('frontImage')
					if (frontImage?.getContentType() && frontImage?.getContentType() != "application/octet-stream") {
						if (! okcontents.contains(frontImage.getContentType())) {
							flash.message = "Front Image must be one of: $okcontents"
							render(view: "create", model: [modelInstance: modelInstance])
							return
						}
						frontImage = frontImage.bytes
					}
					else {
						frontImage = modelInstance.frontImage
					}
				}
				else {
					frontImage = modelInstance.frontImage
				}
				def rearImage
				if (request?.getFile('rearImage')) {
					rearImage = request?.getFile('rearImage')
					if (rearImage?.getContentType() && rearImage?.getContentType() != "application/octet-stream") {
						if (! okcontents.contains(rearImage.getContentType())) {
							flash.message = "Rear Image must be one of: $okcontents"
							render(view: "create", model: [modelInstance: modelInstance])
							return
						}
						rearImage = rearImage.bytes
					}
					else {
						rearImage = modelInstance.rearImage
					}
				}
				else {
					rearImage = modelInstance.rearImage
				}
				modelInstance.height = params.modelHeight != "" ? NumberUtils.toDouble(params.modelHeight,0).round():0
				modelInstance.weight = params.modelWeight != "" ? NumberUtils.toDouble(params.modelWeight,0).round():0
				modelInstance.depth = params.modelDepth != "" ? NumberUtils.toDouble(params.modelDepth,0).round():0
				modelInstance.width = params.modelWidth != "" ? NumberUtils.toDouble(params.modelWidth,0).round():0
				if (params.modelStatus == 'valid' && modelStatus == 'full') {
					modelInstance.validatedBy = person
					modelInstance.updatedBy = modelInstance.updatedBy
				}else{
					modelInstance.updatedBy = person
				}
				modelInstance.properties = params
				modelInstance.rearImage = rearImage
				modelInstance.frontImage = frontImage

				if (!modelInstance.hasErrors() && modelInstance.save(flush:true)) {

					def deletedAka = params.deletedAka
					def akaToSave = params.list('aka')
					if (deletedAka) {
						ModelAlias.executeUpdate("delete ModelAlias where id in (:ids)", [ids:deletedAka.split(",").collect{NumberUtils.toDouble(it,0).round()}])
					}
					def modelAliasList = ModelAlias.findAllByModel(modelInstance)
					log.info "akaToSave = ${akaToSave}"
					log.info "modelAliasList = ${modelAliasList}"
					modelAliasList.each { modelAlias ->
						log.info "old alias: ${modelAlias.name}"
						modelAlias.name = params["aka_"+modelAlias.id]
						if (!modelAlias.save()) {
							modelAlias.errors.allErrors.each {println it}
						}
					}
					akaToSave.each { aka ->
						log.info "new alias: ${aka}"
						modelInstance.findOrCreateAliasByName(aka, true)
					}

					def connectorCount = 0
					if (params.connectorCount) {
						connectorCount = NumberUtils.toDouble(params.connectorCount,0).round()
					}
					if (connectorCount > 0) {
						for(int i=1; i<=connectorCount; i++) {
							def connector = params['connector' + i]
							ModelConnector modelConnector = ModelConnector.findByModelAndConnector(modelInstance, connector ?: i)
							if (!connector && modelConnector) {
								modelConnector.delete(flush:true)
							}
							else {
								if (modelConnector) {
									modelConnector.connector = params['connector' + i]
									modelConnector.label = params['label' + i]
									modelConnector.type = params['type' + i]
									modelConnector.labelPosition = params['labelPosition' + i]
									modelConnector.connectorPosX = NumberUtils.toDouble(params['connectorPosX' + i],0).round()
									modelConnector.connectorPosY = NumberUtils.toDouble(params['connectorPosY' + i],0).round()
									modelConnector.status = params['status' + i]
								} else if (connector) {
									modelConnector = new ModelConnector(
										model: modelInstance,
										connector: params['connector' + i],
										label: params['label' + i],
										type: params['type' + i],
										labelPosition: params['labelPosition' + i],
										connectorPosX: NumberUtils.toDouble(params['connectorPosX' + i],0).round(),
										connectorPosY: NumberUtils.toDouble(params['connectorPosY' + i],0).round(),
										status: params['status' + i])
								}
								if (modelConnector && !modelConnector.hasErrors()) {
									modelConnector.save(flush: true)
								}
							}
						}
					}
					else {
						if (!powerConnector.save(flush: true)) {
							def etext = "Unable to create Power Connectors for $modelInstance: ${GormUtil.allErrorsString(powerConnector)}"
							println etext
						}
					}

					def assetEntitysByModel = AssetEntity.findAllByModel(modelInstance)
					def assetConnectors = ModelConnector.findAllByModel(modelInstance)
					assetEntitysByModel.each { assetEntity ->
						assetConnectors.each {connector ->

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
								def etext = "Unable to create assetCableMap for assetEntity $assetEntity" +
								GormUtil.allErrorsString(assetCableMap)
								println etext
								log.error(etext)
							}
						}
						def assetCableMaps = AssetCableMap.findAllByAssetFrom(assetEntity)
						assetCableMaps.each {assetCableMap ->
							if (!assetConnectors.id?.contains(assetCableMap.assetFromPort?.id)) {
								AssetCableMap.executeUpdate('''
									update AssetCableMap
									set cableStatus=?, assetTo=null, assetToPort=null
									where assetToPort=?
								''', [AssetCableStatus.UNKNOWN, assetCableMap.assetFromPort])
								AssetCableMap.executeUpdate("delete AssetCableMap where assetFromPort = ${assetCableMap.assetFromPort?.id}")
							}
						}
					}

					// TODO : JPM 9/2014 : assetType Legacy code that shoule be removed at some point when we normalize out the assetType from AssetEntity
					def updateAssetsQuery = "update asset_entity set asset_type = '$modelInstance.assetType' where model_id='$modelInstance.id'"
					jdbcTemplate.update(updateAssetsQuery)

					if (modelInstance.sourceTDSVersion) {
						modelInstance.sourceTDSVersion ++
					} else {
						modelInstance.sourceTDSVersion = 1
					}
					modelInstance.save(flush: true)

					flash.message = "$modelInstance.modelName Updated"
					if (params.redirectTo == "assetAudit") {
						render(template: "modelAuditView", model: [modelInstance:modelInstance])
					}
					forward(action: "show", params:[id: modelInstance.id, redirectTo:params.redirectTo])

				} else {
					modelInstance.errors.allErrors.each {log.error it}
					def modelConnectors = ModelConnector.findAllByModel(modelInstance)
					def otherConnectors = []
					for(int i = modelConnectors.size()+1 ; i<51; i++) {
						otherConnectors << i
					}
					render(view: "edit", model: [modelInstance: modelInstance, modelConnectors : modelConnectors, otherConnectors : otherConnectors])
				}
			} else {
				flash.message = "Model not found with Id $params.id"
				redirect(action: "list")
			}
		} catch(RuntimeException rte) {
			flash.message = rte.message
			redirect(controller:'model', action: 'list')
		}
	}

	@HasPermission('ModelDelete')
	def delete() {
		def model = Model.get(params.id)
		def modelRef = AssetEntity.findByModel(model)
		if (!modelRef) {
			UserLogin user = securityService.userLogin
			Person person = user?.person
			if (model) {
				try {
					model.delete(flush: true)
					if (user) {
						int bonusScore = person?.modelScoreBonus ? person?.modelScoreBonus:0
						person.modelScoreBonus = bonusScore+1
						int score = person.modelScore ?: 0
						person.modelScore = score+bonusScore
					}
					if (!person.save(flush:true)) {
						person.errors.allErrors.each {
							println it
							}
					}

					flash.message = "$model deleted"
					redirect(action: "list")
				} catch (DataIntegrityViolationException e) {
					flash.message = "$model not deleted"
					redirect(action: "show", id: params.id)
				}
			}
			else {
				flash.message = "Model not found with Id $params.id"
				redirect(action: "list")
			}
		} else{
			flash.message = "Model $model.modelName can not be deleted, it is referenced ."
			redirect(action: "list")
		}
	}

	/*
	 * Send FrontImage as inputStream
	 */
	@HasPermission('ModelView')
	def retrieveFrontImage() {
		if (params.id) {
			def model = Model.get(params.id)
			def image = model?.frontImage
			response.contentType = 'image/jpg'
			response.outputStream << image
		} else {
			return ""
		}
	}

	/*
	 * Send RearImage as inputStream
	 */
	@HasPermission('ModelView')
	def retrieveRearImage() {
		if (params.id) {
			def model = Model.get(params.id)
			def image = model?.rearImage
			response.contentType = 'image/jpg'
			response.outputStream << image
		} else {
			return ""
		}
	}

	/*
	 * Send List of model as JSON object
	 */
	@HasPermission('ModelView')
	def retrieveModelsListAsJSON() {
		def manufacturer = params.manufacturer
		def assetType = params.assetType
		def models
		if (manufacturer) {
			def manufacturerInstance = Manufacturer.get(manufacturer)
			models = manufacturerInstance ? Model.findAllByManufacturer(
				manufacturerInstance,
				[sort: 'modelName', order: 'asc'])?.findAll{it.assetType == assetType } : null
		}
		def modelsList = []
		if (models.size() > 0) {
			models.each {
				modelsList << [id:it.id, modelName:it.modelName]
			}
		}
		render modelsList as JSON
	}

	/*
	 * check to see that if they were any Asset records exist for the selected model before deleting it
	 */
	@HasPermission('ModelView')
	def checkModelDependency() {
		def modelId = params.modelId
		def modelInstance = Model.get(modelId)
		def returnValue = false
		if (modelInstance) {
			if (AssetEntity.findByModel(modelInstance))
				returnValue = true
		}
		render returnValue
	}

	/*
	 * Return AssetCables to alert the user while deleting the connectors
	*/
	@HasPermission('ModelView')
	def retrieveAssetCablesForConnector() {
		def modelId = params.modelId
		def modelInstance = Model.get(modelId)
		def assetCableMap = []
		if (modelInstance) {
			def connector = params.connector
			def modelConnector = ModelConnector.findByConnectorAndModel(connector, modelInstance)
			assetCableMap = AssetCableMap.findAll("from AssetCableMap where cableStatus in ('$AssetCableStatus.EMPTY','$AssetCableStatus.CABLED','$AssetCableStatus.ASSIGNED') and (assetFromPort = ? or assetToPort = ?)",[modelConnector,modelConnector])
		}
		render assetCableMap as JSON
	}

	/*
	 * TEMP method to redirect to action : show
	 */
	@HasPermission('ModelView')
	def cancel() {
		redirect(action: "show", id: params.id)
	}

	/*
	* When the user clicks on an item do the following actions:
	*	1. Add to the AKA field list in the target record
	*	2. Revise Asset, and any other records that may point to this model
	*	3. Delete model record
	*	4. Return to model list view with the flash message "Merge completed."
	*/
	@HasPermission('ModelMerge')
	def merge() {
		// Get the Model instances for params ids
		def toModel = Model.get(params.id)
		def fromModel = Model.get(params.fromId)

		def assetUpdated = modelService.mergeModel(fromModel, toModel)

		flash.message = "Merge Completed, $assetUpdated assets updated"
		redirect(action:"list")
	}

	/**
	* @param : toId id of target model
	* @param : fromId[] id of model that is being merged
	* @return : message
	*/
	@HasPermission('ModelMerge')
	def mergeModels() {

		Model toModel = Model.get(params.toId)
		List fromModelsId = params.list("fromId[]")
		List mergedModel = []
		String msg = ""
		int assetUpdated = 0
		//Saving toModel before merge
		if (params.endOfLifeDate) {
			params.endOfLifeDate = TimeUtil.formatDate(params.endOfLifeDate)
		} else {
			params.endOfLifeDate=null
		}
		toModel.properties = params
		if (!toModel.save(flush:true)) {
			toModel.errors.allErrors.each {println it }
		}
		fromModelsId.each {
			def fromModel = Model.get(it)
			assetUpdated += modelService.mergeModel(fromModel, toModel)
			mergedModel << fromModel
		}

		render msg + mergedModel.size() + " models were merged to $toModel.modelName . $assetUpdated assets were updated."
	}

	@HasPermission('ModelExport')
	def importExport() {
		if (params.message) {
			flash.message = params.message
		}

		[batchCount: jdbcTemplate.queryForObject('select count(*) from (select * from manufacturer_sync group by batch_id) a', Integer)]
	}

	/*
	* Use excel format with the manufacturer,model and connector sheets.
	* The file name should be of the format TDS-Sync-Data-2011-05-02.xls with the current date.
	*/
	@HasPermission('ModelExport')
	def export() {
		//get template Excel
		try {
			File file = grailsApplication.parentContext.getResource("/templates/Sync_model_template.xls").getFile()
			String filename = ("TDS-Sync-Data-" + TimeUtil.formatDateTime(new Date(), TimeUtil.FORMAT_DATE_TIME_6) + ".xls").replace(" ", "_")
			ExportUtil.setContentType response, filename

			def book = new HSSFWorkbook(new FileInputStream(file))

			def manuSheet = book.getSheet("manufacturer")
			List<Manufacturer> manufacturers = params.exportCheckbox ?
					Model.findAll('FROM Model where sourceTDS = 1 GROUP BY manufacturer').manufacturer :
					Manufacturer.findAll()

			for (int r = 0; r < manufacturers.size(); r++) {
				WorkbookUtil.addCell(manuSheet, 0, r+1, String.valueOf(manufacturers[r].id))
				WorkbookUtil.addCell(manuSheet, 1, r+1, String.valueOf(manufacturers[r].name))
				WorkbookUtil.addCell(manuSheet, 2, r+1, String.valueOf(WebUtil.listAsMultiValueString(ManufacturerAlias.findAllByManufacturer(manufacturers[r]).name)))
				WorkbookUtil.addCell(manuSheet, 3, r+1, String.valueOf(manufacturers[r].description ? manufacturers[r].description : ""))
			}
			def modelSheet = book.getSheet("model")
			def models = params.exportCheckbox == '1' ? Model.findAllBySourceTDS(1) : Model.findAll()

			for (int r = 0; r < models.size(); r++) {
				WorkbookUtil.addCell(modelSheet, 0, r+1, String.valueOf(models[r].id))
				WorkbookUtil.addCell(modelSheet, 1, r+1, String.valueOf(models[r].modelName))
				WorkbookUtil.addCell(modelSheet, 2, r+1, String.valueOf(WebUtil.listAsMultiValueString(ModelAlias.findAllByModel(models[r]).name)))
				WorkbookUtil.addCell(modelSheet, 3, r+1, String.valueOf(models[r].description ? models[r].description : ""))
				WorkbookUtil.addCell(modelSheet, 4, r+1, String.valueOf(models[r].manufacturer.id))
				WorkbookUtil.addCell(modelSheet, 5, r+1, String.valueOf(models[r].manufacturer.name))
				WorkbookUtil.addCell(modelSheet, 6, r+1, String.valueOf(models[r].assetType))
				WorkbookUtil.addCell(modelSheet, 7, r+1, String.valueOf(models[r].bladeCount ? models[r].bladeCount : ""))
				WorkbookUtil.addCell(modelSheet, 8, r+1, String.valueOf(models[r].bladeLabelCount ? models[r].bladeLabelCount : ""))
				WorkbookUtil.addCell(modelSheet, 9, r+1, String.valueOf(models[r].bladeRows ? models[r].bladeRows : ""))
				WorkbookUtil.addCell(modelSheet, 10, r+1, String.valueOf(models[r].sourceTDS == 1 ? "TDS" : ""))
				WorkbookUtil.addCell(modelSheet, 11, r+1, String.valueOf(models[r].powerNameplate ? models[r].powerNameplate : ""))
				WorkbookUtil.addCell(modelSheet, 12, r+1, String.valueOf(models[r].powerDesign ? models[r].powerDesign : ""))
				WorkbookUtil.addCell(modelSheet, 13, r+1, String.valueOf(models[r].powerUse ? models[r].powerUse : ""))
				WorkbookUtil.addCell(modelSheet, 14, r+1, String.valueOf(models[r].roomObject==1 ? 'True' : 'False'))
				WorkbookUtil.addCell(modelSheet, 15, r+1, String.valueOf(models[r].sourceTDSVersion ? models[r].sourceTDSVersion : 1))
				WorkbookUtil.addCell(modelSheet, 16, r+1, String.valueOf(models[r].useImage == 1 ? "yes" : "no"))
				WorkbookUtil.addCell(modelSheet, 17, r+1, String.valueOf(models[r].usize ? models[r].usize : ""))
				WorkbookUtil.addCell(modelSheet, 18, r+1, String.valueOf(models[r].height ? models[r].height : ""))
				WorkbookUtil.addCell(modelSheet, 19, r+1, String.valueOf(models[r].weight ? models[r].weight : ""))
				WorkbookUtil.addCell(modelSheet, 20, r+1, String.valueOf(models[r].depth ? models[r].depth : ""))
				WorkbookUtil.addCell(modelSheet, 21, r+1, String.valueOf(models[r].width ? models[r].width : ""))
				WorkbookUtil.addCell(modelSheet, 22, r+1, String.valueOf(models[r].layoutStyle ? models[r].layoutStyle: ""))
				WorkbookUtil.addCell(modelSheet, 23, r+1, String.valueOf(models[r].productLine ? models[r].productLine :""))
				WorkbookUtil.addCell(modelSheet, 24, r+1, String.valueOf(models[r].modelFamily ? models[r].modelFamily :""))
				WorkbookUtil.addCell(modelSheet, 25, r+1, String.valueOf(models[r].endOfLifeDate ? models[r].endOfLifeDate :""))
				WorkbookUtil.addCell(modelSheet, 26, r+1, String.valueOf(models[r].endOfLifeStatus ? models[r].endOfLifeStatus :""))
				WorkbookUtil.addCell(modelSheet, 27, r+1, String.valueOf(models[r].createdBy ? models[r].createdBy :""))
				WorkbookUtil.addCell(modelSheet, 28, r+1, String.valueOf(models[r].updatedBy ? models[r].updatedBy :""))
				WorkbookUtil.addCell(modelSheet, 29, r+1, String.valueOf(models[r].validatedBy ? models[r].validatedBy : ""))
				WorkbookUtil.addCell(modelSheet, 30, r+1, String.valueOf(models[r].sourceURL ? models[r].sourceURL :""))
				WorkbookUtil.addCell(modelSheet, 31, r+1, String.valueOf(models[r].modelStatus ? models[r].modelStatus:""))
				WorkbookUtil.addCell(modelSheet, 32, r+1, String.valueOf(models[r].modelScope ? models[r].modelScope :""))
				WorkbookUtil.addCell(modelSheet, 33, r+1, TimeUtil.formatDate(models[r].dateCreated))
				WorkbookUtil.addCell(modelSheet, 34, r+1, TimeUtil.formatDate(models[r].lastModified))
			}

			def connectorSheet = book.getSheet("connector")
			def connectors = params.exportCheckbox ?
				ModelConnector.findAll("FROM ModelConnector where model.sourceTDS = 1 order by model.id") :
				ModelConnector.findAll()

			for (int r = 0; r < connectors.size(); r++) {
				WorkbookUtil.addCell(connectorSheet, 0, r+1, String.valueOf(connectors[r].id))
				WorkbookUtil.addCell(connectorSheet, 1, r+1, String.valueOf(connectors[r].connector))
				WorkbookUtil.addCell(connectorSheet, 2, r+1, String.valueOf(connectors[r].connectorPosX))
				WorkbookUtil.addCell(connectorSheet, 3, r+1, String.valueOf(connectors[r].connectorPosY))
				WorkbookUtil.addCell(connectorSheet, 4, r+1, String.valueOf(connectors[r].label ? connectors[r].label : ""))
				WorkbookUtil.addCell(connectorSheet, 5, r+1, String.valueOf(connectors[r].labelPosition))
				WorkbookUtil.addCell(connectorSheet, 6, r+1, String.valueOf(connectors[r].model.id))
				WorkbookUtil.addCell(connectorSheet, 7, r+1, String.valueOf(connectors[r].model.modelName))
				WorkbookUtil.addCell(connectorSheet, 8, r+1, String.valueOf(connectors[r].option ? connectors[r].option : ""))
				WorkbookUtil.addCell(connectorSheet, 9, r+1, String.valueOf(connectors[r].status))
				WorkbookUtil.addCell(connectorSheet, 10, r+1, String.valueOf(connectors[r].type))
			}
			book.write(response.getOutputStream())
		}
		catch (e) {
			log.error e.message, e
			flash.message = "Exception occurred while exporting data: $e"
			redirect(action: 'importExport')
		}
	}

	/*
	*1. On upload the system should put the data into temporary tables and then perform validation to make sure the data is proper and ready.
	*2. Step through each imported model:
	*2a if it's SourceTDSVersion is higher than the one in the database, update the database with the new model and connector data.
	*2b If it is lower, skip it.
	*3. Report the number of Model records updated.
	*/
	@HasPermission('ModelImport')
	def upload() {
		DataTransferBatch.withTransaction { status ->
			UserLogin userLogin = securityService.userLogin
			MultipartHttpServletRequest mpr = (MultipartHttpServletRequest)request
			CommonsMultipartFile file = (CommonsMultipartFile) mpr.getFile("file")
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
			try {
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
					flash.message = "$missingSheets sheets not found, Please check it."
					redirect(action:"importExport", params:[message:flash.message])
					return
				} else {
					def manuAdded = 0
					def manuSkipped = []
					def sheetColumnNames = [:]
					//check for column
					def manuSheet = workbook.getSheet("manufacturer")
					def manuCol = WorkbookUtil.getColumnsCount(manuSheet)
					for (int c = 0; c < manuCol; c++) {
						def cellContent = WorkbookUtil.getStringCellValue(manuSheet, c, 0)
						sheetColumnNames.put(cellContent, c)
					}
					def missingHeader = checkHeader(sheetNameMap.get("manufacturer"), sheetColumnNames)
					// Statement to check Headers if header are not found it will return Error message
					if (missingHeader != "") {
						flash.message = " Column Headers : $missingHeader not found, Please check it."
						redirect(action: "importExport", params:[message:flash.message])
						return
					} else {
						def sheetrows = manuSheet.getLastRowNum()
						for (int r = 1; r < sheetrows ; r++) {
							def valueList = new StringBuffer("(")
							for(int cols = 0; cols < manuCol; cols++) {
								valueList.append("'"+WorkbookUtil.getStringCellValue(manuSheet, cols, r, "").replace("'","\\'")+"',")
							}
							try{
								jdbcTemplate.update("insert into manufacturer_sync(manufacturer_temp_id, name,aka, description, batch_id) values " +
									valueList + modelSyncBatch.id + ')')
								manuAdded = r
							} catch (e) {
								log.error "Can't insert into manufacturer_sync: $e.message"
								manuSkipped += r + 1
							}
						}
					}

					/*
					 * Import Model Information
					 */
					def modelSheetColumnNames = [:]
					def modelAdded = 0
					def modelSkipped = []
					//check for column
					def modelSheet = workbook.getSheet("model")
					def modelCol = WorkbookUtil.getColumnsCount(modelSheet)
					//def colContain = modelCol.
					for (int c = 0; c < modelCol; c++) {
						def cellContent = WorkbookUtil.getStringCellValue(modelSheet, c, 0)
						modelSheetColumnNames.put(cellContent, c)
					}
					missingHeader = checkHeader(sheetNameMap.get("model"), modelSheetColumnNames)
					def onlyTds
					// Statement to check Headers if header are not found it will return Error message
					if (missingHeader != "") {
						flash.message = " Column Headers : $missingHeader not found, Please check it."
						redirect(action:"importExport", params:[ message:flash.message])
						return
					} else {
						def sheetrows = modelSheet.getLastRowNum()
						for (int r = 1; r < sheetrows ; r++) {
							onlyTds = false
							def valueList = new StringBuffer("(")
							def manuId
							def createdPersonId
							def updatedPersonId
							def validatedPersonId
							def projectId
							for(int cols = 0; cols < modelCol; cols++) {
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
									if (params.importCheckbox) {
										if (onlyTds == true) {
											jdbcTemplate.update("insert into model_sync(model_temp_id, name,aka, description,manufacturer_temp_id,manufacturer_name,asset_type,blade_count,blade_label_count,blade_rows,sourcetds,power_nameplate,power_design,power_use,room_object,sourcetdsversion,use_image,usize,height,weight,depth,width,layout_style,product_line,model_family,end_of_life_date,end_of_life_status,sourceurl,model_status,batch_id,manufacturer_id,created_by_id,updated_by_id,validated_by_id, model_scope_id) values " + valueList + "$modelSyncBatch.id, $manuId, $createdPersonId, $updatedPersonId, $validatedPersonId, $projectId)")
											modelAdded = r
										} else {
										// TODO : getting ArrayIndexOutOfbound exception, need to fix
											//modelSkipped += r + 1
										}
									} else {
										jdbcTemplate.update("insert into model_sync(model_temp_id, name,aka, description,manufacturer_temp_id,manufacturer_name,asset_type,blade_count,blade_label_count,blade_rows,sourcetds,power_nameplate,power_design,power_use,room_object,sourcetdsversion,use_image,usize,height,weight,depth,width,layout_style,product_line,model_family,end_of_life_date,end_of_life_status,sourceurl,model_status,batch_id,manufacturer_id,created_by_id,updated_by_id,validated_by_id, model_scope_id) values " + valueList + "$modelSyncBatch.id, $manuId, $createdPersonId, $updatedPersonId, $validatedPersonId, $projectId) ")
										modelAdded = r
									}
								} else {
									//modelSkipped += r + 1
								}
							} catch (Exception e) {
								log.error "Can't insert into model_sync: $e.message"
								e.printStackTrace()
								modelSkipped += r + 1
							}
						}
					}
					/*
					 * Import Model Information
					 */
					def connectorSheetColumnNames = [:]
					def connectorAdded = 0
					def connectorSkipped = []
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
						flash.message = " Column Headers : $missingHeader not found, Please check it."
						redirect(action:"importExport", params:[message:flash.message])
						return
					} else {
						def sheetrows = connectorSheet.getLastRowNum()
						for (int r = 1; r < sheetrows ; r++) {
							def valueList = new StringBuffer("(")
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
									connectorAdded = r
								} else {
									connectorSkipped += r + 1
								}
							} catch (Exception e) {
								log.error "Can't insert into model_connector_sync: $e.message"
								connectorSkipped += r + 1
							}
						}
					}
					if (manuSkipped.size() > 0 || modelSkipped.size() > 0 || connectorSkipped.size() > 0) {
						flash.message = " File Uploaded Successfully with Manufactures:$manuAdded,Model:$modelAdded,Connectors:$connectorAdded records. and Manufactures:$manuSkipped,Model:$modelSkipped,Connectors:$connectorSkipped Records skipped Please click the Manage Batches to review and post these changes."
					} else {
						flash.message = " File uploaded successfully with Manufactures:$manuAdded,Model:$modelAdded,Connectors:$connectorAdded records. Please click the Manage Batches to review and post these changes."
					}
					redirect(action:"importExport", params:[message:flash.message])
					return
				}
			} catch(NumberFormatException ex) {
				flash.message = ex
				status.setRollbackOnly()
				redirect(action:"importExport", params:[message:flash.message])
				return
			} catch(Exception ex) {
				ex.printStackTrace()
				status.setRollbackOnly()
				flash.message = ex
				redirect(action:"importExport", params:[message:flash.message])
				return
			}
		}
	}

	private String checkHeader(List<String> list, sheetColumnNames) {
		String missingHeader = ""
		int listSize = list.size()
		for (int coll = 0; coll < listSize; coll++) {
			if (!sheetColumnNames.containsKey(list[coll])) {
				missingHeader += ", " + list[coll]
			}
		}
		return missingHeader
	}

	@HasPermission('ModelImport')
	def manageImports() {
		[modelSyncBatch: ModelSyncBatch.list()]
	}

	/*
	 * Send Model details as JSON object
	 */
	@HasPermission('ModelView')
	def retrieveModelAsJSON() {
		def id = params.id
		def model = Model.get(params.id)
		def powerNameplate = model.powerNameplate
		def powerDesign = model.powerDesign
		def powerUsed = model.powerUse
		if (userPreferenceService.getPreference(PREF.CURR_POWER_TYPE) != 'Watts') {
			powerNameplate = powerNameplate ? powerNameplate / 120 : ''
			powerNameplate = powerNameplate ? powerNameplate.toDouble().round(1) : ''
			powerDesign = powerDesign ? powerDesign / 120 : ''
			powerDesign = powerDesign ? powerDesign.toDouble().round(1) : ''
			powerUsed = powerUsed ? powerUsed / 120 : ''
			powerUsed = powerUsed ? powerUsed.toDouble().round(1) : ''
		}
		def modelMap = [id:model.id,
						manufacturer:model.manufacturer?.name,
						modelName:model.modelName,
						description:model.description,
						assetType:model.assetType,
						powerUse:powerUsed,
						aka: WebUtil.listAsMultiValueString(ModelAlias.findAllByModelAndManufacturer(model, model.manufacturer).name),
						usize:model.usize,
						frontImage:model.frontImage ? model.frontImage : '',
						rearImage:model.rearImage ? model.rearImage : '',
						useImage:model.useImage,
						bladeRows:model.bladeRows,
						bladeCount:model.bladeCount,
						bladeLabelCount:model.bladeLabelCount,
						bladeHeight:model.bladeHeight,
						bladeHeight:model.bladeHeight,
						sourceTDSVersion:model.sourceTDSVersion,
						powerNameplate: powerNameplate,
						powerDesign : powerDesign]
		render modelMap as JSON
	}
	
	@HasPermission('ModelValidate')
	def validateModel() {
		def modelInstance = Model.get(params.id)
		if (securityService.loggedIn) {
			modelInstance.validatedBy = securityService.loadCurrentPerson()
			modelInstance.modelStatus = "valid"
			if (!modelInstance.save(flush:true)) {
				modelInstance.errors.allErrors.each { println it }
			}
		}
		flash.message = "$modelInstance.modelName Validated"
		render (view: "show",model:[id: modelInstance.id,modelInstance:modelInstance])
	}
	
	/**
	* Validate whether requested alias already exist in DB or not
	* @param: alias, the new alias to be validated
	* @param: id, id of model
	* @param: manufacturerId, id of the manufacturer to validate the alias using (not needed if the model's manufacturer hasn't changed)
	* @param: parentName, name of the model to validate the alias with (not needed if the model's name hasn't changed)
	* @return: "valid" if the alias is valid, "invalid" otherwise
	*/
	@HasPermission('ModelEdit')
	def validateAliasForForm() {
		def alias = params.alias
		def modelId = params.id
		def manufacturerId = params.manufacturerId
		def newModelName = params.parentName
		
		// get the model and manufacturer if specified and call the service method for alias validation
		def model = modelId ? Model.read(modelId) : null
		def manufacturer = manufacturerId ? Manufacturer.read(manufacturerId) : null
		def isValid = modelService.isValidAlias(alias, model, true, manufacturer, newModelName)
		if (isValid)
			render 'valid'
		else
			render 'invalid'
	}
	
	/**
	 * Updates a model for audit view , not using update method as there have a lot of code in update action might degrade performance.
	 * @param id : id of model for update
	 */
	@HasPermission('ModelEdit')
	def updateModel() {
		def modelId = params.id
		if (modelId && modelId.isNumber()) {
			def model = Model.get(params.id)
			model.properties = params
			if (!model.save(flush:true)) {
				model.errors.allErrors.each {
					log.error it
				}
			}
			render(template: "modelAuditView", model: [modelInstance:model])
		} else {
			render "<b> No Model found for id: $params.id</b>"
		}
	}

	/**
	* render a list of suggestions for model's initial.
	* @param : value is initial for which user wants suggestions .
	* @return : sugesstion template.
	*/
	@HasPermission('ModelEdit')
	def autoCompleteModel() {
		def initials = params.value
		def manufacturer = params.manufacturer
		def manu = Manufacturer.findByName(manufacturer)
		def models = []
		if (manu) {
			models = initials ? Model.findAllByModelNameIlikeAndManufacturer(initials+"%", manu) : []
		}
		[models:models]
	}

	/**
	* Fetch models's type for model name
	* @param value : name of model name
	* @return model's assetType
	*
	*/
	@HasPermission('ModelView')
	def retrieveModelType() {
		def modelName = params.value
		def model = Model.findByModelName(modelName)
		def modelType = model?.assetType ?: 'Server'
		render modelType
	}

	/**
	* Methods checks whether model exist in model or model alias table
	* @param modelName : name of model
	* @param manufacturerName : name of manufacturer
	* @return : modelAuditEdit template
	*/
	@HasPermission('ModelView')
	def retrieveModelDetailsByName() {
		def modelName = params.modelName
		def manufacturerName = params.manufacturerName
		def model = assetEntityAttributeLoaderService.findOrCreateModel(manufacturerName, modelName, '', false)
		if (model) {
			render(template: "modelAuditEdit", model: [modelInstance:model])
		} else {
			render "<b> No Model found of name $params.modelName</b>"
		}

	}

	/**
	*@param : ids[] list of ids to compare
	*@return
	*/
	@HasPermission('ModelView')
	def compareOrMerge() {
		def ids = params.list("ids[]")
		def models = []
		ids.each {
			def id = Long.parseLong(it)
			def model = Model.get(id)
			if (model) {
				models << model
			}
		}

		// Sorting Model in order of status (valid, full, new)
		def sortedModel = []
		def validModel = models.findAll{it.modelStatus == 'valid'}
		def fullModel = models.findAll{it.modelStatus == 'full'}
		def newmodel = models.findAll{!['full','valid'].contains(it.modelStatus)}

		sortedModel = validModel + fullModel + newmodel

		// Defined a HashMap as 'columnList' where key is displaying label and value is property of label .
		def columnList = [ 'Model Name': 'modelName', 'Manufacturer':'manufacturer', 'AKA': 'aliases' , 'Asset Type':'assetType','Usize':'usize',
							'Dimensions(inches)':'', 'Weight(pounds)':'weight', 'Layout Style':'layoutStyle', 'Product Line':'productLine',
							'Model Family':'modelFamily', 'End Of Life Date':'endOfLifeDate','End Of Life Status':'endOfLifeStatus',
							'Power(Max/Design/Avg)':'powerUse','Notes':'description', 'Front Image':'frontImage', 'Rear Image':'rearImage', 'Room Object': 'roomObject',
							'Use Image':'useImage','Blade Rows':'bladeRows', 'Blade Count':'bladeCount','Blade Label Count':'bladeLabelCount',
							'Blade Height':'bladeHeight', 'Created By':'createdBy', 'Updated By':'updatedBy', 'Validated By':'validatedBy',
							'Source TDS':'sourceTDS','Source URL':'sourceURL', 'Model Status':'modelStatus', 'Merge To':'']


	// Checking whether models have any Model of Type 'Blade Chassis' or 'Blade' .
	def hasBladeChassis = sortedModel.find { it.assetType == 'Blade Chassis' }
	def hasBlade = sortedModel.find { it.assetType == 'Blade' }

	// If models to compare are not of type 'Blade Chassis' or 'Blade' removing from Map
	if (!hasBladeChassis) {
		['Blade Rows', 'Blade Count', 'Blade Label Count'].each { columnList.remove(it) }
	}
	if (!hasBlade)
		columnList.remove('Blade Height')

		render(template:"compareOrMerge", model:[models:sortedModel, columnList:columnList, hasBladeChassis:hasBladeChassis, hasBlade:hasBlade])
	}

	/**
	* Bulk delete models.
	* @param modelLists
	* @render resp message.
	*/
	@HasPermission('ModelDelete')
	def deleteBulkModels() {
		def resp
		def deletedModels = []
		def skippedModels = []
		def modelList = params.list("modelLists[]")
		try{
			Model.getAll(modelList*.toLong()).findAll().each { model ->
				if (!AssetEntity.countByModel(model)) {
					deletedModels << model
					model.delete()
				}else {
					skippedModels << model
				}
			}
			def delModelNames = WebUtil.listAsMultiValueString(deletedModels)
			def skipModelNames = WebUtil.listAsMultiValueString(skippedModels)
			resp = (delModelNames ? "Models $delModelNames are deleted.</br> " : "No Models Deleted </br>") +
					(skipModelNames ? " Models $skipModelNames skipped due to Asset Reference" : "")
		}
		catch (e) {
			e.printStackTrace()
			resp = "Error while deleting Models"
		}
		render resp
	}
}
