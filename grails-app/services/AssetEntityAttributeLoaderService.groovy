import jxl.*
import jxl.read.biff.*
import jxl.write.*

import org.apache.commons.lang.math.NumberUtils
import org.apache.commons.lang.StringUtils
import org.codehaus.groovy.grails.commons.GrailsClassUtils

import com.tds.asset.AssetCableMap
import com.tds.asset.AssetEntity
import com.tdssrc.eav.EavAttribute
import com.tdssrc.eav.EavAttributeOption
import com.tdssrc.eav.EavAttributeSet
import com.tdssrc.eav.EavEntityAttribute
import com.tdssrc.eav.EavEntityType
import com.tdsops.tm.enums.domain.SizeScale
import com.tdsops.tm.enums.domain.AssetCableStatus
import com.tdssrc.grails.DateUtil
import com.tdssrc.grails.GormUtil

class AssetEntityAttributeLoaderService {

	boolean transactional = true
	def eavAttribute
	def projectService
	def securityService
	def partyRelationshipService

	protected static bundleMoveAndClientTeams = ['sourceTeamMt','sourceTeamLog','sourceTeamSa','sourceTeamDba','targetTeamMt','targetTeamLog','targetTeamSa','targetTeamDba']
	protected static targetTeamType = ['MOVE_TECH':'targetTeamMt', 'CLEANER':'targetTeamLog','SYS_ADMIN':'targetTeamSa',"DB_ADMIN":'targetTeamDba']
	protected static sourceTeamType = ['MOVE_TECH':'sourceTeamMt', 'CLEANER':'sourceTeamLog','SYS_ADMIN':'sourceTeamSa',"DB_ADMIN":'sourceTeamDba']
	
	/*
	 * upload records in to EavAttribute table from from AssetEntity.xls
	 */
	def uploadEavAttribute = {def stream ->
		//get Entity TYpe
		def entityType = EavEntityType.findByEntityTypeCode( "AssetEntity" )
		// create workbook
		def workbook
		def sheet
		def sheetNo = 0
		def map = [
			"Attribute Code":null,
			"Label":null,
			"Type":null,
			"sortOrder":null,
			"Note":null,
			"Mode":null,
			"Input type":null,
			"Required":null,
			"Unique":null,
			"Business Rules (hard/soft errors)":null,
			"Spreadsheet Sheet Name":null,
			"Spreadsheet Column Name":null,
			"Options":null,
			"Walkthru Sheet Name":null,
			"Walkthru Column Name":null ]

		try{
			workbook = Workbook.getWorkbook( stream )
			sheet = workbook.getSheet( sheetNo )
			// export should use the same map.
			//check for column
			def col = sheet.getColumns()
			def checkCol = checkHeader( col, map, sheet )
			// Statement to check Headers if header are not found it will return Error message
			if ( checkCol == false ) {
				println "headers not matched "
			} else {

				// Iterate over the spreadsheet rows and populate the EavAttribute table appropriately
				for ( int r = 1; r < sheet.rows; r++ ) {
					// get fields
					def attributeCode = sheet.getCell( map["Attribute Code"], r ).contents
					def backEndType = sheet.getCell( map["Type"], r ).contents
					def frontEndInput = sheet.getCell( map["Input type"], r ).contents
					def fronEndLabel = sheet.getCell( map["Label"], r ).contents
					def isRequired = sheet.getCell( map["Required"], r ).contents
					def isUnique = sheet.getCell( map["Unique"], r ).contents
					def note = sheet.getCell( map["Note"], r ).contents
					def mode = sheet.getCell( map["Mode"], r ).contents
					def sortOrder = sheet.getCell( map["sortOrder"], r ).contents
					def validation = sheet.getCell( map["Business Rules (hard/soft errors)"], r ).contents
					def options = sheet.getCell( map["Options"], r ).contents
					def spreadSheetName = sheet.getCell( map["Spreadsheet Sheet Name"], r ).contents
					def spreadColumnName = sheet.getCell( map["Spreadsheet Column Name"], r ).contents
					def walkthruSheetName = sheet.getCell( map["Walkthru Sheet Name"], r ).contents
					def walkthruColumnName = sheet.getCell( map["Walkthru Column Name"], r ).contents
					// save data in to db(eavAttribute)

					// Only save "Actual" or "Reference" attributes for the time being
					if ( ! "AR".contains(mode) ) continue

					// Try saving
					eavAttribute = new EavAttribute( attributeCode:attributeCode,
						note: note,
						backendType: backEndType,
						frontendInput: frontEndInput,
						entityType: entityType,
						frontendLabel: fronEndLabel,
						defaultValue: "null",
						validation: validation,
						isRequired: (isRequired.equalsIgnoreCase("X"))?1:0,
						isUnique: (isUnique.equalsIgnoreCase("X"))?1:0,
						sortOrder: sortOrder 
					)

					// Make sure we can save the record
					if ( ! eavAttribute.validate() || ! eavAttribute.save() ) {
						log.error "Unable to load attribute " + com.tdssrc.grails.GormUtil.allErrorsString( eavAttribute )
						continue
					}

					//create DataTransferAttributeMap records related to the DataTransferSet
					//def dataTransferSetId
					def dataTransferSet
					try {
						dataTransferSet = DataTransferSet.findByTitle( "TDS Master Spreadsheet" )
						def dataTransferAttributeMap = new DataTransferAttributeMap(
							columnName: spreadColumnName,
							sheetName: spreadSheetName,
							dataTransferSet: dataTransferSet,
							eavAttribute: eavAttribute,
							validation: validation,
							isRequired: (isRequired.equalsIgnoreCase("X"))?1:0
						)
						if( dataTransferAttributeMap ){
							if ( ! dataTransferAttributeMap.save() ) {
								log.error "Failed to load DataTransferAttributeMap for TDS Master" +
								com.tdssrc.grails.GormUtil.allErrorsString( dataTransferAttributeMap )
							}
						}
					} catch ( Exception ex ) {
						ex.printStackTrace()
					}
					// create DataTransferAttributeMap records (WalkThrough columns)related to the DataTransferSet
					   
					try {
						dataTransferSet = DataTransferSet.findByTitle( "TDS Walkthru" )
						def dataTransferAttributeMap = new DataTransferAttributeMap(
							columnName:walkthruColumnName,
							sheetName:walkthruSheetName,
							dataTransferSet:dataTransferSet,
							eavAttribute:eavAttribute,
							validation:validation,
							isRequired: (isRequired.equalsIgnoreCase("X"))?1:0
						)
						if( dataTransferAttributeMap ){
							dataTransferAttributeMap.save()
						}
					}catch ( Exception ex ) {
						ex.printStackTrace()
					}
					//populate the EavEntityAttribute map associating each of the attributes to the set
					def eavAttributeSetId
					def eavAttributeSet
					try {
						eavAttributeSetId = 1
						eavAttributeSet = EavAttributeSet.findById( eavAttributeSetId )
						
						def eavEntityAttribute = new EavEntityAttribute(
							attribute:eavAttribute,
							eavAttributeSet:eavAttributeSet,
							sortOrder:sortOrder
						)
						if( eavEntityAttribute ){
							eavEntityAttribute.save()
						}
					}catch ( Exception ex ) {
						ex.printStackTrace()
					}
					/*
					 * After eavAttribute saved it will check for any options is there corresponding to current attribute
					 * If there then eavAttributeOptions.save() will be called corresponding to current attribute
					 */
					if(options != ""){
						String attributeOptions = options;
						String[] eavAttributeOptions = null
						eavAttributeOptions = attributeOptions.split(",");
						for( int attributeOption = 0; attributeOption < eavAttributeOptions.length; attributeOption++ ){
							def eavAttributeOption = new EavAttributeOption(
								attribute:eavAttribute,
								sortOrder:sortOrder,
								value:eavAttributeOptions[attributeOption].trim()
							)
							if( eavAttributeOption ){
								eavAttributeOption.save()
							}
						}
					}
				}
				workbook.close()
			}
		}
		catch( Exception ex ) {
			ex.printStackTrace()
		}
	}
	
