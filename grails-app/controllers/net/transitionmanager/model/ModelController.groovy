package net.transitionmanager.model

import com.tdsops.common.security.spring.HasPermission
import com.tdsops.tm.enums.domain.AssetCableStatus
import com.tdsops.tm.enums.domain.UserPreferenceEnum as PREF
import com.tdssrc.grails.ExportUtil
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.NumberUtil
import com.tdssrc.grails.TimeUtil
import com.tdssrc.grails.WebUtil
import com.tdssrc.grails.WorkbookUtil
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.asset.AssetCableMap
import net.transitionmanager.asset.AssetEntity
import net.transitionmanager.asset.AssetEntityAttributeLoaderService
import net.transitionmanager.asset.AssetEntityService
import net.transitionmanager.asset.AssetOptions
import net.transitionmanager.asset.ModelService
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.controller.PaginationMethods
import net.transitionmanager.exception.InvalidParamException
import net.transitionmanager.manufacturer.Manufacturer
import net.transitionmanager.manufacturer.ManufacturerAlias
import net.transitionmanager.person.Person
import net.transitionmanager.person.UserPreferenceService
import net.transitionmanager.security.Permission
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.multipart.MultipartFile

import java.text.DateFormat

@Secured('isAuthenticated()') // TODO BB need more fine-grained rules here
class ModelController implements ControllerMethods, PaginationMethods {
	static allowedMethods = [save: 'POST', update: 'POST', delete: 'POST']
	static defaultAction = 'list'
	static final OK_CONTENTS = ['image/png', 'image/x-png', 'image/jpeg', 'image/pjpeg', 'image/gif']
	private final static DELETE_OK_MESSAGE = "Model deleted successfully."
	private final static DELETE_ERROR_MESSAGE = "Model not found."

	AssetEntityAttributeLoaderService assetEntityAttributeLoaderService
	AssetEntityService                assetEntityService
	JdbcTemplate                      jdbcTemplate
	ModelService                      modelService
	UserPreferenceService             userPreferenceService

	@HasPermission(Permission.ModelList)
	def list() {
		Map modelPref = assetEntityService.getExistingPref(PREF.Model_Columns)
		Map attributes = Model.getModelFieldsAndLabels()
		Map columnLabelpref = [:]
		modelPref.each { key, value -> columnLabelpref[key] = attributes[value] }
		[modelPref: modelPref, attributesList: attributes.keySet().sort(), columnLabelpref: columnLabelpref]
	}

	/**
	* This method is used by JQgrid to load modelList
	*/
	@HasPermission(Permission.ModelList)
	def listJson() {

		// This map contains all the possible fields that the user could be sorting or filtering on
		Map<String, String> filterParams = [
			modelName: params.modelName, manufacturer: params.manufacturer, description: params.description,
			assetType: params.assetType, powerUse: params.powerUse, modelConnectors: params.modelConnectors,
			assetsCount: params.assetsCount, sourceTDSVersion: params.sourceTDSVersion, sourceTDS: params.sourceTDS,
			modelStatus: params.modelStatus]

		// Get user configurable column names
		Map modelPref= assetEntityService.getExistingPref(PREF.Model_Columns)
		List modelPrefVal = modelPref.collect{it.value}

		// Iterate over the Model domain fields and add the user configured columns to the filter params if a valid property
		Model.modelFieldsAndLabels.keySet().each { attribute ->
			if (attribute in modelPrefVal && attribute != 'modelConnectors') {
				filterParams[attribute] = params[attribute]
			}
		}

		List modelInstanceList = modelService.listOfFilteredModels(filterParams, paginationAsObject() )

        def results = modelInstanceList?.collect {
            Map<String, Object> data = [id: it.modelId, modelName: it.modelName, manufacturer: it.manufacturer,
                    description: displayModelValues(modelPref["1"], it),
                    assetType: displayModelValues(modelPref["2"], it),
                    lastModified: displayModelValues(modelPref["3"], it),
                    connectors: displayModelValues(modelPref["4"], it),
                    assetsCount:  it.assetsCount, sourceTDSVersion:  it.sourceTDSVersion,
                    sourceTDS:  it.sourceTDS, modelStatus: it.modelStatus]
            data
        }

            renderSuccessJson([rows: results])
	}

	@HasPermission(Permission.ModelCreate)
	def displayModelValues(value, model) {
		DateFormat formatter = TimeUtil.createFormatter(TimeUtil.FORMAT_DATE_TIME)

		switch (value) {
			case ~/dateCreated|lastModified|endOfLifeDate/:
				TimeUtil.formatDateTime(model[value], formatter)
				break
			case 'modelConnectors':
				model.modelConnectors
				break
			default:
				model[value]
				break
		}
	}

