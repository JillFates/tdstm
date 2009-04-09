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
	//upload records in to EavAttribute table from from AssetEntity.xls
	def uploadEavAttribute = {def stream ->
		//get Entity TYpe
		def entityType = EavEntityType.findByEntityTypeCode( "AssetEntity" )
        // create workbook
        def workbook
        def sheet
        def sheetNo = 0
        def map = [ "Attribute Code":null, "Label":null, "Type":null, "sortOrder":null, "Note":null, "Input type":null, "Required":null, "Unique":null, "Business Rules (hard/soft errors)":null, "Spreadsheet Sheet Name":null, "Spreadsheet Column Name":null, "Options":null, "Walkthru Sheet Name":null, "Walkthru Column Name":null ]
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
            }else{
            	for ( int r = 1; r < sheet.rows; r++ ) {
            		// get fields
            		def applicationCode = sheet.getCell( map["Attribute Code"], r ).contents
            		def backEndType = sheet.getCell( map["Type"], r ).contents                    
            		def frontEndInput = sheet.getCell( map["Input type"], r ).contents
            		def fronEndLabel = sheet.getCell( map["Label"], r ).contents
            		def isRequired = sheet.getCell( map["Required"], r ).contents
            		def isUnique = sheet.getCell( map["Unique"], r ).contents
            		def note = sheet.getCell( map["Note"], r ).contents
            		def sortOrder = sheet.getCell( map["sortOrder"], r ).contents
            		def validation = sheet.getCell( map["Business Rules (hard/soft errors)"], r ).contents
            		def options = sheet.getCell( map["Options"], r ).contents
            		def spreadSheetName = sheet.getCell( map["Spreadsheet Sheet Name"], r ).contents
            		def spreadColumnName = sheet.getCell( map["Spreadsheet Column Name"], r ).contents
            		def walkthruSheetName = sheet.getCell( map["Walkthru Sheet Name"], r ).contents
            		def walkthruColumnName = sheet.getCell( map["Walkthru Column Name"], r ).contents
            		// save data in to db(eavAttribute) 
            		eavAttribute = new EavAttribute( attributeCode:applicationCode,
                        note: note,
                        backendType: "varchar",
                        frontendInput: frontEndInput,
                        entityType: entityType,
                        frontendLabel: fronEndLabel,
                        defaultValue: "null",
                        validation: validation,
                        isRequired: (isRequired.equalsIgnoreCase("X"))?1:0,
                        isUnique: (isUnique.equalsIgnoreCase("X"))?1:0,
                        sortOrder: sortOrder )
            		if ( eavAttribute && eavAttribute.save() ) {
            			
            			//create DataTransferAttributeMap records related to the DataTransferSet 
            			//def dataTransferSetId 
		                def dataTransferSet
		                try {
		                	dataTransferSet = DataTransferSet.findByTitle( "TDS Master Spreadsheet" )
		                	def dataTransferAttributeMap = new DataTransferAttributeMap(
		                		columnName:spreadColumnName,
		                		sheetName:spreadSheetName,
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
            	}
            	workbook.close()
            }
        }
        catch( Exception ex ) {
        	ex.printStackTrace()
        } 
	}
    //  check the sheet headers and return boolean value
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
			def assetsList = getStringArray( assets )
			// assign assets to bundle
			assetsList.each{asset->
				if ( bundleFrom ) {
					def updateAssets = AssetEntity.executeUpdate("update AssetEntity set moveBundle = $bundleTo where moveBundle = $bundleFrom  and id = $asset")
				
				} else {
					/*def assetEntity = AssetEntity.findById( asset )
					def assetsExist = AssetEntity.findByMoveBundle( moveBundleTo )
					if ( !assetsExist ) {
						def moveBundleAsset = new AssetEntity( moveBundle:moveBundleTo, asset:assetEntity ).save()
					}*/
					def updateAssets = AssetEntity.executeUpdate("update AssetEntity set moveBundle = $bundleTo where id = $asset")
				}
			}
			moveBundleAssets = AssetEntity.findAll("from AssetEntity where moveBundle = $bundleTo ")
		} else{
			def deleteAssets = AssetEntity.executeUpdate("update AssetEntity set moveBundle = null where moveBundle = $bundleFrom and id in ($assets)")
		}
		return moveBundleAssets
	}
	
	// get StringArray from StringList 
	def getStringArray(def stringList){
		def list = new ArrayList()
		def token = new StringTokenizer(stringList, ",")
		while (token.hasMoreTokens()) {
			//println"list=============>"+token.nextToken()
			list.add(token.nextToken())
     	}
		return list
	}
	
	//get Team - #Asset count corresponding to Bundle
	def getTeamAssetCount ( def bundleId, def rackPlan ) {
		def teamAssetCounts = []
		def bundleInstance = MoveBundle.findById(bundleId)
		def projectTeamInstanceList = ProjectTeam.findAllByMoveBundle( bundleInstance )
    	if( rackPlan == 'RerackPlan') {
    		projectTeamInstanceList.each{projectTeam ->
    			def assetCount = AssetEntity.countByMoveBundleAndTargetTeam( bundleInstance, projectTeam )
    			teamAssetCounts << [ teamCode: projectTeam.teamCode , assetCount:assetCount ]
    		}
    	} else {
    		projectTeamInstanceList.each{projectTeam ->
				def assetCount = AssetEntity.countByMoveBundleAndSourceTeam( bundleInstance, projectTeam )
				teamAssetCounts << [ teamCode: projectTeam.teamCode , assetCount:assetCount ]
    		}
    	}
		return teamAssetCounts
	}
	//	get Cart - #Asset count corresponding to Bundle
	def getCartAssetCounts ( def bundleId, def cartList ) {
		def cartAssetCounts = []
		def bundleInstance = MoveBundle.findById(bundleId)
		cartList.each { assetCart ->
			def cartAssetCount = AssetEntity.countByMoveBundleAndCart( bundleInstance, assetCart.cart )
			def AssetEntityList = AssetEntity.findAllByMoveBundleAndCart(bundleInstance, assetCart.cart)
			def usize = 0
			for(int AssetEntityRow = 0; AssetEntityRow < AssetEntityList.size(); AssetEntityRow++ ) {
				usize = usize + Integer.parseInt(AssetEntityList[AssetEntityRow].usize ? AssetEntityList[AssetEntityRow].usize : 0)
			}
			cartAssetCounts << [ cart:assetCart.cart, cartAssetCount:cartAssetCount,usizeUsed:usize ]
		}
		return cartAssetCounts
	}
	
	//get assetsList  corresponding to selected bundle to update assetsList dynamically
	
	def getAssetList ( def assetEntityList, rackPlan, bundleInstance ) {
		def assetEntity = []
		def projectTeam =[]
		def projectTeamInstanceList = ProjectTeam.findAllByMoveBundle( bundleInstance )
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
    		assetEntity <<[id:assetEntityInstance.id, assetName:assetEntityInstance.assetName, model:assetEntityInstance.model, sourceLocation:assetEntityInstance.sourceLocation, sourceRack:assetEntityInstance.sourceRack, targetLocation:assetEntityInstance.targetLocation, targetRack:assetEntityInstance.targetRack, sourcePosition:assetEntityInstance?.sourceRackPosition, targetPosition:assetEntityInstance?.targetRackPosition, uSize:assetEntityInstance.usize, team:displayTeam, cart:assetEntityList[assetRow]?.cart, shelf:assetEntityList[assetRow]?.shelf, projectTeam:projectTeam ]
    	}
		return assetEntity
	}
}