	/*
	 * Used to check the sheet headers and return boolean value
	 */
	def checkHeader( def col, def map, def sheet ){
		for ( int c = 0; c < col; c++ ) {
			def cellContent = sheet.getCell( c, 0 ).contents
			if( map.containsKey( cellContent ) ) {
				map.put( cellContent,c )
			}
		}
		if( map.containsValue( null ) == true ) {
			return false
		} else {
			return true
		}
	}
	
	/*
	 * Method to assign Assets to Bundles 
	 */
	def saveAssetsToBundle( def bundleTo, def bundleFrom, def assets ){
		def moveBundleAssets
		
		// remove asstes from source bundle 
		if ( bundleTo ) {
			def moveBundleTo = MoveBundle.findById( bundleTo )
			// get Assets into list
			// def assetsList = assets.tokenize(',')
			def assetsList = getStringArray( assets )

			// assign assets to bundle
			assetsList.each{asset->
				if ( bundleFrom ) {
					def updateAssets = AssetEntity.executeUpdate("update AssetEntity set moveBundle = $bundleTo,project = $moveBundleTo.project.id, sourceTeamMt = null, targetTeamMt = null where moveBundle = $bundleFrom  and id = $asset")
				
				} else {
					/*def assetEntity = AssetEntity.findById( asset )
					def assetsExist = AssetEntity.findByMoveBundle( moveBundleTo )
					if ( !assetsExist ) {
					def moveBundleAsset = new AssetEntity( moveBundle:moveBundleTo, asset:assetEntity ).save()
					}*/
					def updateAssets = AssetEntity.executeUpdate("update AssetEntity set moveBundle = $bundleTo, sourceTeamMt = null, targetTeamMt = null where id = $asset")
				}
			}
			moveBundleAssets = AssetEntity.findAll("from AssetEntity where moveBundle = $bundleTo ")
		} else{
			def deleteAssets = AssetEntity.executeUpdate("update AssetEntity set moveBundle = null, sourceTeamMt = null, targetTeamMt = null where moveBundle = $bundleFrom and id in ($assets)")
		}
		return moveBundleAssets
	}
	
