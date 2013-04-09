import grails.converters.JSON

import java.io.*
import java.text.DateFormat
import java.text.SimpleDateFormat

import jxl.*
import jxl.read.biff.*
import jxl.write.*

import org.codehaus.groovy.grails.commons.ApplicationHolder
import org.jmesa.facade.TableFacade
import org.jmesa.facade.TableFacadeImpl
import org.jmesa.limit.Limit
import org.apache.shiro.SecurityUtils
import org.springframework.web.multipart.*
import org.springframework.web.multipart.commons.*

import com.tds.asset.AssetCableMap
import com.tds.asset.AssetEntity
import com.tdssrc.grails.GormUtil
import com.tdssrc.grails.WebUtil

class ModelController {
	
	//Initialize services
    def jdbcTemplate
	def assetEntityAttributeLoaderService 
    def sessionFactory
	def securityService
	def userPreferenceService
	
	static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
		return 
    }
    
	/**
	 * This method is used by JQgrid to load modelList
	 */
	def listJson = {
		def sortIndex = params.sidx ?: 'modelName'
		def sortOrder  = params.sord ?: 'asc'
		def maxRows = Integer.valueOf(params.rows)
		def currentPage = Integer.valueOf(params.page) ?: 1
		def rowOffset = currentPage == 1 ? 0 : (currentPage - 1) * maxRows
		def project = securityService.getUserCurrentProject()
		
		def manuList = params.manufacturer ? Manufacturer.findAllByNameIlike("%${params.manufacturer}%"): []
		def powers=params.powerUse ? Model.findAll("from Model where powerUse like '%${params.powerUse}%'").powerUse :[]
		def sorceTds=params.sourceTDS ? Model.findAll("from Model where sourceTDS like '%${params.sourceTDS}%'").sourceTDS :[]
		
        def models = Model.createCriteria().list(max: maxRows, offset: rowOffset) {
				if (params.modelName)
					ilike('modelName', "%${params.modelName}%")
				if (params.manufacturer)
					'in' ('manufacturer', manuList)
				if (params.description)
					ilike('description', "%${params.description}%")
				if (params.assetType)
					ilike('assetType', "%${params.assetType}%")
				if (powers)
					'in'('powerUse', powers)
				if (sorceTds)
					'in'('sourceTDS',sorceTds)
				if (params.modelStatus)
					ilike('modelStatus', "%${params.modelStatus}%")
	
				order(sortIndex, sortOrder).ignoreCase()
			}

		def totalRows = models.totalCount
		def numberOfPages = Math.ceil(totalRows / maxRows)

		def results = models?.collect { [ cell: [it.modelName, it.manufacturer?.name, it.description, it.assetType,
					it.powerUse, it.noOfConnectors, it.assetsCount, it.sourceTDSVersion, it.sourceTDS, it.modelStatus], id: it.id,
			]}

		def jsonData = [rows: results, page: currentPage, records: totalRows, total: numberOfPages]

		render jsonData as JSON
    }
	
    def create = {
    	def modelId = params.modelId
        def modelInstance = new Model()
    	def modelConnectors
		def modelTemplate
	    if(modelId){
	    	modelTemplate = Model.get( modelId )
			modelConnectors = ModelConnector.findAllByModel( modelTemplate )
	    }
    	def otherConnectors = []
    	def existingConnectors = modelConnectors ? modelConnectors.size()+1 : 1
		for(int i = existingConnectors ; i<51; i++ ){
			otherConnectors << i
		}
		def powerType = session.getAttribute("CURR_POWER_TYPE")?.CURR_POWER_TYPE
        return [modelInstance: modelInstance, modelConnectors : modelConnectors, 
				otherConnectors:otherConnectors, modelTemplate:modelTemplate, powerType : powerType ]
    }

    def save = {
    	def modelId = params.modelId
		def powerNameplate = params.powerNameplate ? Float.parseFloat(params.powerNameplate) : 0
		def powerDesign = params.powerDesign ? Float.parseFloat(params.powerDesign) : 0
		def powerUsed = params.powerUse ? Float.parseFloat(params.powerUse) : 0
		def powerType = params.powerType
		def endOfLifeDate = params.endOfLifeDate
		def formatter = new SimpleDateFormat("MM/dd/yyyy");
		def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ
		def principal = SecurityUtils.subject?.principal
		def user
		
		if( principal ){
			user  = UserLogin.findByUsername( principal )
			def person = user.person
			def score = person?.modelScore? person?.modelScore:0
			if(user && person){
				     if(params?.modelStatus == "new"||params?.modelStatus=="full" ){
						    person.modelScore = score+10
					 }else{
					        person.modelScore = score+20
					 }
					if(!person.save(flush:true)){
						person.errors.allErrors.each{ println it }
				    }				
				}
		}
		if(endOfLifeDate){
			params.endOfLifeDate =  GormUtil.convertInToGMT(formatter.parse(endOfLifeDate), tzId)
		}
		if( powerType == "Amps"){
			powerNameplate =  powerNameplate * 110
			powerDesign = powerDesign * 110
			powerUsed = powerUsed * 110
        }
	    def modelTemplate 
		if(modelId)
			modelTemplate = Model.get(modelId)
    	params.useImage = params.useImage == 'on' ? 1 : 0
    	params.sourceTDS = params.sourceTDS == 'on' ? 1 : 0
    	params.powerUse = powerUsed
        def  modelInstance = new Model(params)
		modelInstance.powerUse = powerUsed
		modelInstance.powerDesign = powerDesign
		modelInstance.powerNameplate = powerNameplate
		if(params?.modelStatus=='valid'){
			modelInstance.validatedBy = user?.person
		}
		def okcontents = ['image/png', 'image/x-png', 'image/jpeg', 'image/pjpeg', 'image/gif']
		def frontImage = request.getFile('frontImage')
        if( frontImage.bytes.size() > 0 ) {
			if( frontImage.getContentType() && frontImage.getContentType() != "application/octet-stream"){
				if (! okcontents.contains(frontImage.getContentType())) {
	        		flash.message = "Front Image must be one of: ${okcontents}"
	        		render(view: "create", model: [modelInstance: modelInstance])
	        		return;
	        	}
        	}
        } else if(modelTemplate){
        	modelInstance.frontImage = modelTemplate.frontImage
        } else {
        	modelInstance.frontImage = null
        }
        def rearImage = request.getFile('rearImage')
        if( rearImage.bytes.size() > 0 ) {
			if( rearImage.getContentType() && rearImage.getContentType() != "application/octet-stream"){
				if (! okcontents.contains(rearImage.getContentType())) {
	        		flash.message = "Rear Image must be one of: ${okcontents}"
	        		render(view: "create", model: [modelInstance: modelInstance])
	        		return;
	        	}
        	}
        } else if(modelTemplate){
        	modelInstance.rearImage = modelTemplate.rearImage
        } else {
        	modelInstance.rearImage = null
        }
        if (modelInstance.save(flush: true)) {
        	def connectorCount = Integer.parseInt(params.connectorCount)
			if(connectorCount > 0){
	        	for(int i=1; i<=connectorCount; i++){
	        		def modelConnector = new ModelConnector(model : modelInstance,
	        												connector : params["connector"+i],
															label : params["label"+i],
															type :params["type"+i],
															labelPosition : params["labelPosition"+i],
															connectorPosX : Integer.parseInt(params["connectorPosX"+i]),
															connectorPosY : Integer.parseInt(params["connectorPosY"+i]),
															status:params["status"+i] )
	        		
	        		if (!modelConnector.hasErrors() )
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
				
				if (!powerConnector.save(flush: true)){
					def etext = "Unable to create Power Connectors for ${modelInstance}" +
					GormUtil.allErrorsString( powerConnector )
					println etext
				}
			}
			
        	modelInstance.sourceTDSVersion = 1
        	modelInstance.save(flush: true)
			def akaNames = params.list('aka')
			akaNames.each{ aka->
				modelInstance.findOrCreateAliasByName(aka.trim(), true)
			}
            flash.message = "${modelInstance.modelName} created"
            redirect(action: "show", id: modelInstance.id)
        }
        else {
        	flash.message = modelInstance.errors.allErrors.each{  log.error it }
			def	modelConnectors = modelTemplate ? ModelConnector.findAllByModel( modelTemplate ) : null
	    	def otherConnectors = []
			def existingConnectors = modelConnectors ? modelConnectors.size()+1 : 1
			for(int i = existingConnectors ; i<51; i++ ){
				otherConnectors << i
			}
            render(view: "create", model: [modelInstance: modelInstance, modelConnectors:modelConnectors,
										   otherConnectors:otherConnectors, modelTemplate:modelTemplate ] )
        }
    }

    def show = {
		def modelId = params.id
		if(modelId && modelId.isNumber()){
	        def model = Model.get(params.id)
			def subject = SecurityUtils.subject
	        if (!model) {
	        	flash.message = "Model not found with Id ${params.id}"
	            redirect(action: "list")
	        }
	        else {
	        	def modelConnectors = ModelConnector.findAllByModel( model,[sort:"id"] )
				def modelAkas = WebUtil.listAsMultiValueString(ModelAlias.findAllByModel(model, [sort:'name']).name)
				def paramsMap = [ modelInstance : model, modelConnectors : modelConnectors, modelAkas:modelAkas,
	                  			  modelHasPermission:RolePermissions.hasPermission("ValidateModel") ]
				if(params.redirectTo == "assetAudit"){
					render(template: "modelAuditView", model: paramsMap )
				}
	            return paramsMap
	        }
		} else {
			if(params.redirectTo == "assetAudit"){
				render "<b>Model not found with Id ${params.id}</b>"
			} else {
				flash.message = "Model not found with Id ${params.id}"
				redirect(action: "list")
			}
		}
    }

    def edit = {
		def modelId = params.id
		if(modelId && modelId.isNumber()){
	        def model = Model.get(params.id)
	        if (!model) {
	            flash.message = "Model not found with Id ${params.id}"
	            redirect(action: "list")
	        }
	        else {
	        	def modelConnectors = ModelConnector.findAllByModel( model,[sort:"id"] )
				def nextConnector = 0
				try{
					nextConnector = modelConnectors.size() > 0 ? Integer.parseInt(modelConnectors[modelConnectors.size()-1]?.connector) : 0
				} catch( NumberFormatException ex){
					nextConnector = modelConnectors.size()+1
				}
				def otherConnectors = []
				for(int i = nextConnector+1 ; i<51; i++ ){
					otherConnectors << i
				}
				def modelAliases = ModelAlias.findAllByModel(model, [sort:'name'])
				def paramsMap = [ modelInstance: model, modelConnectors : modelConnectors, otherConnectors : otherConnectors, 
	                nextConnector:nextConnector, modelAliases:modelAliases ]
				
	            return paramsMap
	        }
		} else {
			flash.message = "Model id ${params.id} is not a valid Id "
			redirect(action: "list")
		}
    }

    def update = {
		
        def modelInstance = Model.get(params.id)
		def modelStatus = modelInstance?.modelStatus 
		def endOfLifeDate = params.endOfLifeDate
		def formatter = new SimpleDateFormat("MM/dd/yyyy");
		def tzId = getSession().getAttribute( "CURR_TZ" )?.CURR_TZ
		def principal = SecurityUtils.subject?.principal
		def user
		def person
		if( principal ){
			user  = UserLogin.findByUsername( principal )
		    person = user.person
			if(user && person){
				def score = person?.modelScore ?: 0
				if(params?.modelStatus == "full" && modelStatus != params?.modelStatus){
					person.modelScore = score+20
				}else if(params?.modelStatus == "valid" && modelStatus != params?.modelStatus){
					if(modelInstance?.validatedBy?.id == person?.id && modelInstance.updatedBy?.id != person?.id ){
						person.modelScore = score+20
					} else {
						person.modelScore = score+50
					}
				}
				if(!person.save(flush:true)){
					person.errors.allErrors.each{println it}
				}
		     }
		}
		if(endOfLifeDate){
			params.endOfLifeDate =  GormUtil.convertInToGMT(formatter.parse(endOfLifeDate), tzId)
		}
		if( principal ){
			user  = UserLogin.findByUsername( principal )
		}
        if (modelInstance) {
			def powerNameplate = params.powerNameplate ? Float.parseFloat(params.powerNameplate) : 0
			def powerDesign = params.powerDesign ? Float.parseFloat(params.powerDesign) : 0
			def powerUsed = params.powerUse ? Float.parseFloat(params.powerUse) : 0
			def powerType = params.powerType
			if( powerType == "Amps"){
				powerNameplate = powerNameplate * 110
				powerDesign = powerDesign * 110
				powerUsed = powerUsed * 110
			}
        	params.useImage = params.useImage == 'on' ? 1 : 0
        	params.sourceTDS = params.sourceTDS == 'on' ? 1 : 0
			params.powerNameplate = powerNameplate
			params.powerDesign = powerDesign
			params.powerUse = powerUsed
            def okcontents = ['image/png', 'image/x-png', 'image/jpeg', 'image/pjpeg', 'image/gif']
    		def frontImage = request?.getFile('frontImage')
            if( frontImage ) {
    			if( frontImage.getContentType() && frontImage.getContentType() != "application/octet-stream"){
    				if (! okcontents.contains(frontImage.getContentType())) {
    	        		flash.message = "Front Image must be one of: ${okcontents}"
    	        		render(view: "create", model: [modelInstance: modelInstance])
    	        		return;
    	        	}
    				frontImage = frontImage.bytes
            	} else {
            		frontImage = modelInstance.frontImage
            	}
            }
            def rearImage = request?.getFile('rearImage')
            if( rearImage ) {
    			if( rearImage.getContentType() && rearImage.getContentType() != "application/octet-stream"){
    				if (! okcontents.contains(rearImage.getContentType())) {
    	        		flash.message = "Rear Image must be one of: ${okcontents}"
    	        		render(view: "create", model: [modelInstance: modelInstance])
    	        		return;
    	        	}
    				rearImage = rearImage.bytes
            	} else {
                	rearImage = modelInstance.rearImage
                }
            }
		
			modelInstance.height = params.modelHeight != "" ? Integer.parseInt(params.modelHeight):0 
			modelInstance.weight = params.modelWeight != "" ? Integer.parseInt(params.modelWeight):0 
			modelInstance.depth  = params.modelDepth  != "" ? Integer.parseInt(params.modelDepth):0 
			modelInstance.width  = params.modelWidth  != "" ? Integer.parseInt(params.modelWidth):0
            if( params?.modelStatus == 'valid' && modelStatus == 'full'){
			   modelInstance.validatedBy = user?.person
			   modelInstance.updatedBy =  modelInstance.updatedBy
			}else{
			   modelInstance.updatedBy = person
			}
            modelInstance.properties = params
            modelInstance.rearImage = rearImage
            modelInstance.frontImage = frontImage
			
			def oldModelManufacturer = modelInstance.manufacturer.id
			def oldModelType = modelInstance.assetType          
			
			if (!modelInstance.hasErrors() && modelInstance.save()) {
				
				def deletedAka = params.deletedAka
				def akaToSave = params.list('aka')
				if(deletedAka){
					ModelAlias.executeUpdate("delete from ModelAlias mo where mo.id in (:ids)",[ids:deletedAka.split(",").collect{return Long.parseLong(it)}])
				}
				def modelAliasList = ModelAlias.findAllByModel( modelInstance )
				modelAliasList.each{ modelAlias->
					modelAlias.name = params["aka_"+modelAlias.id]
					if(!modelAlias.save()){
						modelAlias.errors.allErrors.each {println it}
					}
				}
				akaToSave.each{aka->
					modelInstance.findOrCreateAliasByName(aka, true)
				}
				
				def connectorCount = 0
				if(params.connectorCount){
            	    connectorCount = Integer.parseInt(params.connectorCount)
				}
				if(connectorCount > 0){
		        	for(int i=1; i<=connectorCount; i++){
						def connector = params["connector"+i]
		        		def modelConnector = connector ? ModelConnector.findByModelAndConnector(modelInstance,connector) : ModelConnector.findByModelAndConnector(modelInstance,i)
						if( !connector && modelConnector ){
							
							modelConnector.delete(flush:true)
							
						} else {
							if(modelConnector){
								modelConnector.connector = params["connector"+i]
								modelConnector.label = params["label"+i]
								modelConnector.type = params["type"+i]
								modelConnector.labelPosition = params["labelPosition"+i]
								modelConnector.connectorPosX = Integer.parseInt(params["connectorPosX"+i])
								modelConnector.connectorPosY = Integer.parseInt(params["connectorPosY"+i])
								modelConnector.status = params["status"+i]
								
							} else if(connector){
								modelConnector = new ModelConnector(model : modelInstance,
			        												connector : params["connector"+i],
																	label : params["label"+i],
																	type : params["type"+i],
																	labelPosition : params["labelPosition"+i],
																	connectorPosX : Integer.parseInt(params["connectorPosX"+i]),
																	connectorPosY : Integer.parseInt(params["connectorPosY"+i]),
																	status : params["status"+i] )
			        		
							}
			        		if (modelConnector && !modelConnector.hasErrors() )
			        			modelConnector.save(flush: true)
						}
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
					
					if (!powerConnector.save(flush: true)){
						def etext = "Unable to create Power Connectors for ${modelInstance}" +
						GormUtil.allErrorsString( powerConnector )
						println etext
					}
				
				}
            	def assetEntitysByModel = AssetEntity.findAllByModel( modelInstance )
				def assetConnectors = ModelConnector.findAllByModel( modelInstance )
				assetEntitysByModel.each{ assetEntity ->
            		assetConnectors.each{connector->
            			
    					def assetCableMap = AssetCableMap.findByFromAssetAndFromConnectorNumber( assetEntity, connector )
						
						if( !assetCableMap ){
	    					assetCableMap = new AssetCableMap(
	    														cable : "Cable"+connector.connector,
	    														fromAsset: assetEntity,
	    														fromConnectorNumber : connector,
	    														status : connector.status
	    														)
							
						}
						if(assetEntity?.rackTarget && connector.type == "Power" && 
							connector.label?.toLowerCase() == 'pwr1' && !assetCableMap.toPower){
							assetCableMap.toAsset = assetEntity
							assetCableMap.toAssetRack = assetEntity?.rackTarget?.tag
							assetCableMap.toAssetUposition = 0
							assetCableMap.toConnectorNumber = null
							assetCableMap.toPower = "A"
						}
						if ( !assetCableMap.validate() || !assetCableMap.save() ) {
							def etext = "Unable to create assetCableMap for assetEntity ${assetEntity}" +
							GormUtil.allErrorsString( assetCableMap )
							println etext
							log.error( etext )
						}
    				}
					def assetCableMaps = AssetCableMap.findAllByFromAsset( assetEntity )
					assetCableMaps.each{assetCableMap->
						if(!assetConnectors.id?.contains(assetCableMap.fromConnectorNumber?.id)){
							AssetCableMap.executeUpdate("""Update AssetCableMap set status='missing',toAsset=null,
														toConnectorNumber=null,toAssetRack=null,toAssetUposition=null
														where toConnectorNumber = ${assetCableMap.fromConnectorNumber?.id}""")
							AssetCableMap.executeUpdate("delete AssetCableMap where fromConnectorNumber = ${assetCableMap.fromConnectorNumber?.id}")
						}
					}
            	}
            	def updateAssetsQuery = "update asset_entity set asset_type = '${modelInstance.assetType}' where model_id='${modelInstance.id}'"
            	jdbcTemplate.update(updateAssetsQuery)
                
				if(modelInstance.sourceTDSVersion){
	        		modelInstance.sourceTDSVersion ++
	    		} else {
	    			modelInstance.sourceTDSVersion = 1
	    		}
	        	modelInstance.save(flush: true)
				def newManufacturer = Manufacturer.get(params.manufacturer.id)
				if( oldModelManufacturer != params.manufacturer.id){
					def updateModelQuery = "update asset_entity set manufacturer = '${newManufacturer.name}' where model_id='${modelInstance.id}'"
					jdbcTemplate.update(updateModelQuery)
				}
				if(oldModelType!=params.assetType){
					def updateModelTypeQuery = "update asset_entity set asset_type = '${params.assetType}' where model_id='${modelInstance.id}'"
					jdbcTemplate.update(updateModelTypeQuery)
				
				}
				
				flash.message = "${modelInstance.modelName} Updated"
				if(params.redirectTo == "assetAudit"){
					render(template: "modelAuditView", model: [modelInstance:modelInstance] )
				}
                redirect(action: "show", id: modelInstance.id)
            }
            else {
				modelInstance.errors.allErrors.each {log.error it}
            	def modelConnectors = ModelConnector.findAllByModel( modelInstance )
				def otherConnectors = []
				for(int i = modelConnectors.size()+1 ; i<51; i++ ){
					otherConnectors << i
				}
                render(view: "edit", model: [modelInstance: modelInstance, modelConnectors : modelConnectors, otherConnectors : otherConnectors])
            }
        }
        else {
            flash.message = "Model not found with Id ${params.id}"
            redirect(action: "list")
        }
    }

    def delete = {
        def modelInstance = Model.get(params.id)
		def principal = SecurityUtils.subject?.principal
		def user
		def person
		if( principal ){
			user  = UserLogin.findByUsername( principal )
		    person = user.person
		}
        if(modelInstance) {
            try {
                modelInstance.delete(flush: true)
				if(user){
					int bonusScore = person?.modelScoreBonus ? person?.modelScoreBonus:0
				    person.modelScoreBonus = bonusScore+1
					int score =  person.modelScore ?: 0
					person.modelScore = score+bonusScore;
				}
				if(!person.save(flush:true)){
					person.errors.allErrors.each{
						println it
						}
				}
				
                flash.message = "${modelInstance} deleted"
                redirect(action: "list")
            } catch (org.springframework.dao.DataIntegrityViolationException e) {
            	flash.message = "${modelInstance} not deleted"
                redirect(action: "show", id: params.id)
            }
        }
        else {
        	flash.message = "Model not found with Id ${params.id}"
            redirect(action: "list")
        }
    }
    /*
     *  Send FrontImage as inputStream
     */
    def getFrontImage = {
		if( params.id ) {
    		def model = Model.findById( params.id )
     		def image = model?.frontImage
     		response.contentType = 'image/jpg'		
     		response.outputStream << image
		} else {
			return "";
		}
    }
    /*
     *  Send RearImage as inputStream
     */
    def getRearImage = {
		if( params.id ) {
    		def model = Model.findById( params.id )
     		def image = model?.rearImage
     		response.contentType = 'image/jpg'		
     		response.outputStream << image
		} else {
			return "";
		}
    }
    /*
     *  Send List of model as JSON object
     */
	def getModelsListAsJSON = {
    	def manufacturer = params.manufacturer
		def assetType = params.assetType
    	def models
		if(manufacturer){
			def manufacturerInstance = Manufacturer.get(manufacturer)
			models = manufacturerInstance ? Model.findAllByManufacturer( manufacturerInstance,[sort:'modelName',order:'asc'] )?.findAll{it.assetType == assetType } : null
		}
    	def modelsList = []
    	if(models.size() > 0){
    		models.each{
    			modelsList << [id:it.id, modelName:it.modelName]
    		}
    	}
		render modelsList as JSON
    }
    /*
     *  check to see that if they were any Asset records exist for the selected model before deleting it
     */
    def checkModelDependency = {
    	def modelId = params.modelId
		def modelInstance = Model.findById(Integer.parseInt(modelId))
		def returnValue = false
		if( modelInstance ){
			if( AssetEntity.findByModel( modelInstance ) )
				returnValue = true
		}
    	render returnValue
    }
    /*
     *  Return AssetCables to alert the user while deleting the connectors
     */
	def getAssetCablesForConnector = {
    	def modelId = params.modelId
		def modelInstance = Model.get(modelId)
		def assetCableMap = []
		if(modelInstance){
			def connector = params.connector
			def modelConnector = ModelConnector.findByConnectorAndModel( connector, modelInstance )
			assetCableMap = AssetCableMap.findAll("from AssetCableMap where status in ('empty','cabled','cabledDetails') and (fromConnectorNumber = ? or toConnectorNumber = ? )",[modelConnector,modelConnector])
		}
    	render assetCableMap as JSON
    }
    /*
     *  TEMP method to redirect to action : show
     */
    def cancel = {
    		 redirect(action: "show", id: params.id)
    }
    /*
     *  When the user clicks on an item do the following actions:
     *	1. Add to the AKA field list in the target record
	 *	2. Revise Asset, and any other records that may point to this model
	 *	3. Delete model record
	 *	4. Return to model list view with the flash message "Merge completed."
     */
	def merge = {
    	// Get the Model instances for params ids
		def toModel = Model.get(params.id)
		def fromModel = Model.get(params.fromId)
		
		//	Revise Asset, and any other records that may point to this model
		def fromModelAssets = AssetEntity.findAllByModel( fromModel )
		fromModelAssets.each{ assetEntity->
			assetEntity.model = toModel
			assetEntity.assetType = toModel.assetType
			assetEntity.save(flush:true)
			assetEntityAttributeLoaderService.updateModelConnectors( assetEntity )
		}
    	// Delete model associated record
		AssetCableMap.executeUpdate("delete AssetCableMap where fromConnectorNumber in (from ModelConnector where model = ${fromModel.id})")
		AssetCableMap.executeUpdate("""Update AssetCableMap set status='missing',toAsset=null,
												toConnectorNumber=null,toAssetRack=null,toAssetUposition=null
												where toConnectorNumber in (from ModelConnector where model = ${fromModel.id})""")
    	ModelConnector.executeUpdate("delete ModelConnector where model = ?",[fromModel])
		def toModelAlias = ModelAlias.findAllByModel(toModel).name
		
		// Add to the AKA field list in the target record
		if(!toModelAlias.contains(fromModel.modelName)){
			def fromModelAlias = ModelAlias.findAllByModel(fromModel)
			ModelAlias.executeUpdate("delete from ModelAlias mo where mo.model = ${fromModel.id}")
			
			fromModelAlias.each{
				toModel.findOrCreateAliasByName(it.name, true)
			}
			//merging fromModel as AKA of toModel
			toModel.findOrCreateAliasByName(fromModel.modelName, true)
			
			// Delete model record
			fromModel.delete()
			
			def principal = SecurityUtils.subject?.principal
			def user
			if( principal ){
				user = UserLogin.findByUsername( principal )
				def person = user.person
				def bonusScore = person.modelScoreBonus ? person.modelScoreBonus:0
				if(user){
					person.modelScoreBonus = bonusScore+10
					person.modelScore = person.modelScoreBonus + person.modelScore
				    person.save(flush:true)
				}
		}
		} else {
			//	Delete model record
			fromModel.delete()
			sessionFactory.getCurrentSession().flush();
		}
		// Return to model list view with the flash message "Merge completed."
    	flash.message = "Merge completed."
    	redirect(action:list)
    }
    /*
     * 
     */
	def importExport = {
		if( params.message ) {
			flash.message = params.message
		}
		
		def batchCount = jdbcTemplate.queryForInt("select count(*) from ( select * from manufacturer_sync group by batch_id ) a")
		[batchCount:batchCount]
    }
    /*
     * Use excel format with the manufacturer,model and connector sheets. 
     * The file name should be of the format TDS-Sync-Data-2011-05-02.xls with the current date.
     */
    def export = {
        //get template Excel
        try {
        	File file =  ApplicationHolder.application.parentContext.getResource( "/templates/Sync_model_template.xls" ).getFile()
			WorkbookSettings wbSetting = new WorkbookSettings()
			wbSetting.setUseTemporaryFileDuringWrite(true)
			def workbook = Workbook.getWorkbook( file, wbSetting )
			//set MIME TYPE as Excel
			DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			def filename = 	"TDS-Sync-Data-"+formatter.format(new Date())+".xls"
					filename = filename.replace(" ", "_")
			response.setContentType( "application/vnd.ms-excel" )
			response.setHeader( "Content-Disposition", "attachment; filename = ${filename}" )
			
			def book = Workbook.createWorkbook( response.getOutputStream(), workbook )
			
			def manuSheet = book.getSheet("manufacturer")
			def manufacturers = params.exportCheckbox ? Model.findAll("FROM Model where sourceTDS = 1 GROUP BY manufacturer").manufacturer :
			 Manufacturer.findAll()
			def dateFormatter = new SimpleDateFormat("MM/dd/yyyy");
			
			for ( int r = 0; r < manufacturers.size(); r++ ) {
				manuSheet.addCell( new Label( 0, r+1, String.valueOf(manufacturers[r].id )) )
				manuSheet.addCell( new Label( 1, r+1, String.valueOf(manufacturers[r].name )) )
				manuSheet.addCell( new Label( 2, r+1, String.valueOf(WebUtil.listAsMultiValueString(ManufacturerAlias.findAllByManufacturer(manufacturers[r]).name) )) )
				manuSheet.addCell( new Label( 3, r+1, String.valueOf(manufacturers[r].description ? manufacturers[r].description : "" )) )
			}
			def modelSheet = book.getSheet("model")
			def models = params.exportCheckbox == '1' ? Model.findAllBySourceTDS(1) : Model.findAll()

			for ( int r = 0; r < models.size(); r++ ) {
				modelSheet.addCell( new Label( 0, r+1, String.valueOf(models[r].id )) )
				modelSheet.addCell( new Label( 1, r+1, String.valueOf(models[r].modelName )) )
				modelSheet.addCell( new Label( 2, r+1, String.valueOf(WebUtil.listAsMultiValueString(ModelAlias.findAllByModel(models[r]).name))) )
				modelSheet.addCell( new Label( 3, r+1, String.valueOf(models[r].description ? models[r].description : "" )) )
				modelSheet.addCell( new Label( 4, r+1, String.valueOf(models[r].manufacturer.id )) )
				modelSheet.addCell( new Label( 5, r+1, String.valueOf(models[r].manufacturer.name )) )
				modelSheet.addCell( new Label( 6, r+1, String.valueOf(models[r].assetType )) )
				modelSheet.addCell( new Label( 7, r+1, String.valueOf(models[r].bladeCount ? models[r].bladeCount : "" )) )
				modelSheet.addCell( new Label( 8, r+1, String.valueOf(models[r].bladeLabelCount ? models[r].bladeLabelCount : "" )) )
				modelSheet.addCell( new Label( 9, r+1, String.valueOf(models[r].bladeRows ? models[r].bladeRows : "" )) )
				modelSheet.addCell( new Label( 10, r+1, String.valueOf(models[r].sourceTDS == 1 ? "TDS" : "" )) )
				modelSheet.addCell( new Label( 11, r+1, String.valueOf(models[r].powerNameplate ? models[r].powerNameplate : "" )) )
				modelSheet.addCell( new Label( 12, r+1, String.valueOf(models[r].powerDesign ? models[r].powerDesign : "" )) )
				modelSheet.addCell( new Label( 13, r+1, String.valueOf(models[r].powerUse ? models[r].powerUse : "" )) )
				modelSheet.addCell( new Label( 14, r+1, String.valueOf(models[r].roomObject==1 ? 'True' : 'False' )) )
				modelSheet.addCell( new Label( 15, r+1, String.valueOf(models[r].sourceTDSVersion ? models[r].sourceTDSVersion : 1 )) )
				modelSheet.addCell( new Label( 16, r+1, String.valueOf(models[r].useImage == 1 ? "yes" : "no" )) )
				modelSheet.addCell( new Label( 17, r+1, String.valueOf(models[r].usize ? models[r].usize : "")) )
				modelSheet.addCell( new Label( 18, r+1, String.valueOf(models[r].height ? models[r].height : "")) )
				modelSheet.addCell( new Label( 19, r+1, String.valueOf(models[r].weight ? models[r].weight : "")) )
				modelSheet.addCell( new Label( 20, r+1, String.valueOf(models[r].depth ? models[r].depth : "")) )
				modelSheet.addCell( new Label( 21, r+1, String.valueOf(models[r].width ? models[r].width : "")) )
				modelSheet.addCell( new Label( 22, r+1, String.valueOf(models[r].layoutStyle ? models[r].layoutStyle: "")) )
				modelSheet.addCell( new Label( 23, r+1, String.valueOf(models[r].productLine ? models[r].productLine :"")) )
				modelSheet.addCell( new Label( 24, r+1, String.valueOf(models[r].modelFamily ? models[r].modelFamily :"")) )
				modelSheet.addCell( new Label( 25, r+1, String.valueOf(models[r].endOfLifeDate ? models[r].endOfLifeDate :"")) )
				modelSheet.addCell( new Label( 26, r+1, String.valueOf(models[r].endOfLifeStatus ? models[r].endOfLifeStatus :"")) )
				modelSheet.addCell( new Label( 27, r+1, String.valueOf(models[r].createdBy ? models[r].createdBy :"")) )
				modelSheet.addCell( new Label( 28, r+1, String.valueOf(models[r].updatedBy ? models[r].updatedBy :"")) )
				modelSheet.addCell( new Label( 29, r+1, String.valueOf(models[r].validatedBy ? models[r].validatedBy : "")) )
				modelSheet.addCell( new Label( 30, r+1, String.valueOf(models[r].sourceURL ? models[r].sourceURL :"")) )
				modelSheet.addCell( new Label( 31, r+1, String.valueOf(models[r].modelStatus ? models[r].modelStatus:"")) )
				modelSheet.addCell( new Label( 32, r+1, String.valueOf(models[r].modelScope ? models[r].modelScope :"")) )
				modelSheet.addCell( new Label( 33, r+1, String.valueOf(models[r].dateCreated ? dateFormatter.format(models[r].dateCreated) : '')) )
				modelSheet.addCell( new Label( 34, r+1, String.valueOf(models[r].lastModified ? dateFormatter.format(models[r].lastModified) : '')) )

			}
			def connectorSheet = book.getSheet("connector")
			def connectors = params.exportCheckbox ? ModelConnector.findAll("FROM ModelConnector where model.sourceTDS = 1 order by model.id") : 
				ModelConnector.findAll()
			
			for ( int r = 0; r < connectors.size(); r++ ) {
				connectorSheet.addCell( new Label( 0, r+1, String.valueOf(connectors[r].id )) )
				connectorSheet.addCell( new Label( 1, r+1, String.valueOf(connectors[r].connector )) )
				connectorSheet.addCell( new Label( 2, r+1, String.valueOf(connectors[r].connectorPosX )) )
				connectorSheet.addCell( new Label( 3, r+1, String.valueOf(connectors[r].connectorPosY )) )
				connectorSheet.addCell( new Label( 4, r+1, String.valueOf(connectors[r].label ? connectors[r].label : "" )) )
				connectorSheet.addCell( new Label( 5, r+1, String.valueOf(connectors[r].labelPosition )) )
				connectorSheet.addCell( new Label( 6, r+1, String.valueOf(connectors[r].model.id )) )
				connectorSheet.addCell( new Label( 7, r+1, String.valueOf(connectors[r].model.modelName )) )
				connectorSheet.addCell( new Label( 8, r+1, String.valueOf(connectors[r].option ? connectors[r].option : "" )) )
				connectorSheet.addCell( new Label( 9, r+1, String.valueOf(connectors[r].status )) )
				connectorSheet.addCell( new Label( 10, r+1, String.valueOf(connectors[r].type )) )
			}
			book.write()
			book.close()
		} catch( Exception ex ) {
			flash.message = "Exception occurred while exporting data"+ex
			redirect( controller:'model', action:"importExport")
			return;
		}
    }
    /*
     *1. On upload the system should put the data into temporary tables and then perform validation to make sure the data is proper and ready.
	 *2. Step through each imported model:
	 *2a if it's SourceTDSVersion is higher than the one in the database, update the database with the new model and connector data.
	 *2b If it is lower, skip it.
	 *3. Report the number of Model records updated.
     */
    def upload = {
		DataTransferBatch.withTransaction { status ->
			//get user name.
			def subject = SecurityUtils.subject
			def principal = subject.principal
			def userLogin = UserLogin.findByUsername( principal )
	        // get File
	        MultipartHttpServletRequest mpr = ( MultipartHttpServletRequest )request
	        CommonsMultipartFile file = ( CommonsMultipartFile ) mpr.getFile("file")
			def date = new Date();
	        def modelSyncBatch = new ModelSyncBatch(userlogin:userLogin, changesSince:date,createdBy:userLogin,source:"TDS").save()
	        // create workbook
	        def workbook
	        def sheetNameMap = new HashMap()
	        //get column name and sheets
			sheetNameMap.put( "manufacturer", ["manufacturer_id", "name", "aka", "description"] )
			sheetNameMap.put( "model", ["model_id", "name","aka","description","manufacturer_id","manufacturer_name","asset_type","blade_count","blade_label_count","blade_rows","sourcetds","power_nameplate","power_design","power_use","sourcetdsversion","use_image","usize","height","weight","depth","width", "layout_style","product_line","model_family","end_of_life_date","end_of_life_status","created_by","updated_by","validated_by","sourceurl","model_status","model_scope"] )
			sheetNameMap.put( "connector", ["model_connector_id", "connector", "connector_posx", "connector_posy", "label", "label_position", "model_id", "model_name", "connector_option", "status", "type"] )
	        try {
	            workbook = Workbook.getWorkbook( file.inputStream )
	            List sheetNames = workbook.getSheetNames()
				def sheets = sheetNameMap.keySet()
				def missingSheets = []
	            def flag = 1
	            def sheetsLength = sheets.size()
				
				sheets.each{
					if ( !sheetNames.contains( it ) ) {
	                    flag = 0
						missingSheets<< it
	                }
				}
	            if( flag == 0 ) {
	                flash.message = "${missingSheets} sheets not found, Please check it."
	                redirect( action:importExport, params:[message:flash.message] )
	                return;
	            } else {
	            	def manuAdded = 0
					def manuSkipped = []
	            	def sheetColumnNames = [:]
	                //check for column
					def manuSheet = workbook.getSheet( "manufacturer" )
	                def manuCol = manuSheet.getColumns()
	                for ( int c = 0; c < manuCol; c++ ) {
	                    def cellContent = manuSheet.getCell( c, 0 ).contents
	                    sheetColumnNames.put(cellContent, c)
	                }
	                def missingHeader = checkHeader( sheetNameMap.get("manufacturer"), sheetColumnNames )
	                // Statement to check Headers if header are not found it will return Error message
	                if ( missingHeader != "" ) {
	                    flash.message = " Column Headers : ${missingHeader} not found, Please check it."
	                    redirect( action:importExport, params:[message:flash.message] )
	                    return;
	                } else {
	                    def sheetrows = manuSheet.rows
	                    for ( int r = 1; r < sheetrows ; r++ ) {
	                		def valueList = new StringBuffer("(")
	                    	for( int cols = 0; cols < manuCol; cols++ ) {
	                    		valueList.append("'"+manuSheet.getCell( cols, r ).contents.replace("'","\\'")+"',")
	                        }
	                		try{
	                			jdbcTemplate.update("insert into manufacturer_sync( manufacturer_temp_id, name,aka, description, batch_id) values "+valueList.toString()+"${modelSyncBatch.id})")
								manuAdded = r
	                		} catch (Exception e) {
	                			manuSkipped += ( r +1 )
	                		}
	                    }
	
	                }
	                /*
	                 *  Import Model Information
	                 */
					def modelSheetColumnNames = [:]
					def modelAdded = 0
					def modelSkipped = []
	                 //check for column
	 				def modelSheet = workbook.getSheet( "model" )
					def modelCol = modelSheet.getColumns()
					//def colContain = modelCol.
					for ( int c = 0; c < modelCol; c++ ) {
						def cellContent = modelSheet.getCell( c, 0 ).contents
						modelSheetColumnNames.put(cellContent, c)
					}
	                missingHeader = checkHeader( sheetNameMap.get("model"), modelSheetColumnNames )
					def onlyTds 
					// Statement to check Headers if header are not found it will return Error message
					if ( missingHeader != "" ) {
						flash.message = " Column Headers : ${missingHeader} not found, Please check it."
						redirect( action:importExport, params:[ message:flash.message] )
						return;
					} else {
						def sheetrows = modelSheet.rows
						for ( int r = 1; r < sheetrows ; r++ ) {
							onlyTds = false
							def valueList = new StringBuffer("(")
		             		def manuId
							def createdPersonId
							def updatedPersonId
							def validatedPersonId
							def projectId
		                 	for( int cols = 0; cols < modelCol; cols++ ) {
								switch(modelSheet.getCell( cols, 0 ).contents){
								case "manufacturer_name" : 
									def manuName = modelSheet.getCell( cols, r ).contents
									manuId = ManufacturerSync.findByNameAndBatch(manuName,modelSyncBatch)?.id
									valueList.append("'"+modelSheet.getCell( cols, r ).contents.replace("'","\\'")+"',")
									break;
								case "blade_count" : 
									valueList.append((modelSheet.getCell( cols, r ).contents ? modelSheet.getCell( cols, r ).contents : null)+",")
									break;
								case "blade_label_count" :
									valueList.append((modelSheet.getCell( cols, r ).contents ? modelSheet.getCell( cols, r ).contents : null)+",")
									break;
								case "blade_rows" : 
									valueList.append((modelSheet.getCell( cols, r ).contents ? modelSheet.getCell( cols, r ).contents : null)+",")
									break;
								case "use_image" : 
									int useImage = 0
									if(modelSheet.getCell( cols, r ).contents.toLowerCase() != "no"){
										useImage = 1
									}
									valueList.append(useImage+",")
									break;
								case "power_nameplate" : 
									valueList.append((modelSheet.getCell( cols, r ).contents ? modelSheet.getCell( cols, r ).contents : null)+",")
									break;
								case "power_design" : 
									valueList.append((modelSheet.getCell( cols, r ).contents ? modelSheet.getCell( cols, r ).contents : null)+",")
									break;
								case "power_use" : 
									valueList.append((modelSheet.getCell( cols, r ).contents ? modelSheet.getCell( cols, r ).contents : null)+",")
									break;
								case "usize" : 
									valueList.append((modelSheet.getCell( cols, r ).contents ? modelSheet.getCell( cols, r ).contents : null)+",")
									break;
								case "sourcetds" : 
									int isTDS = 0
									if(modelSheet.getCell( cols, r ).contents.toLowerCase() == "tds"){
										isTDS = 1
										onlyTds = true
									}
									valueList.append(isTDS+",")
									break;
								case "sourcetdsversion" : 
									valueList.append((modelSheet.getCell( cols, r ).contents ? modelSheet.getCell( cols, r ).contents : null)+",")
									break;
								case "height" :
									valueList.append((modelSheet.getCell( cols, r ).contents ? modelSheet.getCell( cols, r ).contents : null)+",")
									break;
								case "weight" :
									valueList.append((modelSheet.getCell( cols, r ).contents ? modelSheet.getCell( cols, r ).contents : null)+",")
									break;
								case "depth" :
									valueList.append((modelSheet.getCell( cols, r ).contents ? modelSheet.getCell( cols, r ).contents : null)+",")
									break;
								case "width" :
									valueList.append((modelSheet.getCell( cols, r ).contents ? modelSheet.getCell( cols, r ).contents : null)+",")
									break;
								case "model_scope" :
								    def modelScope = modelSheet.getCell( cols, r ).contents
									projectId = Project.findByProjectCode(modelScope)?.id
									//valueList.append((modelSheet.getCell( cols, r ).contents ? modelSheet.getCell( cols, r ).contents : null)+",")
									break;
								case "end_of_life_date" :
								    def endOfLifeDate = modelSheet.getCell( cols, r ).contents
									if(endOfLifeDate){
									valueList.append("'"+(modelSheet.getCell( cols, r ).contents ? modelSheet.getCell( cols, r ).contents : null)+"',")
									}else{
									valueList.append((modelSheet.getCell( cols, r ).contents ? modelSheet.getCell( cols, r ).contents : null)+",")
									}
									break;
								/*case "end_of_life_status" :
									valueList.append((modelSheet.getCell( cols, r ).contents ? modelSheet.getCell( cols, r ).contents : null)+",")
									break;*/
								case "created_by" :
								    def createdByName = modelSheet.getCell( cols, r ).contents
									createdPersonId = Person.findByFirstName(createdByName)?.id
									break;
								case "updated_by" :
									def updatedByName = modelSheet.getCell( cols, r ).contents
									updatedPersonId = Person.findByFirstName(updatedByName)?.id
									//valueList.append((modelSheet.getCell( cols, r ).contents ? modelSheet.getCell( cols, r ).contents : null)+",")
									break;
								case "validated_by" :
									def validatedByName = modelSheet.getCell( cols, r ).contents
									validatedPersonId = Person.findByFirstName(validatedByName)?.id
									//valueList.append((modelSheet.getCell( cols, r ).contents ? modelSheet.getCell( cols, r ).contents : null)+",")
									break;
								default : 
									valueList.append("'"+modelSheet.getCell( cols, r ).contents.replace("'","\\'")+"',")
									break;
								}
		                 									
		                 	}
		             		try{
		             			if(manuId){ 
									 if(params.importCheckbox ){
										 if(onlyTds == true) {
											jdbcTemplate.update("insert into model_sync( model_temp_id, name,aka, description,manufacturer_temp_id,manufacturer_name,asset_type,blade_count,blade_label_count,blade_rows,sourcetds,power_nameplate,power_design,power_use,sourcetdsversion,use_image,usize,height,weight,depth,width,layout_style,product_line,model_family,end_of_life_date,end_of_life_status,sourceurl,model_status,batch_id,manufacturer_id,created_by_id,updated_by_id,validated_by_id, model_scope_id ) values "+valueList.toString()+"${modelSyncBatch.id}, $manuId, $createdPersonId, $updatedPersonId, $validatedPersonId, $projectId)")
											modelAdded = r                                                                                                                                                                                                                    
										 } else {
										 // TODO : getting ArrayIndexOutOfbound exception, need to fix
										 	//modelSkipped += ( r +1 )
										 }	
									 } else {
									 	jdbcTemplate.update("insert into model_sync( model_temp_id, name,aka, description,manufacturer_temp_id,manufacturer_name,asset_type,blade_count,blade_label_count,blade_rows,sourcetds,power_nameplate,power_design,power_use,sourcetdsversion,use_image,usize,height,weight,depth,width,layout_style,product_line,model_family,end_of_life_date,end_of_life_status,sourceurl,model_status,batch_id,manufacturer_id,created_by_id,updated_by_id,validated_by_id, model_scope_id ) values "+valueList.toString()+"${modelSyncBatch.id}, $manuId, $createdPersonId, $updatedPersonId, $validatedPersonId, $projectId) ")
										 modelAdded = r
									 }	  
		             			} else {
		             				//modelSkipped += ( r +1 )
		             			}
		             		} catch (Exception e) {
								 e.printStackTrace()
		             			//modelSkipped += ( r +1 )
								 e.printStackTrace()
		             		}
		                }
					}
	                /*
	                 *  Import Model Information
	                 */
					def connectorSheetColumnNames = [:]
					def connectorAdded = 0
					def connectorSkipped = []
	                 //check for column
	 				def connectorSheet = workbook.getSheet( "connector" )
					def connectorCol = connectorSheet.getColumns()
					for ( int c = 0; c < connectorCol; c++ ) {
						def cellContent = connectorSheet.getCell( c, 0 ).contents
						connectorSheetColumnNames.put(cellContent, c)
					}
	                missingHeader = checkHeader( sheetNameMap.get("connector"), connectorSheetColumnNames )
					// Statement to check Headers if header are not found it will return Error message
					if ( missingHeader != "" ) {
						flash.message = " Column Headers : ${missingHeader} not found, Please check it."
						redirect( action:importExport, params:[message:flash.message] )
						return;
					} else {
						def sheetrows = connectorSheet.rows
						for ( int r = 1; r < sheetrows ; r++ ) {
							def valueList = new StringBuffer("(")
		             		def modelId
		                 	for( int cols = 0; cols < connectorCol; cols++ ) {
		                 		
								switch(connectorSheet.getCell( cols, 0 ).contents){
								case "model_name" : 
									def modelName = connectorSheet.getCell( cols, r ).contents
									modelId = ModelSync.findByModelNameAndBatch(modelName,modelSyncBatch)?.id
									valueList.append("'"+connectorSheet.getCell( cols, r ).contents.replace("'","\\'")+"',")
									break;
								case "connector_posx" : 
									valueList.append((connectorSheet.getCell( cols, r ).contents ? connectorSheet.getCell( cols, r ).contents : null)+",")
									break;
								case "connector_posy" :
									valueList.append((connectorSheet.getCell( cols, r ).contents ? connectorSheet.getCell( cols, r ).contents : null)+",")
									break;
								default : 
									valueList.append("'"+connectorSheet.getCell( cols, r ).contents.replace("'","\\'")+"',")
									break;
								}
		                 									
		                 	}
		             		try{
		             			if(modelId){
			             			jdbcTemplate.update("insert into model_connector_sync( connector_temp_id,connector,connector_posx,connector_posy,label,label_position,model_temp_id,model_name,connector_option,status,type,batch_id,model_id ) values "+valueList.toString()+"${modelSyncBatch.id}, $modelId)")
									connectorAdded = r
		             			} else {
		             				connectorSkipped += ( r +1 )
		             			}
		             		} catch (Exception e) {
		             			connectorSkipped += ( r +1 )
		             		}
		                }
					} 
	                workbook.close()
	                if (manuSkipped.size() > 0 || modelSkipped.size() > 0 || connectorSkipped.size() > 0) {
	                    flash.message = " File Uploaded Successfully with Manufactures:${manuAdded},Model:${modelAdded},Connectors:${connectorAdded} records. and  Manufactures:${manuSkipped},Model:${modelSkipped},Connectors:${connectorSkipped} Records skipped Please click the Manage Batches to review and post these changes."
	                } else {
	                    flash.message = " File uploaded successfully with Manufactures:${manuAdded},Model:${modelAdded},Connectors:${connectorAdded} records.  Please click the Manage Batches to review and post these changes."
	                }
	                redirect( action:importExport, params:[message:flash.message] )
		            return;  
		        }
	        } catch( NumberFormatException ex ) {
	            flash.message = ex
	            status.setRollbackOnly()
	            redirect( action:importExport, params:[message:flash.message] )
	            return;
	        } catch( Exception ex ) {
	        	ex.printStackTrace()
				status.setRollbackOnly()
	            flash.message = ex
	            redirect( action:importExport, params:[message:flash.message] )
	            return;
	        } 
		}
    }
    def checkHeader( def list, def sheetColumnNames  ) {  
    	def missingHeader = ""
        def listSize = list.size()
        for ( int coll = 0; coll < listSize; coll++ ) {
            if( !sheetColumnNames.containsKey( list[coll] ) ) {
                missingHeader = missingHeader + ", " + list[coll]
            }
        }
    	return missingHeader
    }
    def manageImports = {
    	[modelSyncBatch:ModelSyncBatch.list()]
    }
    /*
     *  Send Model details as JSON object
     */
	def getModelAsJSON = {
    	def id = params.id
    	def model = Model.get(params.id)
		def powerNameplate = model.powerNameplate
		def powerDesign = model.powerDesign
		def powerUsed = model.powerUse
		if( session.getAttribute("CURR_POWER_TYPE")?.CURR_POWER_TYPE !='Watts'){
			powerNameplate = powerNameplate ? powerNameplate / 110 : ''
			powerNameplate = powerNameplate ? powerNameplate.toDouble().round(1) : ''
			powerDesign = powerDesign ? powerDesign / 110 : ''
			powerDesign = powerDesign ? powerDesign.toDouble().round(1) : ''
			powerUsed = powerUsed ? powerUsed / 110 : ''
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
						powerDesign : powerDesign
						
						]
    	render modelMap as JSON
    }
	def validateModel={
		def modelInstance = Model.get(params.id)
		def principal = SecurityUtils.subject?.principal
		def user
		if(principal){
			user  = UserLogin.findByUsername( principal )
		    def  person = user.person
			modelInstance.validatedBy = person
			modelInstance.modelStatus = "valid"
			
		}
		if(!modelInstance.save(flush:true)){
			modelInstance.errors.allErrors.each { println it }
		}
		flash.message = "${modelInstance.modelName} Validated"
		render (view: "show",model:[id: modelInstance.id,modelInstance:modelInstance])
	}

	/**
	 *  Validate whether requested AKA already exist in DB or not
	 *  @param: aka, name of aka
	 *  @param: id, id of model
	 *  @return : return aka if exists
	 */
	def validateAKA = {
		def duplicateAka = ""
		def aka = params.name
		def modelId = params.id
		def akaExist = Model.findByModelName(aka)
        
        if(akaExist) {
            duplicateAka = aka
        } else if( modelId ){
			def model = Model.read(modelId)
			def akaInAlias = ModelAlias.findByNameAndManufacturer(aka, model.manufacturer)
			if( akaInAlias ){
				duplicateAka = aka
			}
		} 
        
	
		render duplicateAka
	}
	/**
	 * this method is used to update model for audit view , not using update method as there have a lot of code in update action might degrade performance.
	 * @param id : id of model for update
	 * 
	 */
	def updateModel = {
		def modelId = params.id
		if(modelId && modelId.isNumber()){
			def model = Model.get( params.id ) 
			model.properties = params
			if(!model.save(flush:true)){
				model.errors.allErrors.each{
					log.error it
				}
			}
			render(template: "modelAuditView", model: [modelInstance:model] )
		}
	}
	
	/**
	 * render a list of suggestions for model's initial.
	 * @param : value is initial for which user wants suggestions .
	 * @return : sugesstion template.
	 */
	def autoCompleteModel ={
		def initials = params.value
		def manufacturer = params.manufacturer
		def manu = Manufacturer.findByName( manufacturer )
		def models = []
		if( manu ){
			models =  initials ? Model.findAllByModelNameIlikeAndManufacturer(initials+"%", manu) : []
		}
		[models:models]
	}
	
	/**
	 * Fetch models's type for model name
	 * @param value : name of model name
	 * @return model's assetType
	 * 
	 */
	def getModelType ={
		def modelName = params.value
		def model = Model.findByModelName( modelName )
		def modelType = model?.assetType ?: 'Server'
		render modelType
	}
	
	/**
	 * Methods checks whether model exist in model or model alias table
	 * @param modelName : name of model
	 * @param manufacturerName : name of manufacturer
	 * @return : modelAuditEdit template
	 */
	def getModelDetailsByName ={
		def modelName = params.modelName
		def manufacturerName = params.manufacturerName
		def model = assetEntityAttributeLoaderService.findOrCreateModel(manufacturerName, modelName, '', false)
		if(model){
			render(template: "modelAuditEdit", model: [modelInstance:model] )
		} else {
			render "<b> No Model found of name ${params.modelName}</b>"
		}
		
	}
}
