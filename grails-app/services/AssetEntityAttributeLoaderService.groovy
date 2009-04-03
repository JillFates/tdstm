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
        def map = [ "Attribute Code":null, "Label":null, "Type":null, "sortOrder":null, "Note":null, "Input type":null, "Required":null, "Unique":null, "Business Rules (hard/soft errors)":null, "Spreadsheet Sheet Name":null, "Spreadsheet Column Name":null, "Options":null ]
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
            		def columnName = sheet.getCell( map["Spreadsheet Column Name"], r ).contents
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
            			def dataTransferSetId 
		                def dataTransferSet
		                try {
		                	dataTransferSetId = 1
		                	dataTransferSet = DataTransferSet.findById( dataTransferSetId )
		                	def dataTransferAttributeMap = new DataTransferAttributeMap(
		                		columnName:columnName,
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
					
					def updateAssets = MoveBundleAsset.executeUpdate("update MoveBundleAsset set moveBundle = $bundleTo where moveBundle = $bundleFrom and asset = $asset ")
				
				} else {
					def assetEntity = AssetEntity.findById( asset )
					def assetsExist = MoveBundleAsset.findByMoveBundleAndAsset( moveBundleTo, assetEntity )
					if ( !assetsExist ) {
						def moveBundleAsset = new MoveBundleAsset( moveBundle:moveBundleTo, asset:assetEntity ).save()
					}
				}
			}
			moveBundleAssets = MoveBundleAsset.findAll("from MoveBundleAsset where moveBundle = $bundleTo ")
		} else{
			def deleteAssets = MoveBundleAsset.executeUpdate("delete from MoveBundleAsset where moveBundle = $bundleFrom and asset in ($assets)")
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
    			def assetCount = MoveBundleAsset.countByMoveBundleAndTargetTeam( bundleInstance, projectTeam )
    			teamAssetCounts << [ teamCode: projectTeam.teamCode , assetCount:assetCount ]
    		}
    	} else {
    		projectTeamInstanceList.each{projectTeam ->
				def assetCount = MoveBundleAsset.countByMoveBundleAndSourceTeam( bundleInstance, projectTeam )
				teamAssetCounts << [ teamCode: projectTeam.teamCode , assetCount:assetCount ]
    		}
    	}
		return teamAssetCounts
	}
	
	//get assetsList  corresponding to selected bundle to update assetsList dynamically
	
	def getAssetList ( def moveBundleAssetList, rackPlan ) {
		def moveBundleAsset = []
		for( int assetRow = 0; assetRow < moveBundleAssetList.size(); assetRow++) {
    		def displayTeam  
    		if( rackPlan == "RerackPlan" ) {
    			displayTeam = moveBundleAssetList[assetRow]?.targetTeam?.id
    		}else {
    			displayTeam = moveBundleAssetList[assetRow]?.sourceTeam?.id
    		}
    		def assetEntityInstance = AssetEntity.findById( moveBundleAssetList[assetRow].asset.id )
    		moveBundleAsset <<[id:assetEntityInstance.id, serverName:assetEntityInstance.serverName, model:assetEntityInstance.model, sourceLocation:assetEntityInstance.sourceLocation, sourceRack:assetEntityInstance.sourceRack, targetLocation:assetEntityInstance.targetLocation, targetRack:assetEntityInstance.targetRack, sourcePosition:assetEntityInstance?.sourceRackPosition, targetPosition:assetEntityInstance?.targetRackPosition, uSize:assetEntityInstance.usize, team:displayTeam, cart:moveBundleAssetList[assetRow]?.cart, shelf:moveBundleAssetList[assetRow]?.shelf ]
    	}
		return moveBundleAsset
	}
}