	// get StringArray from StringList
	// TODO : JPM - Why not just use String.split(",") ?
	def getStringArray(def stringList){
		def list = new ArrayList()
		def token = new StringTokenizer(stringList, ",")
		while (token.hasMoreTokens()) {
			list.add(token.nextToken())
		}
		return list
	}
	
	/*
	 * get Team - #Asset count corresponding to Bundle
	 */
	def getTeamAssetCount ( def bundleInstance, def rackPlan, def role ) {
		def teamAssetCounts = []
		//def bundleInstance = MoveBundle.findById(bundleId)
		def projectTeamInstanceList = ProjectTeam.findAll( "from ProjectTeam pt where pt.moveBundle = $bundleInstance.id and pt.role = '${role}' " )
		def assetEntityInstanceList = AssetEntity.findAllByMoveBundle( bundleInstance)
		if( rackPlan == 'RerackPlan') {
			projectTeamInstanceList.each{projectTeam ->
				def assetCount = assetEntityInstanceList.findAll{it[targetTeamType.get(role)]?.id == projectTeam.id}?.size()
				teamAssetCounts << [ teamCode: projectTeam.teamCode , assetCount:assetCount ]
			}
			def unAssignCount = assetEntityInstanceList.findAll{!it[targetTeamType.get(role)]?.id}?.size()
			teamAssetCounts << [ teamCode: "UnAssigned" , assetCount:unAssignCount ]
			
		} else {
			projectTeamInstanceList.each{projectTeam ->
				def assetCount = assetEntityInstanceList.findAll{it[sourceTeamType.get(role)]?.id == projectTeam.id}?.size()
				teamAssetCounts << [ teamCode: projectTeam.teamCode , assetCount:assetCount ]
			}
			def unAssignCount = assetEntityInstanceList.findAll{!it[sourceTeamType.get(role)]?.id}?.size()
			teamAssetCounts << [ teamCode: "UnAssigned" , assetCount:unAssignCount ]
		}
		return teamAssetCounts
	}


	//	get Cart - #Asset count corresponding to Bundle
	def getCartAssetCounts ( def bundleId ) {
		def cartAssetCounts = []
		def bundleInstance = MoveBundle.findById(bundleId)
		def cartList = AssetEntity.executeQuery(" select ma.cart from AssetEntity ma where ma.moveBundle = $bundleInstance.id  group by ma.cart")
		cartList.each { assetCart ->
			def cartAssetCount = AssetEntity.countByMoveBundleAndCart( bundleInstance, assetCart )
			def AssetEntityList = AssetEntity.findAllByMoveBundleAndCart(bundleInstance, assetCart)
			def usize = 0
			for(int AssetEntityRow = 0; AssetEntityRow < AssetEntityList.size(); AssetEntityRow++ ) {
				try {
					usize = usize + Integer.parseInt(AssetEntityList[AssetEntityRow]?.model?.usize? (AssetEntityList[AssetEntityRow]?.model?.usize).trim() : "0")
				} catch ( Exception e ) {
					println "uSize containing blank value."
				}
			}
			cartAssetCounts << [ cart:assetCart, cartAssetCount:cartAssetCount,usizeUsed:usize ]
		}
		return cartAssetCounts
	}
	
	//get assetsList  corresponding to selected bundle to update assetsList dynamically
	
