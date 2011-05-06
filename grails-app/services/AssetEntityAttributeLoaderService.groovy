import com.tdssrc.eav.EavAttribute
import com.tdssrc.eav.EavEntityAttribute
import com.tdssrc.eav.EavAttributeOption
import com.tdssrc.eav.EavEntityType
import com.tdssrc.eav.EavAttributeSet
import java.io.*
import jxl.*
import jxl.write.*
import jxl.read.biff.*

class AssetEntityAttributeLoaderService {

	boolean transactional = true
	def eavAttribute

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
					def updateAssets = AssetEntity.executeUpdate("update AssetEntity set moveBundle = $bundleTo,project = $moveBundleTo.project.id, sourceTeam = null, targetTeam = null where moveBundle = $bundleFrom  and id = $asset")
				
				} else {
					/*def assetEntity = AssetEntity.findById( asset )
					def assetsExist = AssetEntity.findByMoveBundle( moveBundleTo )
					if ( !assetsExist ) {
					def moveBundleAsset = new AssetEntity( moveBundle:moveBundleTo, asset:assetEntity ).save()
					}*/
					def updateAssets = AssetEntity.executeUpdate("update AssetEntity set moveBundle = $bundleTo, sourceTeam = null, targetTeam = null where id = $asset")
				}
			}
			moveBundleAssets = AssetEntity.findAll("from AssetEntity where moveBundle = $bundleTo ")
		} else{
			def deleteAssets = AssetEntity.executeUpdate("update AssetEntity set moveBundle = null, sourceTeam = null, targetTeam = null where moveBundle = $bundleFrom and id in ($assets)")
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
	def getTeamAssetCount ( def bundleInstance, def rackPlan ) {
		def teamAssetCounts = []
		//def bundleInstance = MoveBundle.findById(bundleId)
		def projectTeamInstanceList = ProjectTeam.findAll( "from ProjectTeam pt where pt.moveBundle = $bundleInstance.id and pt.teamCode != 'Logistics' and pt.teamCode != 'Transport' " )
    	if( rackPlan == 'RerackPlan') {
    		projectTeamInstanceList.each{projectTeam ->
    			def assetCount = AssetEntity.countByMoveBundleAndTargetTeam( bundleInstance, projectTeam )
    			teamAssetCounts << [ teamCode: projectTeam.teamCode , assetCount:assetCount ]
    		}
    		def unAssignCount = AssetEntity.countByMoveBundleAndTargetTeam( bundleInstance, null )
    		teamAssetCounts << [ teamCode: "UnAssigned" , assetCount:unAssignCount ]
    		
    	} else {
    		projectTeamInstanceList.each{projectTeam ->
				def assetCount = AssetEntity.countByMoveBundleAndSourceTeam( bundleInstance, projectTeam )
				teamAssetCounts << [ teamCode: projectTeam.teamCode , assetCount:assetCount ]
    		}
    		def unAssignCount = AssetEntity.countByMoveBundleAndSourceTeam( bundleInstance, null )
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
	
	def getAssetList ( def assetEntityList, rackPlan, bundleInstance ) {
		def assetEntity = []
		def projectTeam =[]
		def projectTeamInstanceList = ProjectTeam.findAll( "from ProjectTeam pt where pt.moveBundle = $bundleInstance.id and pt.teamCode != 'Logistics' and pt.teamCode != 'Transport' " )
		projectTeamInstanceList.each{teams ->
			
			projectTeam << [ teamCode: teams.teamCode ]
		}
		for( int assetRow = 0; assetRow < assetEntityList.size(); assetRow++) {
    		def displayTeam  
    		if( rackPlan == "RerackPlan" ) {
    			displayTeam = assetEntityList[assetRow]?.targetTeam?.teamCode
    		}else {
    			displayTeam = assetEntityList[assetRow]?.sourceTeam?.teamCode
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
	/*----------------------------------------------------
	 * To Validate the Import Process If any Errors update DataTransferBatch and DataTransferValue
	 * @author Srinivas
	 * @param DataTransferBatch, AssetEntity
	 * @return flag
	 *----------------------------------------------------*/
    def importValidation( def dataTransferBatch, def assetEntity, def  dtvList,def projectInstance) {
		//Export Date Validation
		def errorConflictCount = 0
		def flag = 0
		def validateResultList = []
		if( assetEntity.lastUpdated >= dataTransferBatch.exportDatetime ) {
			flag = 1
			dtvList.each { dtValue->
				def attribName = dtValue.eavAttribute.attributeCode
				//validation for sourceTeam and targetTeam and MoveBundle and Backendtype int field
				if( attribName == "sourceTeam" || attribName == "targetTeam" ) {
					def bundleInstance = assetEntity.moveBundle 
    				def teamInstance
					if(assetEntity?."$attribName"?.teamCode != dtValue.correctedValue && assetEntity?."$attribName"?.teamCode != dtValue.importValue ){
						updateChangeConflicts( dataTransferBatch, dtValue )
						errorConflictCount+=1
					}
				}else if ( attribName == "moveBundle" ) {
					if(assetEntity?.moveBundle?.name != dtValue.correctedValue && assetEntity?.moveBundle?.name != dtValue.importValue ){
						updateChangeConflicts( dataTransferBatch, dtValue )
						errorConflictCount+=1
					}
				}else if( dtValue.eavAttribute.backendType == "int" ){
					def correctedPos
					try {
						if( dtValue.correctedValue ) {
							correctedPos = Integer.parseInt(dtValue.correctedValue.trim())
						} else if( dtValue.importValue ) {
							correctedPos = Integer.parseInt(dtValue.importValue.trim())
						}
						if( assetEntity."$attribName" != correctedPos ){
							updateChangeConflicts( dataTransferBatch, dtValue )
							errorConflictCount+=1
						}
					} catch ( Exception ex ) {
					}
				} else {
					if(assetEntity."$attribName" != dtValue.correctedValue && assetEntity."$attribName" != dtValue.importValue ){
						updateChangeConflicts( dataTransferBatch, dtValue )
						errorConflictCount+=1
					}
					
				}
			}
		}
		validateResultList << [flag : flag, errorConflictCount : errorConflictCount]
		return validateResultList
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
	}
	
	/* To get DataTransferValue Asset MoveBundle
	 * @param dataTransferValue, projectInstance
	 * @author srinivas
	 */
	 def getdtvMoveBundle(def dtv, def projectInstance ) {
		def moveBundleInstance
		 if( dtv.correctedValue ) {
				moveBundleInstance = MoveBundle.findByNameAndProject( dtv.correctedValue, projectInstance )
				if( !moveBundleInstance ) {
					moveBundleInstance = new MoveBundle( name:dtv.correctedValue, project:projectInstance, operationalOrder:1 ).save()
				}
			} else if( dtv.importValue ) {
				moveBundleInstance = MoveBundle.findByNameAndProject( dtv.importValue, projectInstance )
				if( !moveBundleInstance ) {
					moveBundleInstance = new MoveBundle( name:dtv.importValue, project:projectInstance, operationalOrder:1 ).save()
				}
			}
		return moveBundleInstance
	}
	/* To get DataTransferValue Asset Manufacturer
	 * @param dataTransferValue
	 * @author Lokanada Reddy
	 */
	def getdtvManufacturer( def dtv ) {
		def manufacturerInstance
		def manufacturerValue = dtv.correctedValue ? dtv.correctedValue : dtv.importValue
		if(manufacturerValue){
			manufacturerInstance = Manufacturer.findByName( manufacturerValue )
			if( !manufacturerInstance ){
				def manufacuturers = Manufacturer.findAllByAkaIsNotNull()
				manufacuturers.each{manufacuturer->
					if(manufacuturer.aka.toLowerCase().contains( manufacturerValue.toLowerCase() )){
						manufacturerInstance = manufacuturer
					}
				}
				if(!manufacturerInstance){
					manufacturerInstance = new Manufacturer( name : manufacturerValue )
					if ( !manufacturerInstance.validate() || !manufacturerInstance.save() ) {
						def etext = "Unable to create manufacturerInstance" +
		                GormUtil.allErrorsString( manufacturerInstance )
						println etext
					}
				}
			}
		}
		return manufacturerInstance
	}
	/* To get DataTransferValue Asset Model
	 * @param dataTransferValue, dataTransferValueList
	 * @author Lokanada Reddy
	 */
	def getdtvModel(def dtv, def dtvList, def assetEntity ) {
		def modelInstance
		def modelValue = dtv.correctedValue ? dtv.correctedValue : dtv.importValue
		try{
		//assetEntity.assetType = assetEntity.assetType ? assetEntity.assetType : "Server"
		def dtvManufacturer = dtvList.find{it.eavAttribute.attributeCode == "manufacturer"}
		if(modelValue && dtvManufacturer){
			def manufacturerName = dtvManufacturer?.correctedValue ? dtvManufacturer?.correctedValue : dtvManufacturer?.importValue
			def manufacturerInstance = manufacturerName ? Manufacturer.findByName(manufacturerName) : null
			if( !manufacturerInstance ){
				def manufacuturers = Manufacturer.findAllByAkaIsNotNull()
				manufacuturers.each{manufacuturer->
					if(manufacuturer.aka.toLowerCase().contains( manufacturerName.toLowerCase() )){
						manufacturerInstance = manufacuturer
					}
				}
			}
			if(manufacturerInstance){
				modelInstance = Model.findByModelNameAndManufacturer( modelValue, manufacturerInstance )
				//modelInstance = modelInstance?.find{it.assetType == assetEntity?.assetType}
				if( !modelInstance ){
					def models = Model.findAllByManufacturerAndAkaIsNotNull( manufacturerInstance )//.findAll{it.assetType == assetEntity?.assetType}
					models.each{ model->
						if(model.aka.toLowerCase().contains( modelValue.toLowerCase() )){
							modelInstance = model
						}
					}
					if(!modelInstance){
						def dtvAssetType = dtvList.find{it.eavAttribute.attributeCode == "assetType"}
						dtvAssetType = dtvAssetType ? dtvAssetType : "Server"
						modelInstance = new Model( modelName:modelValue, manufacturer:manufacturerInstance, assetType:dtvAssetType, sourceTDS : 0 )
						if ( !modelInstance.validate() || !modelInstance.save() ) {
							def etext = "Unable to create modelInstance" +
			                GormUtil.allErrorsString( modelInstance )
							println etext
						}
					}
				}
			}
		}
		} catch(Exception ex){
			ex.printStackTrace()
		}
		return modelInstance
	}
	/* To get DataTransferValue source/target Team
	 * @param dataTransferValue,moveBundle
	 * @author srinivas
	 */
	def getdtvTeam(def dtv, def bundleInstance ){
		def teamInstance
		if( dtv.correctedValue && bundleInstance ) {
			teamInstance = projectTeam.findByTeamCodeAndMoveBundle(dtv.correctedValue,bundleInstance)
			if(!teamInstance){
				teamInstance = new ProjectTeam(teamCode:dtv.correctedValue,moveBundle:bundleInstance).save()
			}
		} else if( dtv.importValue && bundleInstance ) {
			teamInstance = ProjectTeam.findByTeamCodeAndMoveBundle(dtv.importValue,bundleInstance)
			if(!teamInstance){
				teamInstance = new ProjectTeam( name:dtv.importValue, teamCode:dtv.importValue,moveBundle:bundleInstance ).save()
			}
		}
		return teamInstance
	}
	/*
    *  Create asset_cabled_Map for all asset model connectors 
    */
    def createModelConnectors( assetEntity ){
    	if(assetEntity.model){
	    	def assetConnectors = ModelConnector.findAllByModel( assetEntity.model )
			assetConnectors.each{
				def assetCableMap = new AssetCableMap(
													cable : "Cable"+it.connector,
													fromAsset: assetEntity,
													fromConnectorNumber : it,
													status : it.status
													)
				if ( !assetCableMap.validate() || !assetCableMap.save() ) {
					def etext = "Unable to create assetCableMap" +
	                GormUtil.allErrorsString( assetCableMap )
					println etext
					log.error( etext )
				}
			}
	    }
    }
    /*
     *  Create asset_cabled_Map for all asset model connectors 
     */
     def updateModelConnectors( assetEntity ){
     	if(assetEntity.model){
     		// Set to connectors to blank if associated 
     		AssetCableMap.executeUpdate("""Update AssetCableMap set status='missing',toAsset=null,
					toConnectorNumber=null,toAssetRack=null,toAssetUposition=null
					where toAsset = ? """,[assetEntity])
			// Delete AssetCableMap for this asset
			AssetCableMap.executeUpdate("delete from AssetCableMap where fromAsset = ?",[assetEntity])
 	    	// Create new connectors 
			def assetConnectors = ModelConnector.findAllByModel( assetEntity.model )
 			assetConnectors.each{
 				def assetCableMap = new AssetCableMap(
 													cable : "Cable"+it.connector,
 													fromAsset: assetEntity,
 													fromConnectorNumber : it,
 													status : it.status
 													)
 				if ( !assetCableMap.validate() || !assetCableMap.save() ) {
 					def etext = "Unable to create assetCableMap" +
 	                GormUtil.allErrorsString( assetCableMap )
 					println etext
 					log.error( etext )
 				}
 			}
 	    }
     }
}
