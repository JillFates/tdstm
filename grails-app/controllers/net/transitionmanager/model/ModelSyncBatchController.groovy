package net.transitionmanager.model

import com.tdsops.common.security.spring.HasPermission
import grails.plugin.springsecurity.annotation.Secured
import net.transitionmanager.controller.ControllerMethods
import net.transitionmanager.manufacturer.Manufacturer
import net.transitionmanager.manufacturer.ManufacturerSync
import net.transitionmanager.security.Permission
import net.transitionmanager.asset.ManufacturerService
import net.transitionmanager.asset.ModelService

@Secured('isAuthenticated()') // TODO BB need more fine-grained rules here
class ModelSyncBatchController implements ControllerMethods {

	static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']
	static defaultAction = 'list'

	ManufacturerService manufacturerService
	ModelService modelService

	@HasPermission(Permission.ModelImport)
	def list() {
      	//params.max = Math.min(params.max ? params.int('max') : 20, 100)
		[modelSyncBatchInstanceList: ModelSyncBatch.list(sort: 'id', order: 'desc'),
		modelSyncBatchInstanceTotal: ModelSyncBatch.count()]
	}

	@HasPermission(Permission.ModelImport)
	def process() {
		def modelBatch = params.batchId
		def modelSyncBatch = ModelSyncBatch.get(modelBatch)
		try{
			if (modelSyncBatch) {
				List<ManufacturerSync> manufacturersSync = ManufacturerSync.findAllByBatch(modelSyncBatch)
				List<ModelSync> modelsSync = ModelSync.findAllByBatch(modelSyncBatch)
				List<ModelConnectorSync> connectorsSync = ModelConnectorSync.findAllByBatch(modelSyncBatch)
				// Merge manufacturers
				def manuAdded = 0
				def manuUpdated = 0
				manufacturersSync.each { manufacturerSync->
					def manufacturerInstance = Manufacturer.findByIdAndName(manufacturerSync.manufacturerTempId,manufacturerSync.name)
					if (!manufacturerInstance) {
						manufacturerInstance = Manufacturer.findByName(manufacturerSync.name)
						if (!manufacturerInstance) {
							manufacturerInstance = new Manufacturer(name: manufacturerSync.name,
							                                        description: manufacturerSync.description)
							manufacturerInstance.save()

							if (manufacturerSync.aka) {
								def akas = manufacturerSync.aka?.split(",")
								akas.each {
									def manuExist = Manufacturer.findByName(it.trim())
									if (!manuExist) {
										//manufacturerInstance.findOrCreateAliasByName(it.trim(), true)
										manufacturerService.findOrCreateAliasByName(manufacturerInstance, it.trim(), true)
									}
								}
							}

							manuAdded ++

						} else {
							//manufacturerInstance.aka = manufacturerSync.aka
							manufacturerInstance.description = manufacturerSync.description
							manufacturerInstance.userLogin = securityService.loadCurrentUserLogin()
							manufacturerInstance.save()

							if (manufacturerSync.aka) {
								def akas = manufacturerSync.aka?.split(",")
								akas.each {
									def manuExist = Manufacturer.findByName(it.trim())
									if (!manuExist) {
										//manufacturerInstance.findOrCreateAliasByName(it.trim(), true)
										manufacturerService.findOrCreateAliasByName(manufacturerInstance, it.trim(), true)
									}
								}
							}

							manuUpdated ++

						}
					} else {
						//manufacturerInstance.aka = manufacturerSync.aka
						manufacturerInstance.description = manufacturerSync.description
						manufacturerInstance.save()

						if (manufacturerSync.aka) {
							def akas = manufacturerSync.aka?.split(",")
							akas.each {
								def manuExist = Manufacturer.findByName(it.trim())
								if (!manuExist) {
									//manufacturerInstance.findOrCreateAliasByName(it.trim(), true)
									manufacturerService.findOrCreateAliasByName(manufacturerInstance, it.trim(), true)
								}

							}
						}

						manuUpdated ++

					}
				}
				// Merge manufacturers
				def modelAdded = 0
				def modelUpdated = 0
				modelsSync.each { modelSync->
					def manufacturer = Manufacturer.findByName(modelSync.manufacturerName)
					def modelInstance = Model.findWhere(id:modelSync.modelTempId,modelName : modelSync.modelName,assetType : modelSync.assetType)
					if (!modelInstance) {
						modelInstance = Model.findByModelNameAndAssetType(modelSync.modelName,modelSync.assetType)
						if (!modelInstance) {
							modelInstance = new Model(
							   modelName : modelSync.modelName,
							   description : modelSync.description,
							   assetType : modelSync.assetType,
							   powerNameplate : modelSync.powerNameplate,
							   powerDesign : modelSync.powerDesign,
							   powerUse : modelSync.powerUse,
							   usize : modelSync.usize,
							   bladeRows : modelSync.bladeRows,
							   bladeCount : modelSync.bladeCount,
							   bladeLabelCount : modelSync.bladeLabelCount,
							   sourceTDSVersion : 1,
							   manufacturer : manufacturer,
							   height : modelSync?.height,
							   weight : modelSync?.weight,
							   depth : modelSync?.depth,
							   width : modelSync?.width,
							   layoutStyle : modelSync.layoutStyle,
							   productLine : modelSync.productLine,
							   modelFamily : modelSync.modelFamily,
							   endOfLifeDate : modelSync.endOfLifeDate,
							   endOfLifeStatus : modelSync.endOfLifeStatus,
							   createdBy : modelSync.createdBy,
							   sourceURL : modelSync.sourceURL,
							   modelStatus : modelSync.modelStatus,
							   modelScope : modelSync.modelScope
							)
							modelInstance.save()

							if (modelSync.aka) {
								def akas = modelSync.aka?.split(",")
								akas.each { String aka ->
									aka = aka.trim()
									def akaExist = Model.findByModelName(aka)
									if (!akaExist) {
										//modelInstance.findOrCreateAliasByName(it.trim(), true)
										modelService.findOrCreateAliasByName(modelInstance, aka, true)
									}
								}
							}

							modelAdded ++

						} else {
							if (modelInstance.sourceTDSVersion < modelSync.sourceTDSVersion) {

								modelInstance.description = modelSync.description
								modelInstance.powerNameplate = modelSync.powerNameplate
								modelInstance.powerDesign = modelSync.powerDesign
								modelInstance.powerUse = modelSync.powerUse
								modelInstance.usize = modelSync.usize
								modelInstance.bladeRows = modelSync.bladeRows
								modelInstance.bladeCount = modelSync.bladeCount
								modelInstance.bladeLabelCount = modelSync.bladeLabelCount
								modelInstance.sourceTDSVersion = modelSync.sourceTDSVersion
								modelInstance.manufacturer = manufacturer
								modelInstance.height = modelSync.height
								modelInstance.weight = modelSync.weight
								modelInstance.depth = modelSync.depth
								modelInstance.width = modelSync.width
								modelInstance.layoutStyle = modelSync.layoutStyle
								modelInstance.productLine = modelSync.productLine
								modelInstance.modelFamily = modelSync.modelFamily
								modelInstance.endOfLifeDate = modelSync.endOfLifeDate
								modelInstance.endOfLifeStatus = modelSync.endOfLifeStatus
								modelInstance.createdBy = modelSync.createdBy
								modelInstance.updatedBy = modelSync.updatedBy
								modelInstance.sourceURL = modelSync.sourceURL
								modelInstance.modelStatus = modelSync.modelStatus
								modelInstance.modelScope = modelSync.modelScope

								modelInstance.save()

								if (modelSync.aka) {
									def akas = modelSync.aka?.split(",")
									akas.each { String aka ->
										aka = aka.trim()
										def akaExist = Model.findByModelName(aka)
										if (!akaExist) {
											//modelInstance.findOrCreateAliasByName(it.trim(), true)
											modelService.findOrCreateAliasByName(modelInstance, aka, true)
										}
									}
								}

								modelUpdated ++

							}
						}
					} else {
						if (modelInstance.sourceTDSVersion < modelSync.sourceTDSVersion) {

							modelInstance.description = modelSync.description
							modelInstance.powerNameplate = modelSync.powerNameplate
							modelInstance.powerDesign = modelSync.powerDesign
							modelInstance.powerUse = modelSync.powerUse
							modelInstance.usize = modelSync.usize
							modelInstance.bladeRows = modelSync.bladeRows
							modelInstance.bladeCount = modelSync.bladeCount
							modelInstance.bladeLabelCount = modelSync.bladeLabelCount
							modelInstance.sourceTDSVersion = modelSync.sourceTDSVersion
							modelInstance.manufacturer = manufacturer
							modelInstance.height = modelSync.height
							modelInstance.weight = modelSync.weight
							modelInstance.depth = modelSync.depth
							modelInstance.width = modelSync.width
							modelInstance.layoutStyle = modelSync.layoutStyle
							modelInstance.productLine = modelSync.productLine
							modelInstance.modelFamily = modelSync.modelFamily
							modelInstance.endOfLifeDate = modelSync.endOfLifeDate
							modelInstance.endOfLifeStatus = modelSync.endOfLifeStatus
							modelInstance.createdBy = modelSync.createdBy
							modelInstance.updatedBy = modelSync.updatedBy
							modelInstance.validatedBy = modelSync.validatedBy
							modelInstance.sourceURL = modelSync.sourceURL
							modelInstance.modelStatus = modelSync.modelStatus
							modelInstance.modelScope = modelSync.modelScope

							modelInstance.save()

							if (modelSync.aka) {
								def akas = modelSync.aka?.split(",")
								akas.each { String aka ->
									aka = aka.trim()
									// TODO - THIS should be checking if model exists by model name AND manufacturer. The model name is NOT unique
									def modelExist = Model.findByModelName(aka)
									if (!modelExist) {
										//modelInstance.findOrCreateAliasByName(it.trim(), true)
										modelService.findOrCreateAliasByName(modelInstance, aka, true)
									}
								}
							}

							modelUpdated ++

						}
					}
				}
				// Merge Model Connectors
				def connectorsAdded = 0
				def connectorsUpdated = 0
				connectorsSync.each { connectorSync->
					def manufacturer = Manufacturer.findByName(connectorSync.model.manufacturerName)
					def model = Model.findWhere(manufacturer:manufacturer,modelName : connectorSync.model.modelName,assetType : connectorSync.model.assetType)
					if (model) {
						def connectorInstance = ModelConnector.findByConnectorAndModel(connectorSync.connector ,model)
						if (!connectorInstance) {
							connectorInstance = new ModelConnector(
								connector : connectorSync.connector,
								label : connectorSync.label,
								type : connectorSync.type,
								labelPosition : connectorSync.labelPosition,
								connectorPosX : connectorSync.connectorPosX,
								connectorPosY : connectorSync.connectorPosY,
								status : connectorSync.status,
								option : connectorSync.option,
								model : model
							)

							connectorInstance.save()
							connectorsAdded ++

						} else {
							if (connectorInstance.model.sourceTDSVersion <= connectorSync.model.sourceTDSVersion) {
								connectorInstance.label = connectorSync.label
								connectorInstance.type = connectorSync.type
								connectorInstance.labelPosition = connectorSync.labelPosition
								connectorInstance.connectorPosX = connectorSync.connectorPosX
								connectorInstance.connectorPosY = connectorSync.connectorPosY
								connectorInstance.status = connectorSync.status
								connectorInstance.option = connectorSync.option

								connectorInstance.save()
								connectorsUpdated ++

							}
						}
					}
				}
				flash.message = """ Process Results:<ul>
						<li>Manufacturers in Batch: ${manufacturersSync.size()}</li>
						<li>Maufacturers Inserted: $manuAdded</li>
						<li>Manufacturers Updated: $manuUpdated</li>
					</ul>
					<ul>
						<li>Models in Batch: ${modelsSync.size()}</li>
						<li>Models Inserted: $modelAdded</li>
						<li>Models Updated: $modelUpdated</li>
					</ul>
					<ul>
						<li> Connectors in Batch: ${connectorsSync.size()}</li>
						<li>Connectors Inserted: $connectorsAdded</li>
						<li>Connectors Updated: $connectorsUpdated</li>
					</ul>"""
			}
			modelSyncBatch.statusCode = "COMPLETED"
			modelSyncBatch.save()

		} catch (Exception e) {
			flash.message = "Import Batch process failed"
			log.error "Can't import: " + e.message
			e.printStackTrace()
		}
		redirect(action: "list", params: params)
	}
}