	def getAssetList ( def assetEntityList, rackPlan, bundleInstance, role ) {
		def assetEntity = []
		def projectTeam =[]
		def projectTeamInstanceList = ProjectTeam.findAll( "from ProjectTeam pt where pt.moveBundle = $bundleInstance.id and pt.role = '${role}' " )
		projectTeamInstanceList.each{teams ->
			
			projectTeam << [ teamCode: teams.teamCode ]
		}
		for( int assetRow = 0; assetRow < assetEntityList.size(); assetRow++) {
			def displayTeam  
			if( rackPlan == "RerackPlan" ) {
				displayTeam = assetEntityList[assetRow][targetTeamType.get(role)]?.teamCode
			}else {
				displayTeam = assetEntityList[assetRow][sourceTeamType.get(role)]?.teamCode
			}
			def assetEntityInstance = AssetEntity.findById( assetEntityList[assetRow].id )
			assetEntity <<[id:assetEntityInstance.id, assetName:assetEntityInstance.assetName, model:assetEntityInstance?.model?.toString(), 
				sourceLocation:assetEntityInstance.sourceLocation, sourceRack:assetEntityInstance.sourceRack, 
				targetLocation:assetEntityInstance.targetLocation, targetRack:assetEntityInstance.targetRack, 
				sourcePosition:assetEntityInstance?.sourceRackPosition, targetPosition:assetEntityInstance?.targetRackPosition, 
				uSize:assetEntityInstance?.model?.usize, team:displayTeam, cart:assetEntityList[assetRow]?.cart, 
				shelf:assetEntityList[assetRow]?.shelf, projectTeam:projectTeam, assetTag:assetEntityInstance?.assetTag]
		}
		return assetEntity
	}
	/**
	 * To Validate the Import Process If any Errors update DataTransferBatch and DataTransferValue
	 * @author Srinivas
	 * @param DataTransferBatch - the record of the transfer batch
	 * @param AssetEntity - the asset to validate 
	 * @param Map of the property attributes from the import
	 * @return Map of [ flag, errorConflictCount] 
	 *     flag being true indicates that the asset was updated since the export was generated
	 *     errorConflictCount indicates the number of fields that have conflicts
	 */
	def importValidation( dataTransferBatch, asset, dtvList) {
		//Export Date Validation
		def errorConflictCount = 0

		def flag = asset.lastUpdated >= dataTransferBatch.exportDatetime

		if ( flag ) {
			log.debug "importValidation() Asset $asset was modified since the export"
			// If the asset has been modified, see how many of the fields are in conflict
			dtvList.each { dtValue->
				def attribName = dtValue.eavAttribute.attributeCode
				//validation for sourceTeamMt and targetTeamMt and MoveBundle and Backendtype int field
				if( bundleMoveAndClientTeams.contains(attribName) ) {
					def bundleInstance = asset.moveBundle 
					def teamInstance
					if (asset?."$attribName"?.teamCode != dtValue.correctedValue && asset?."$attribName"?.teamCode != dtValue.importValue ){
						updateChangeConflicts( dataTransferBatch, dtValue )
						errorConflictCount++
					}
				} else if ( attribName == "moveBundle" ) {
					if(asset?.moveBundle?.name != dtValue.correctedValue && asset?.moveBundle?.name != dtValue.importValue ){
						updateChangeConflicts( dataTransferBatch, dtValue )
						errorConflictCount++
					}
				} else if( attribName in ["usize", "modifiedBy", "lastUpdated"]){
					// skip the validation
				} else if( dtValue.eavAttribute.backendType == "int" ){
					def correctedPos
					try {
						if( dtValue.correctedValue ) {
							correctedPos = Integer.parseInt(dtValue.correctedValue.trim())
						} else if( dtValue.importValue ) {
							correctedPos = Integer.parseInt(dtValue.importValue.trim())
						}
						if( asset."$attribName" != correctedPos ){
							updateChangeConflicts( dataTransferBatch, dtValue )
							errorConflictCount++
						}
					} catch ( Exception ex ) {
						// TODO - JM 10/2013 - nothing? really? if the parseInt fails shouldn't it be an error?
					}
				} else {
					if(asset."$attribName" != dtValue.correctedValue && asset."$attribName" != dtValue.importValue ){
						updateChangeConflicts( dataTransferBatch, dtValue )
						errorConflictCount++
					}
					
				}
			}
		}
		
		return [flag : flag, errorConflictCount : errorConflictCount]
	}
	
	/*
	 * Update ChangeConficts if value is changed in spreadsheet
	 * @param dataTransferBatch, datatransfervalue
	 * @author srinivas
	 */
	def updateChangeConflicts( def dataTransferBatch, def dtValue) {
		if( dataTransferBatch.hasErrors == 0 ) {
			dataTransferBatch.hasErrors = 1
		}
		dtValue.hasError = 1
		dtValue.errorText = "change conflict"
		dtValue.save(flush:true)
		log.warn "Conflict in change: $dtValue"
	}
	
	/* To get DataTransferValue Asset MoveBundle
	 * @param dataTransferValue, projectInstance
	 * @author srinivas
	 */
	 def getdtvMoveBundle(def dtv, def projectInstance) {
		def moveBundleInstance = null
		if( dtv.correctedValue && dtv.importValue.toUpperCase().trim() != "NULL" ) {
			moveBundleInstance = createBundleIfNotExist(dtv.correctedValue, projectInstance)
		} else if(!dtv.importValue.isEmpty() && dtv.importValue.toUpperCase().trim() !="NULL") {
			moveBundleInstance = createBundleIfNotExist(dtv.importValue, projectInstance)
		} else if(dtv.importValue.isEmpty()) {
			moveBundleInstance = createBundleIfNotExist("TBD", projectInstance)
		}
		return moveBundleInstance
	}
	 
	/**
	 * 
	 * @param bundleName
	 * @param project
	 * @return
	 */
	def createBundleIfNotExist(String bundleName, Project project){
		def moveBundle = MoveBundle.findByNameAndProject( bundleName, project );
		if(!moveBundle){
			moveBundle = new MoveBundle( name:bundleName, project:project, operationalOrder:1, workflowCode: project.workflowCode )
			if(!moveBundle.save()){
				def etext = "Unable to create movebundle" + GormUtil.allErrorsString( moveBundle )
				log.error(etext)
			}
		}
		return moveBundle
	}
	