	@HasPermission(Permission.ModelCreate)
	def create() {

		renderSuccessJson([
				modelInstance  : new Model(),
				modelConnectors: null,
				otherConnectors: (1..50).collect {it},
				modelTemplate  : null,
				powerType      : userPreferenceService.getPreference(PREF.CURR_POWER_TYPE),
				assetTypes     : AssetOptions.findAllByType(AssetOptions.AssetOptionsType.ASSET_TYPE, [sort: 'value']).value,
				usizeList      : GormUtil.getConstrainedProperties(Model).usize.inList
		])
	}

	private boolean isValidImage(MultipartFile file) {
		if (file && file.getContentType() != MediaType.APPLICATION_OCTET_STREAM_VALUE) {
			return OK_CONTENTS.contains(file.getContentType())
		}
		return false
	}

	@HasPermission(Permission.ModelView)
	def show() {
		def modelId = params.id
		if (modelId && modelId.isNumber()) {
			def model = Model.get(params.id)
			if (!model) {
				flash.message = "Model not found with Id $params.id"
				redirect(action: "list")
			} else {
				def userList = Person.getAll()
				def modelConnectors = ModelConnector.findAllByModel(model,[sort:"id"])
				def modelAkas = WebUtil.listAsMultiValueString(ModelAlias.findAllByModel(model, [sort:'name']).name)
                def modelAkas2 = ModelAlias.findAllByModel(model, [sort:'name'])
                def powerType = userPreferenceService.getPreference(PREF.CURR_POWER_TYPE) ?: 'Watts'

				def usizeList = com.tdssrc.grails.GormUtil.getConstrainedProperties(model.class).usize.inList
				def assetTypes = AssetOptions.findAllByType(AssetOptions.AssetOptionsType.ASSET_TYPE, [sort: 'value']).value


				def paramsMap = [modelInstance: model, modelConnectors: modelConnectors, modelAkas: modelAkas2,
				                 modelHasPermission: securityService.hasPermission(Permission.ModelValidate),
				                 redirectTo: params.redirectTo, modelRef: AssetEntity.findByModel(model),
								 usizeList: usizeList, assetTypes: assetTypes, userList: userList,
								 modelCreatedBy: model.getCreatedByName(), modelUpdatedBy: model.getUpdatedByName(),
								 modelValidatedBy: model.getValidatedByName(), powerType: powerType]

				// def view = params.redirectTo == "assetAudit" ? "_modelAuditView" : (params.redirectTo == "modelDialog" ? "_show" : "show")

				// render(view: view, model: paramsMap)
				renderSuccessJson(paramsMap)
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

	@HasPermission(Permission.ModelEdit)
	def edit() {
		def modelId = params.id

		if (modelId && modelId.isNumber()) {
			def model = Model.get(params.id)

			if (!model) {
				flash.message = "Model not found with Id $params.id"
				redirect(action: "list")
			} else {
				def modelConnectors = ModelConnector.findAllByModel(model, [sort: "id"])
				def nextConnector = 0

				try {
					nextConnector = modelConnectors.size() > 0 ? Integer.parseInt(modelConnectors[modelConnectors.size() - 1]?.connector) : 0
				} catch (NumberFormatException ex) {
					nextConnector = modelConnectors.size() + 1
				}

				def otherConnectors = []

				for (int i = nextConnector + 1; i < 51; i++) {
					otherConnectors << i
				}

				def modelAliases = ModelAlias.findAllByModel(model)
				def paramsMap = [
					angularModalDialog : params.angularModalDialog,
					modelInstance  : model,
					modelConnectors: modelConnectors,
					otherConnectors: otherConnectors,
					nextConnector  : nextConnector,
					modelAliases   : modelAliases, redirectTo: params.redirectTo,
					assetTypes     : AssetOptions.findAllByType(AssetOptions.AssetOptionsType.ASSET_TYPE, [sort: 'value']).value
				]

				def view = params.redirectTo == "modelDialog" ? "_edit" : "edit"
				render(view: view, model: paramsMap)

			}
		} else {
			flash.message = "Model id $params.id is not a valid Id "
			redirect(action: "list")
		}
	}

	/*
	 * Send FrontImage as inputStream
	 */
	@HasPermission(Permission.ModelView)
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
	@HasPermission(Permission.ModelView)
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
	@HasPermission(Permission.ModelView)
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
	@HasPermission(Permission.ModelView)
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
	@HasPermission(Permission.ModelView)
	def retrieveAssetCablesForConnector() {
		def modelId = params.modelId
		def modelInstance = Model.get(modelId)
		def assetCableMap = []
		if (modelInstance) {
			def connector = params.connector
			def modelConnector = ModelConnector.findByConnectorAndModel(connector, modelInstance)
			assetCableMap = AssetCableMap.findAll("from AssetCableMap where cableStatus in ('Empty','Cabled','Assigned') and (assetFromPort = ?0 or assetToPort = ?1)",[modelConnector,modelConnector])
		}
		def numberOfAssets = assetCableMap.size()
		renderSuccessJson(numberOfAssets)
	}

	/*
	 * TEMP method to redirect to action : show
	 */
	@HasPermission(Permission.ModelView)
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
	@HasPermission(Permission.ModelMerge)
	def merge() {
		// Get the Model instances for params ids
		def toModel = Model.get(params.id)
		def fromModel = Model.get(params.fromId)

		def assetUpdated = modelService.merge(fromModel, toModel)

		flash.message = "Merge Completed, $assetUpdated assets updated"
		redirect(action:"list")
	}

	/**
	 * Merges a list of Models into a target Model.
	 *
	 * @param : toId  id of target Model
	 * @param : fromId[]  ids of Models that will be merged into the target Model
	 * @return : message
	 */
	@HasPermission(Permission.ModelMerge)
	def mergeModels() {
		if (!params.toId) {
			throw new InvalidParamException("ModelController.mergeModels() - id cannot be null.")
		}
		Long toId = NumberUtil.toLong(params.toId)
		List fromModelsId = params.list("fromId[]")
		//Saving toModel before merge
		if (params.endOfLifeDate) {
			params.endOfLifeDate = TimeUtil.formatDate(params.endOfLifeDate)
		} else {
			params.endOfLifeDate=null
		}
		Map results = modelService.mergeModels(fromModelsId, toId, params)
		if (results.mergedModels) {
			render results.mergedModels.size() + " models were merged to $results.toModel.modelName. $results.assetsUpdated assets were updated. "
		} else {
			render "No models were merged. "
		}
		if (results.toModel.hasErrors()) {
			render "There were errors saving $results.toModel.modelName : ${results.toModel.errors.allErrors.each { println it}}"
		}
	}

	@HasPermission(Permission.ModelExport)
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
	@HasPermission(Permission.ModelExport)
	def export() {
		//get template Excel
		try {
			File file = grailsApplication.parentContext.getResource("/templates/Sync_model_template.xls").getFile()
			String filename = ("TDS-Sync-Data-" + TimeUtil.formatDateTime(new Date(), TimeUtil.FORMAT_DATE_ISO8601) + ".xls").replace(" ", "_")
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
				WorkbookUtil.addCell(modelSheet, 18, r+1, String.valueOf(models[r].cpuType ? models[r].cpuType : ""))
				WorkbookUtil.addCell(modelSheet, 19, r+1, String.valueOf(models[r].cpuCount ? models[r].cpuCount : ""))
				WorkbookUtil.addCell(modelSheet, 20, r+1, String.valueOf(models[r].memorySize ? models[r].memorySize : ""))
				WorkbookUtil.addCell(modelSheet, 21, r+1, String.valueOf(models[r].storageSize ? models[r].storageSize : ""))
				WorkbookUtil.addCell(modelSheet, 22, r+1, String.valueOf(models[r].height ? models[r].height : ""))
				WorkbookUtil.addCell(modelSheet, 23, r+1, String.valueOf(models[r].weight ? models[r].weight : ""))
				WorkbookUtil.addCell(modelSheet, 24, r+1, String.valueOf(models[r].depth ? models[r].depth : ""))
				WorkbookUtil.addCell(modelSheet, 25, r+1, String.valueOf(models[r].width ? models[r].width : ""))
				WorkbookUtil.addCell(modelSheet, 26, r+1, String.valueOf(models[r].layoutStyle ? models[r].layoutStyle: ""))
				WorkbookUtil.addCell(modelSheet, 27, r+1, String.valueOf(models[r].productLine ? models[r].productLine :""))
				WorkbookUtil.addCell(modelSheet, 28, r+1, String.valueOf(models[r].modelFamily ? models[r].modelFamily :""))
				WorkbookUtil.addCell(modelSheet, 29, r+1, String.valueOf(models[r].endOfLifeDate ? models[r].endOfLifeDate :""))
				WorkbookUtil.addCell(modelSheet, 30, r+1, String.valueOf(models[r].endOfLifeStatus ? models[r].endOfLifeStatus :""))
				WorkbookUtil.addCell(modelSheet, 31, r+1, String.valueOf(models[r].createdBy ? models[r].createdBy :""))
				WorkbookUtil.addCell(modelSheet, 32, r+1, String.valueOf(models[r].updatedBy ? models[r].updatedBy :""))
				WorkbookUtil.addCell(modelSheet, 33, r+1, String.valueOf(models[r].validatedBy ? models[r].validatedBy : ""))
				WorkbookUtil.addCell(modelSheet, 34, r+1, String.valueOf(models[r].sourceURL ? models[r].sourceURL :""))
				WorkbookUtil.addCell(modelSheet, 35, r+1, String.valueOf(models[r].modelStatus ? models[r].modelStatus:""))
				WorkbookUtil.addCell(modelSheet, 36, r+1, String.valueOf(models[r].modelScope ? models[r].modelScope :""))
				WorkbookUtil.addCell(modelSheet, 37, r+1, TimeUtil.formatDate(models[r].dateCreated))
				WorkbookUtil.addCell(modelSheet, 38, r+1, TimeUtil.formatDate(models[r].lastModified))
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

	@HasPermission(Permission.ModelImport)
	def manageImports() {
		[modelSyncBatch: ModelSyncBatch.list()]
	}

	/*
	 * Send Model details as JSON object
	 */
	@HasPermission(Permission.ModelView)
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
						akaCollection: model.getAliases(),
						connectorsCollection: ModelConnector.findAllByModel(model, [sort: "id"]),
						manufacturer:model.manufacturer?.name,
						manufacturerId: model.manufacturer?.id,
						modelName:model.modelName,
						modelHeight: model.height,
						modelWidth: model.width,
						modelWeight: model.weight,
						layoutStyle: model.layoutStyle,
						productLine: model.productLine,
						modelFamily: model.modelFamily,
						modelDepth: model.depth,
						endOfLifeDate: model.endOfLifeDate,
						endOfLifeStatus: model.endOfLifeStatus,
						sourceURL: model.sourceURL,
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
						sourceTDSVersion:model.sourceTDSVersion,
						powerNameplate: powerNameplate,
						powerDesign : powerDesign,
						cpuType : model.cpuType,
						cpuCount : model.cpuCount,
						memorySize : model.memorySize,
						storageSize : model.storageSize,
						createdBy: model.createdBy ? model.createdBy.firstName + ' ' + model.createdBy.lastName : '',
						updatedBy: model.updatedBy ? model.updatedBy.firstName + ' ' + model.updatedBy.lastName : '',
						validatedBy: model.validatedBy ? model.validatedBy.firstName + ' ' + model.validatedBy.lastName : '',
						roomObject: model.roomObject,
						sourceTDS: model.sourceTDS,
						modelStatus: model.modelStatus,
						powerType: userPreferenceService.getPreference(PREF.CURR_POWER_TYPE)

		]
		render modelMap as JSON
	}

	/**
	* Validate whether requested alias already exist in DB or not
	* @param: alias, the new alias to be validated
	* @param: id, id of model
	* @param: manufacturerId, id of the manufacturer to validate the alias using (not needed if the model's manufacturer hasn't changed)
	* @param: parentName, name of the model to validate the alias with (not needed if the model's name hasn't changed)
	* @return: "valid" if the alias is valid, "invalid" otherwise
	*/
	@HasPermission(Permission.ModelEdit)
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
	* render a list of suggestions for model's initial.
	* @param : value is initial for which user wants suggestions .
	* @return : sugesstion template.
	*/
	@HasPermission(Permission.ModelEdit)
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
	@HasPermission(Permission.ModelView)
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
	@HasPermission(Permission.ModelView)
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
	@HasPermission(Permission.ModelView)
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
	@HasPermission(Permission.ModelDelete)
	def deleteBulkModels() {
		def resp
		def deletedModels = []
		def skippedModels = []
		def modelList = params.list("modelLists[]")
		try {
			Model.getAll(modelList*.toLong()).findAll().each { model ->
				if (!AssetEntity.countByModel(model)) {
					deletedModels << model
					//model.delete()
					modelService.delete(model)
				}else {
					skippedModels << model
				}
			}
			def delModelNames = WebUtil.listAsMultiValueString(deletedModels)
			def skipModelNames = WebUtil.listAsMultiValueString(skippedModels)
			resp = (delModelNames ? "Models $delModelNames are deleted.</br> " : "No Models Deleted </br>") +
					(skipModelNames ? " Models $skipModelNames skipped due to Asset Reference" : "")
		} catch (e) {
			log.error(e.message, e)
			resp = "Error while deleting Models"
		}
		render resp
	}
}