	/* To get DataTransferValue Asset Manufacturer
	 * @param dataTransferValue
	 * @author Lokanada Reddy
	 */
	def getdtvManufacturer( def manufacturerValue ) {
		def manufacturer
		if(manufacturerValue){
			manufacturer = Manufacturer.findByName( manufacturerValue )
			if( !manufacturer ){
				manufacturer = ManufacturerAlias.findByName(manufacturerValue)?.manufacturer
				if( !manufacturer ) {
					manufacturer = new Manufacturer( name : manufacturerValue )
					if ( !manufacturer.validate() || !manufacturer.save() ) {
						def etext = "Unable to create manufacturer" + GormUtil.allErrorsString( manufacturer )
						log.error(etext)
					}
				}
			}
		}
		return manufacturer
	}
	
	/* To get DataTransferValue Asset Model
	 * @param dataTransferValue, dataTransferValueList
	 * @author Lokanada Reddy
	 */
	def getdtvModel(def dtv, def dtvList, def assetEntity ) {
		def model
		def modelValue = dtv.correctedValue ? dtv.correctedValue : dtv.importValue
		def dtvManufacturer = dtvList.find{ it.eavAttribute.attributeCode == "manufacturer" }
		def dtvUsize = dtvList.find{it.eavAttribute.attributeCode == "usize"}?.importValue
		if(dtvManufacturer){
			def manufacturerName = dtvManufacturer?.correctedValue ? dtvManufacturer?.correctedValue : dtvManufacturer?.importValue
			model = findOrCreateModel(manufacturerName, modelValue, '', true, dtvUsize, dtvList);
		}
		return model
	}
	
	// TODO: Move to AssetEntityService
	/**
	 * Method used to find model by manufacturrName as well as create model if modelnot exist and manufacturer exist. 
	 * @param manufacturerName : name of manufacturer
	 * @param modelName : name of model
	 * @param type : asset's asset type
	 * @param create : a boolean flag to determine if model don't exist create model or not.
	 * @param usize : usize of model (default 1)
	 * @params dtvList : dataTransferValueList 
	 * @return model instance
	 */
	def findOrCreateModel(def manufacturerName, def modelName, def type, def doCreate=true, def usize=1, def dtvList = []){
		def model
		try{
		if( manufacturerName ){
			// first checking imported value in manufacturer table .
			def manufacturer = manufacturerName ? Manufacturer.findByName(manufacturerName) : null
			if( !manufacturer && manufacturerName){
				// if not found in manufacturer table then searching in manufacturer_alias table as it should exist per previous manufacturer creation
				// if found assign to manufacturerAlias.manufacturer.
				manufacturer = ManufacturerAlias.findByName( manufacturerName )?.manufacturer
			}
			if(manufacturer){
				// if modelValue exist using that else using 'unknown' as modelValue
				modelName = modelName?:'unknown'
				// if manufacturer searching in model table if found assigning .
				model = Model.findByModelNameAndManufacturer( modelName, manufacturer )
				if( !model ){
					// if imported value is not in model table then search in model alias table .
					model = ModelAlias.findByNameAndManufacturer(modelName,manufacturer)?.model
					if(!model && doCreate){
						def assetType
						if(type){
							assetType = type
						} else {
							def dtvAssetType = dtvList.find{it.eavAttribute.attributeCode == "assetType"}
							assetType = dtvAssetType?.correctedValue ? dtvAssetType?.correctedValue : dtvAssetType?.importValue
							assetType = assetType ? assetType : "Server"
						}
						model = Model.createModelByModelName(modelName, manufacturer, assetType, usize)
					}
				}
			}
		}
		} catch(Exception ex){
			ex.printStackTrace()
			log.error("Unexpected exception:" + ex.toString() )
		}
	  return model
	}
	
	/* To get DataTransferValue source/target Team
	 * @param dataTransferValue,moveBundle
	 * @author srinivas
	 */
	def getdtvTeam(def dtv, def bundleInstance, def role ){
		def teamInstance
		if( dtv.correctedValue && bundleInstance ) {
			teamInstance = projectTeam.findByTeamCodeAndMoveBundle(dtv.correctedValue, bundleInstance )
			if(!teamInstance && !teamInstance.find{it.role==role}){
				teamInstance = new ProjectTeam(teamCode:dtv.correctedValue, moveBundle:bundleInstance, role:role).save()
			}
		} else if( dtv.importValue && bundleInstance ) {
			teamInstance = ProjectTeam.findByTeamCodeAndMoveBundle(dtv.importValue, bundleInstance )
			if(!teamInstance && !teamInstance.find{it.role==role}){
				teamInstance = new ProjectTeam( name:dtv.importValue, teamCode:dtv.importValue, moveBundle:bundleInstance, role:role ).save()
			}
		}
		return teamInstance
	}

	// TODO: Move to AssetEntityService
	/*
	*  Create asset_cabled_Map for all asset model connectors 
	*/
	def createModelConnectors( assetEntity ){
		if(assetEntity.model){
			def assetConnectors = ModelConnector.findAllByModel( assetEntity.model )
			assetConnectors.each{
				def assetCableMap = new AssetCableMap(
					cable : "Cable"+it.connector,
					assetFrom: assetEntity,
					assetFromPort : it,
					cableStatus : it.status
				)
				if(assetEntity?.rackTarget && it.type == "Power" && it.label?.toLowerCase() == 'pwr1'){
					assetCableMap.assetTo = assetEntity
					assetCableMap.assetToPort = null
					assetCableMap.toPower = "A"
				}
				if ( !assetCableMap.validate() || !assetCableMap.save() ) {
					def etext = "Unable to create assetCableMap" +
					GormUtil.allErrorsString( assetCableMap )
					println etext
					log.error( etext )
				}
			}
		}
	}

	// TODO: Move to AssetEntityService
	/*
	 *  Create asset_cabled_Map for all asset model connectors 
	 */
	 def updateModelConnectors( assetEntity ){
		if(assetEntity.model){
			// Set to connectors to blank if associated 
			AssetCableMap.executeUpdate("""Update AssetCableMap set cableStatus='${AssetCableStatus.UNKNOWN}',assetTo=null,
				assetToPort=null where assetTo = ? """,[assetEntity])
			// Delete AssetCableMap for this asset
			AssetCableMap.executeUpdate("delete from AssetCableMap where assetFrom = ?",[assetEntity])
			// Create new connectors 
			def assetConnectors = ModelConnector.findAllByModel( assetEntity.model )
			assetConnectors.each{
				def assetCableMap = new AssetCableMap(
					cable : "Cable"+it.connector,
					assetFrom: assetEntity,
					assetFromPort : it,
					cableStatus : it.status
				)
				if(assetEntity?.rackTarget && it.type == "Power" && it.label?.toLowerCase() == 'pwr1'){
					assetCableMap.assetTo = assetEntity
					assetCableMap.assetToPort = null
					assetCableMap.toPower = "A"
				}
				if ( !assetCableMap.validate() || !assetCableMap.save() ) {
					def etext = "Unable to create assetCableMap" +
					GormUtil.allErrorsString( assetCableMap )
					println etext
					log.error( etext )
				}
			}
		}
	 }
	 
	 /**
	  * Storing imported asset type in EavAttributeOptions table if not exist .
	  * @param assetTypeName : assetTypeName is imported assetTypeName 
	  * @param create : create (Boolean) a flag to determine assetType will get created or not
	  * @return
	  */
	 def findOrCreateAssetType(assetTypeName, def create = false){
		 def typeAttribute = EavAttribute.findByAttributeCode("assetType")
		 def assetType = EavAttributeOption.findByValueAndAttribute(assetTypeName, typeAttribute)
		 if(!assetType && create){
			 def eavAttrOpt = new EavAttributeOption('value':assetTypeName, 'attribute':typeAttribute, 'sort':0)
			 if(!eavAttrOpt.save(flush:true)){
				 eavAttrOpt.errors.allErrors.each{
					 log.error(it)
				 }
			 }
		 }
		 return assetType
	 }
	 
	// TODO: Move to AssetEntityService
	/**
	 * This method is used to find a person object after importing and if not found create it
	 * @param importValue is value what is there in excel file consist firstName and LastName
	 * @param create : create is flag which will determine if person does not exist in db should they create record or not
	 * @return instance of person
	 */
	def findOrCreatePerson(importValue, def create = false){
		def project = securityService.getUserCurrentProject()
		def firstName
		def lastName
		if(importValue.contains(",")){
			def splittedName = importValue.split(",")
			firstName = splittedName[1].trim()
			lastName = splittedName[0].trim()
		} else if(StringUtils.containsAny(importValue, " ")){
			def splittedName = importValue.split("\\s+")
			firstName = splittedName[0].trim()
			lastName = splittedName[1].trim()
		} else {
			firstName = importValue.trim()
		}
		 
		//Serching Person in compnies staff list .
		def personList = partyRelationshipService.getCompanyStaff(project.client.id)
		def person = personList.find{it.firstName==firstName && it.lastName==lastName}
		if(!person && firstName && create){
			log.debug "Person $firstName $lastName does not found in selected company"
			person = new Person('firstName':firstName, 'lastName':lastName, 'staffType':'Contractor')
			if(!person.save(insert:true, flush:true)){
				def etext = "findOrCreatePerson Unable to create Person"+GormUtil.allErrorsString( person )
				log.error( etext )
			}
			def partyRelationshipType = PartyRelationshipType.findById( "STAFF" )
			 def roleTypeFrom = RoleType.findById( "COMPANY" )
			 def roleTypeTo = RoleType.findById( "STAFF" )
			 
			 def partyRelationship = new PartyRelationship( partyRelationshipType:partyRelationshipType,
				 partyIdFrom :project.client, roleTypeCodeFrom:roleTypeFrom, partyIdTo:person,
				 roleTypeCodeTo:roleTypeTo, statusCode:"ENABLED" )
			 .save( insert:true, flush:true )
		 }
		 
		return person
	}

	/**
	 * A helper closure used to set property to null or blank if the import value equals "NULL" and the property supports NULL or is a String. 
	 * In the case of being a String, if not blankable, then it sets the field to "NULL"
	 * @param The asset instance that is being updated
	 * @param The name of the property
	 * @param The import value
	 * @param The list of properties that support NULL
	 * @param The list of properties that support blank
	 * @return String message
	 * @usage setToNull().call(assetInstance, property, value)
	 * @references
	 *    - nullFProps
	 *    - blankFProps
	 *    
	 */
	def setToNullOrBlank(asset, propertyName, value, nullProps, blankProps ) {
		def msg = ''
		if (value == "NULL") {
			log.debug "setToNullOrBlank() for ${asset.getClass().getName()} $propertyName presently ${asset[propertyName]}"
			//If imported "NULL" and field allows blank and null updating value to null
			def type = GrailsClassUtils.getPropertyType(asset.getClass(), propertyName)?.getName()
			if ( nullProps.contains( propertyName ) ) {
				asset[propertyName] = null
			} else if ( type == "java.lang.String" ) {
				asset[propertyName] = blankProps.contains( propertyName ) ? '' : 'NULL'	
			} else {
				log.warn "setToNullOrBlank() Imported invalid value 'NULL' which is not allowed for $propertyName property."
				msg = "Unable to set $propertyName to NULL"
			}
		}
		return msg
	}
	/**
	 * A helper method used to do the initial lookup of an asset and perform the EAV attribute validation. If the asset was not found then it will
	 * create a new asset and initialize various properties. If the asset was modified since the export and import then it will return null
	 * @param The class to use (e.g. AssetEntity or Application)
	 * @param The asset id to lookup
	 * @return The asset that was looked up or a new one. If the asset exists and was modified since the import then it will return null
	 * @references
	 *   - errorCount
	 *   - errorConflictCount
	 *   - ignoredAssets
	 *   - dataTransferBatch
	 *   - dtvList
	 *   - project
	 */
	def findAndValidateAsset(clazz, assetId, project, dataTransferBatch, dtvList, eavAttributeSet, errorCount, errorConflictCount, ignoredAssets) { 
		
		// Try loading the application and make sure it is associated to the current project
		def asset 
		def clazzName = clazz.getName().tokenize('.')[-1]
		def clazzMap = ['AssetEntity':'Server', 'Database':'Database', 'Application':'Application', 'Files':'Files']

		if (assetId) {
			asset = clazz.get(assetId)
			if (asset) {
				log.debug "findAndValidateAsset() Found $clazzName id $assetId"
				if ( asset.project.id == project.id ) {
					if ( dataTransferBatch?.dataTransferSet.id == 1 ) {
						// Validate that the AE fields are valid
						def validateResultList = importValidation( dataTransferBatch, asset, dtvList )
						if ( validateResultList.flag ) {
							// The asset has been updated since the last export so we don't want to overwrite any possible changes
							errorCount++
							errorConflictCount += validateResultList.errorConflictCount
							ignoredAssets << asset
							log.warn "findAndValidateAsset() Field validation error for $clazzName (id:${asset.id}, assetName:${asset.assetName})"
							asset = false
						}
					}
				} else {
					log.warn "findAndValidateAsset() Attempted it import $clazzName (id ${asset.id}) into unassociated project"
					asset = null
				}
			}
		}

		if (asset == null) {
			asset = clazz.newInstance()
			asset.project = project
			asset.owner = project.client
			asset.attributeSet = eavAttributeSet
			asset.assetType = clazzMap[clazzName]

			log.debug "findAndValidateAsset() Created $clazzName"
		}

		// If there were conflicts above, set the object to null
		if ( asset.is(false) )
			return null

		return asset
		
	} 

	/**
	 * Used by the import process to save the assets and update various vars used to track status
	 * @param
	 * @return
	 */
	def saveAssetChanges(asset, assetList, rowNum, insertCount, updateCount, errorCount, warnings) {
		def saved = false
		if ( asset.id ) {
			if ( asset.dirtyPropertyNames.size() ) {
				// Check to see if dirty
				log.info "saveAssetChanges() Updated asset ${asset.id} ${asset.assetName} - Dirty properties: ${asset.dirtyPropertyNames}"
				saved = asset.validate() && asset.save()
				if (saved) {
					updateCount++
					assetList << asset.id
				}
			} else {
				saved = true 	// Mark as saved even though it wasn't changed
			}
		} else {
			// Handle a new asset 
			saved = asset.validate() && asset.save()
			if (saved) {
				insertCount++
				assetList << asset.id // Once asset saved to DB it will provide ID for that.
				log.debug "saveAssetChanges() saved new asset id:$asset.id, insertCount:$insertCount"
			}
		}
		if (! saved) {
			log.warn "appProcess() Performing discard for rowNum $rowNum. " + GormUtil.allErrorsString(asset)
			warnings << "Asset ${asset.assetName}, row $rowNum had an error and was not updated. " + GormUtil.errorsAsUL(asset)
			asset.discard()
			errorCount++
		}

		return [insertCount, updateCount, errorCount]
	}

	/** 
	 * Used by the import process to update status and clear hibernate session
	 * @param
	 * @return void
	 */
	def updateStatusAndClear(project, dataTransferValueRow, sessionFactory, session, clearEvery=100) {
		if (dataTransferValueRow % clearEvery == (clearEvery - 1)) {
			sessionFactory.getCurrentSession().flush()
			sessionFactory.getCurrentSession().clear()
			 
			// Merging back the project to current session, 
			// As it is being detach after flushing and clearing hibernate session 

			project = project.merge()
		}

		session.setAttribute("TOTAL_PROCESSES_ASSETS",dataTransferValueRow)
	}

	/**
	 * Used by the asset import process to set a value on an asset property or a default if it wasn't already set
	 * @param Object the asset to update
	 * @param String the name of the property to update
	 * @param Object the value to set on the property
	 * @param Object the default value (optional)
	 * @return void
	 */
	def setValueOrDefault(asset, property, value, defValue = null) {
		if ( (value?.size() && value != asset[property]) || ! asset[property])
			if (value)
				asset[property] = value
			else if (defValue)
				asset[property] = defValue
	}

	/**
	 * Used by the asset import process to set the various properties that a common across the various asset classes
	 * @param Project - the project that is being processed
	 * @param Object the asset that is being updated 
	 * @param Map the DataTransferValue to update
	 * @param Integer the row number being processed
	 * @param List the list of warning messages
	 * @param Integer the counter for error conflicts
	 * @return void
	 */	 
	def setCommonProperties(project, asset, dtv, rowNum, warnings, errorConflictCount) {
		// def handled = true
		def property = dtv.eavAttribute.attributeCode
		def value = dtv.importValue
		def newVal
		def clazz = asset.getClass().getName().tokenize('.')[-1]

		if (value || ['assetName','assetTag'].contains(property))
			log.debug "setCommonProperties() attempting to update ${clazz}.${property} with [$value] on row $rowNum"

		switch (property) {
			case ~/assetTag|assetName/:
				// This is a special case when the clazz is AssetEntity as we construct the assetName & assetTag if not presented
				if (clazz == 'AssetEntity') {
					if (! asset[property] && ! value ) {
						newVal = projectService.getNewAssetTag(project, asset)
					} else {
						newVal = value ?: null						
					}
				} else {
					newVal = value ?: null						
				}
				if (newVal)
					asset[property] = newVal
				break
			case "moveBundle":
				if(!asset.id || dtv.importValue){
					def moveBundle = getdtvMoveBundle(dtv, project)
						asset[property] = moveBundle
				}
				break
			case ~/maintExpDate|retireDate/:
				if (value) {
					newVal = DateUtil.parseDate(value)
					if (! newVal) {
						warnings << "Unable to set date (${value}) for $property on row $rowNum" + 
							(asset.assetName ? ", asset '${asset.assetName}'" : '') +
							', proper format mm-dd-yyyy'
						errorConflictCount++
					} else if (asset[property] != newVal ) {
						asset[property] = newVal
					}
				}
				break
			case "owner":
				// TODO : JPM 10/13 - what the heck is this doing?  - This in the spreadsheet refers to the AppOwner?
				// asset[property] = asset.owner
				break
			case "planStatus":
				setValueOrDefault(asset, property, value, 'Unassigned')
				break
			case ~/rateOfChange|size/:
				newVal = NumberUtils.toDouble(value, 0).round()
				if (asset[property] != newVal) {
					asset[property] = newVal
				}
				break
			case 'scale':
				newVal = SizeScale.asEnum( value )
				if ( value.size() && !newVal ) {
					// Value wrong
					warnings << "Invalid value ($value) for Scale on row $rowNum, valid values ${SizeScale.getKeys()}"
					errorConflictCount++
				} else if ( newVal != asset[property]) {
					asset[property] = newVal
				}
				break
			case "validation":
				setValueOrDefault(asset, property, value, 'Discovery')
				break
			case ~/modifiedBy|lastUpdated/: 
				break
			default:
				if (value.size() ) {
					if ( dtv.eavAttribute.backendType == "int" ) {
						def correctedPos
						try {
							if( dtv.correctedValue ) {
								correctedPos = NumberUtils.toDouble(dtv.correctedValue.trim(), 0).round()
							} else if( dtv.importValue ) {
								correctedPos = NumberUtils.toDouble(value, 0).round()
							}
							//correctedPos = dtv.correctedValue
							if ( asset[property] != correctedPos || ! asset.id ) {
								asset[property] = correctedPos 
							}
						} catch ( Exception ex ) {
							log.error "setCommonProperties() exception 1 : ${ex.getMessage()}"
							ex.printStackTrace()
							warnings << "Unable to update $property with value [$value] of ${asset.assetName} from row $rowNum"
							errorConflictCount++
							dtv.hasError = 1
							dtv.errorText = "format error"
							dtv.save()
						}
					} else {
						try {
							asset[property] = dtv.correctedValue ?: dtv.importValue
						} catch ( Exception ex ) {
							log.error "setCommonProperties() exception 2 : ${ex.getMessage()}"
							ex.printStackTrace()
							warnings << "Unable to update $property with value [$value] of ${asset.assetName} from row $rowNum"
							errorConflictCount++
							dtv.hasError = 1
							dtv.errorText = ex.getMessage()
							dtv.save()
						}
					}
				}
		}
		
	}
}