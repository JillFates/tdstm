import java.io.*
import jxl.*
import jxl.write.*
import jxl.read.biff.*
import org.springframework.web.multipart.*
import org.springframework.web.multipart.commons.*
import grails.converters.JSON
import org.jsecurity.SecurityUtils
import com.tdssrc.eav.*
class AssetEntityController {
    
    //TODO : Fix indentation
	def missingHeader = ""
	def added = 0
	def skipped = []
    def index = {
		redirect( action:list, params:params )
	}

    //upload , export
    /*
     * render import export form
     */
    def assetImport = {
        //get id of selected project from project view
        def projectId = params.projectId
        def assetsByProject
        def projectInstance
        def moveBundleInstanceList
        if( projectId != null ) {
            projectInstance = Project.findById( projectId )
            moveBundleInstanceList = MoveBundle.findAllByProject( projectInstance )
        }
        def dataTransferSetImport = DataTransferSet.findAll(" from DataTransferSet dts where dts.transferMode IN ('B','I') ")
        def dataTransferSetExport = DataTransferSet.findAll(" from DataTransferSet dts where dts.transferMode IN ('B','E') ")
        
        if( projectId == null ) {
            //get project id from session
            def currProj = getSession().getAttribute( "CURR_PROJ" )
            projectId = currProj.CURR_PROJ
            projectInstance = Project.findById( projectId )
            moveBundleInstanceList = MoveBundle.findAllByProject( projectInstance )
            if( projectId == null ) {

                flash.message = " No Projects are Associated, Please select Project. "
                redirect( controller:"project",action:"list" )

            }
            
        }   	
    	
    	if ( projectId != null ) {
    		def project = Project.findById(projectId)
    		assetsByProject = AssetEntity.findAllByProject(project)
    	}
    	def	dataTransferBatchs = DataTransferBatch.count()
        render( view:"importExport", model : [ assetsByProject: assetsByProject, projectId: projectId, moveBundleInstanceList: moveBundleInstanceList, dataTransferSetImport: dataTransferSetImport, dataTransferSetExport: dataTransferSetExport, dataTransferBatchs: dataTransferBatchs ] )
    }
    /*
     * render export form
     */
    def assetExport = {
        render( view:"assetExport" )
    }
    /*
     * upload excel file into Asset table
     */
    def upload = {
	        //get project Name
	        def projectId
	        def project
	        def dataTransferSet = params.dataTransferSet
	        def dataTransferSetInstance = DataTransferSet.findById( dataTransferSet )
	        def dataTransferAttributeMap = DataTransferAttributeMap.findAllByDataTransferSet( dataTransferSetInstance )
	       
	        try {
	            projectId = params["projectIdImport"]
	            if ( projectId == null || projectId == "" ) {

	                flash.message = "Project Name is required"
	                redirect( controller:"asset", action:"assetImport" )
	                
	            }

	            project = Project.findById( projectId )
	            
	            //delete previous records existed for Project
	        
	            //def assetDelete = Asset.executeUpdate("delete from Asset a where a.project = $project.id " )

	        }catch ( Exception ex ) {
	            
	            flash.message = " Project Name is required. "
	            redirect( controller:"asset", action:"assetImport" )
	        }

	        // get File
	        MultipartHttpServletRequest mpr = ( MultipartHttpServletRequest )request
	        CommonsMultipartFile file = ( CommonsMultipartFile ) mpr.getFile("file")

	        // create workbook
	        def workbook
	        def sheet
	        //def sheetNo = 1
	        //def map = [ "Server":null, "Type":null, "S/N":null, "AssetTag":null ]        
	        
	        def sheetColumnNames = [:]
	        def sheetNameMap = [:] 
	        def list = new ArrayList()
	        def dataTransferAttributeMapSheetName
	        //get column name and sheets
	        dataTransferAttributeMap.eachWithIndex { item, pos ->    		
	    		list.add( item.columnName )
	    		sheetNameMap.put( "sheetName", (item.sheetName).trim() )
	        }
	        try {
	            workbook = Workbook.getWorkbook( file.inputStream )
	            def sheetNames = workbook.getSheetNames()        
	            def flag = 0
	            def sheetNamesLength = sheetNames.length
	            for( int i=0;  i < sheetNamesLength; i++ ) {           	
	            	
	                if ( sheetNameMap.containsValue(sheetNames[i].trim()) ) {
	                    flag = 1
	                    sheet = workbook.getSheet( sheetNames[i] )
	                }
	            	
	      		}
	            
	            if( flag == 0 ) {
	            	
	            	flash.message = " Sheet not found, Please check it."
	                redirect( action:assetImport, params:[projectId:projectId] ) 
	                
	            } else {           
	           
	            
	                // TODO : All columns should be done using maps as this will get unwieldly to manage with 20+ columns.  Both the import and
	                // export should use the same map.

	                //check for column
	                def col = sheet.getColumns()
	                for ( int c = 0; c < col; c++ ) {
	                	def cellContent = sheet.getCell( c, 0 ).contents
	                	sheetColumnNames.put(cellContent, c)                	
	                }
	                def checkCol = checkHeader( list, sheetColumnNames )
	                // Statement to check Headers if header are not found it will return Error message

	                // TODO : map here too.
	                if ( checkCol == false ) {
	                	
	                	missingHeader = missingHeader.replaceFirst(",","")
	                    flash.message = " Column Headers : ${missingHeader} not found, Please check it."
	                    redirect( action:assetImport, params:[projectId:projectId] )

	                } else {
	            	
	                    //get user name.
	                    def subject = SecurityUtils.subject
	                    def principal = subject.principal
	                    def userLogin = UserLogin.findByUsername( principal )
	                    //Add Data to dataTransferBatch.
	                    def dataTransferBatch = new DataTransferBatch()
	                	dataTransferBatch.statusCode = "PENDING"
	                	dataTransferBatch.transferMode = "I"
	                	dataTransferBatch.dataTransferSet = dataTransferSetInstance
	                	dataTransferBatch.project = project
	                	dataTransferBatch.userLogin = userLogin 
	                    if(dataTransferBatch.save()){                       
	                        
	                        def dataTransferValue
	                        def eavAttributeInstance
	                        for( int cols = 0; cols < col; cols++ ) {
	                            def dataTransferAttributeMapInstance = DataTransferAttributeMap.findByColumnName(sheet.getCell( cols, 0 ).contents)
	                            if( dataTransferAttributeMapInstance != null ) {
	                                for ( int r = 1; r < sheet.rows; r++ ) {
	                                    dataTransferValue = new DataTransferValue()
	                                    eavAttributeInstance = dataTransferAttributeMapInstance.eavAttribute
	                                    dataTransferValue.importValue = sheet.getCell( cols, r ).contents
	                                    dataTransferValue.rowId = r
	                                    dataTransferValue.dataTransferBatch = dataTransferBatch
	                                    dataTransferValue.eavAttribute = eavAttributeInstance
	                                    //dataTransferValue.save()
	                                    if ( dataTransferValue.save() ) {                                    	
	                                        added++
	                                    } else {
	                                        skipped += ( r +1 )
	                                    }
	                                    
	                                }
	                            }
	                	
	                            /*// get fields
	                            def assetName = sheet.getCell( map["Server"], r ).contents
	                            def assetType = sheet.getCell( map["Type"], r ).contents
	                            def assetTypeObj = AssetType.findById(assetType)
	                            def serialNumber = sheet.getCell( map["S/N"], r ).contents
	                            def assetTag = sheet.getCell( map["AssetTag"], r ).contents


	                            // save data in to db and check for added rows and skipped
	                            def asset = new 	(

	                            project: project,
	                            assetType: assetTypeObj,
	                            assetName: assetName,
	                            assetTag: assetTag,
	                            serialNumber: serialNumber,
	                            deviceFunction: "")

	                            // TODO : This logic will ALWAY return true since the asset is created.  It should be testing asset.save() and not called above.
	                            if ( asset ) {
	                            asset.save()
	                            added++
	                            } else {
	                            skipped += ( r +1 )
	                            }*/
	                        }

	                    } // generate error message
	                    workbook.close()
	                    if (skipped.size() > 0) {
	                    	flash.message = " File Uploaded Successfully with ${added} Assets and Skipped are ${skipped}. "
	                    } else {
	                    	flash.message = " File Uploaded Successfully with ${added} Assets. "
	                    }
	                    redirect( action:assetImport, params:[projectId:projectId] )
	                }
	            }
	        }catch( Exception ex ) {
	            flash.message = grailsApplication.metadata[ 'app.file.format' ]
	            redirect( action:assetImport, params:[projectId:projectId] )
	        }
	    }
    /*
     * download data form Asset Entity table into Excel file
     */
    def export = {

	        //get project Id
	        def projectId = params[ "projectIdExport" ]
	        def dataTransferSet = params.dataTransferSet
	        def bundle = request.getParameterValues( "bundle" )
	        def bundleList = new StringBuffer()
	        def bundleSize = bundle.size()
	        for ( int i=0; i< bundleSize ; i++ ) {
	        	
	        	if( i != bundleSize - 1) {
	        		bundleList.append( bundle[i] + "," )
	        	} else {
	        		bundleList.append( bundle[i] )        		
	        	}
	        }        
	        def dataTransferSetInstance = DataTransferSet.findById( dataTransferSet )
	        def dataTransferAttributeMap = DataTransferAttributeMap.findAllByDataTransferSet( dataTransferSetInstance )
	        def project = Project.findById( projectId )
	        
	        if ( projectId == null || projectId == "" ) {
	            flash.message = " Project Name is required. "
	            redirect( action:assetImport, params:[projectId:projectId] )
	        }
	        
	        def asset
	        def moveBundleAssetInstance 
	        if( bundleSize == 1 && bundle[0] == "" ) {
	        	asset = AssetEntity.findAllByProject( project )
	        } else {
	        	moveBundleAssetInstance = MoveBundleAsset.findAll( "from MoveBundleAsset m where  m.moveBundle in ( $bundleList )" )
	        	if( moveBundleAssetInstance.size()>0 ) {
	        		asset = AssetEntity.findAll( "from AssetEntity where project = project and id in (:asset)", [asset:moveBundleAssetInstance.asset.id] )
	        	} else {
	        		asset = []
	        	}
	        }
	        
	        //get template Excel
	        def workbook
	        def book
	        try {
	        	// Statements to get context details
	        	def tempProtocol = request.getProtocol()
	        	def protocol = tempProtocol.substring(0,tempProtocol.indexOf("/"))
	        	def serverName = request.getServerName() 
	        	def serverPort = request.getServerPort()
	        	// construct application URL
	        	def appUrl = protocol + "://" + serverName + ":" + serverPort + "/" + grailsApplication.metadata['app.name']
	        	// get connection
	        	def filenametoSet = dataTransferSetInstance.templateFilename
	        	def templateFilePath = appUrl + filenametoSet
	        	def url = new URL( templateFilePath )
	        	HttpURLConnection con = url.openConnection() 
	            workbook = Workbook.getWorkbook( con.getInputStream() )
	            
	            //set MIME TYPE as Excel 
	            filenametoSet = filenametoSet.split("/")
	            response.setContentType( "application/vnd.ms-excel" )
	            response.setHeader( "Content-Disposition", "attachment; filename= ${filenametoSet[2]}" )

	            //create workbook and sheet
	            book = Workbook.createWorkbook( response.getOutputStream(), workbook )            
	            def sheet
	            
	            //check for column
	            def map = [:]
	        	def sheetColumnNames = [:]
	            def columnNameList = new ArrayList()
	            def sheetNameMap = [:]        
	            def dataTransferAttributeMapSheetName
	            //get columnNames in to map
	            dataTransferAttributeMap.eachWithIndex { item, pos ->
		    		map.put( item.columnName, null )
		    		columnNameList.add(item.columnName)
		    		sheetNameMap.put( "sheetName", (item.sheetName).trim() )
	            }
	            def sheetNames = book.getSheetNames()  
	            def flag = 0
	            def sheetNamesLength = sheetNames.length
	            for( int i=0;  i < sheetNamesLength; i++ ) {           	
	            	
	                if ( sheetNameMap.containsValue( sheetNames[i].trim()) ) {
	                    flag = 1
	                    sheet = book.getSheet( sheetNames[i] )
	                }
	            	
	      		}
	            if( flag == 0 ) {
	            	
	            	flash.message = " Sheet not found, Please check it."
	                redirect( action:assetImport, params:[projectId:projectId] ) 
	                
	            } else {
	                def col = sheet.getColumns()
	                for ( int c = 0; c < col; c++ ) {
	                	def cellContent = sheet.getCell( c, 0 ).contents
	                	sheetColumnNames.put(cellContent, c)
	                	if( map.containsKey( cellContent ) ) {
	                        map.put( cellContent, c )
	                    } 
	                }
	                //calling method to check for Header
	                def checkCol = checkHeader( columnNameList, sheetColumnNames )
	                
	                // Statement to check Headers if header are not found it will return Error message
	                if ( checkCol == false ) {
	                	
	                	missingHeader = missingHeader.replaceFirst(",","")
	                    flash.message = " Column Headers : ${missingHeader} not found, Please check it."
	                    redirect( action:assetImport, params:[projectId:projectId] )

	                } else {
	                    //update data from Asset Entity table to EXCEL
	                    def assetSize = asset.size()
	                    def columnNameListSize = columnNameList.size()
	                    for ( int r = 1; r <= assetSize; r++ ) {
	                    	//get Move Bundle
	                    	/*def assetEntityInstance = AssetEntity.findById( asset[r-1].id )
	                        def moveBundleName = MoveBundleAsset.findByAsset( assetEntityInstance )*/
//	                      Add assetId for walkthrough template only.
	                        if( sheetColumnNames.containsKey("asset Id") ) {
		                        def addAssetId = new Label( 0, r, String.valueOf(asset[r-1].id))
		                        sheet.addCell( addAssetId )
	                        }                        
	                        for ( int coll = 0; coll < columnNameListSize; coll++ ) {
	                            def addContentToSheet
	                            if( dataTransferAttributeMap.eavAttribute.attributeCode[coll].equals("moveBundle") ) {
	                                //Add moveBundle for move into sheet.
	                               /* if(moveBundleName == null){
	                                	addContentToSheet = new Label( map[columnNameList.get(coll)], r, "" )
	                                }else{
	                                	addContentToSheet = new Label( map[columnNameList.get(coll)], r, String.valueOf(moveBundleName.moveBundle) )
	                                }*/
	                            } else {                            	
	                                if ( asset[r-1].(dataTransferAttributeMap.eavAttribute.attributeCode[coll]) == null ) {
	                                    addContentToSheet = new Label( map[columnNameList.get(coll)], r, "" )
	                                } else {
	                                    addContentToSheet = new Label( map[columnNameList.get(coll)], r, String.valueOf(asset[r-1].(dataTransferAttributeMap.eavAttribute.attributeCode[coll])) )
	                                }
	                                sheet.addCell( addContentToSheet )
	                            }
	                            
	                        }
	                    }

	                    book.write()
	                    book.close()                    
	                    render( view: "importExport" )
	                }
	            }
	        } catch( Exception fileEx ) {

	            flash.message = "Excel template not found. "
	            redirect( action:assetImport, params:[projectId:projectId] )

	        }
	    }
	// check the sheet headers and return boolean value
    def checkHeader( def list, def sheetColumnNames  ){       
        def listSize = list.size()
        for ( int coll = 0; coll < listSize; coll++ ) {
            
            if( sheetColumnNames.containsKey( list[coll] ) ) {
            	//Nonthing to perform.
            } else {
                missingHeader = missingHeader + ", " + list[coll]
            }
        }
    	if( missingHeader == "" ) {
    		
    		return true

    	} else {

    		return false
    	}
    }

    // the delete, save and update actions only accept POST requests
    def allowedMethods = [delete:'POST', save:'POST', update:'POST']

    def list = {
	    if(!params.max) params.max = 15
        def projectId = params.projectId
        if(projectId == null || projectId == ""){
        	projectId = getSession().getAttribute( "CURR_PROJ" ).CURR_PROJ
        }
        def project = Project.findById( projectId )
        def assetEntityInstanceList = AssetEntity.findAllByProject( project, params ) 
        [ assetEntityInstanceList: assetEntityInstanceList, projectId: projectId ]
    }   

    def delete = {
        def assetEntityInstance = AssetEntity.get( params.id )
        def projectId = params.projectId
        if(assetEntityInstance) {
            assetEntityInstance.delete()
            flash.message = "AssetEntity ${assetEntityInstance.assetName} deleted"
            redirect(action:list, params:[projectId:projectId])
        }
        else {
            flash.message = "AssetEntity not found with id ${params.id}"
            redirect(action:list, params:[projectId:projectId])
        }
    }
    
    def save = {
        def assetEntityInstance = new AssetEntity(params)
        def projectId = params.projectId
        def projectInstance = Project.findById( projectId )
        assetEntityInstance.project = projectInstance
        assetEntityInstance.owner = projectInstance.client
        if(!assetEntityInstance.hasErrors() && assetEntityInstance.save()) {
            flash.message = "AssetEntity ${assetEntityInstance.assetName} created"
            redirect( action:list, params:[projectId: projectId] )
        }
        else {
        	flash.message = "AssetEntity ${assetEntityInstance.assetName} not created"
            redirect( action:list, params:[projectId: projectId] )
        }
    }
    
    //remote link for asset entity dialog.
    def editShow = {
        
        def items = []
        def assetEntityInstance = AssetEntity.get( params.id )
        
        /*if( assetEntityInstance.assetType != null ){
        	items = [id:assetEntityInstance.id, model: assetEntityInstance.model, sourceLocation: assetEntityInstance.sourceLocation, targetLocation: assetEntityInstance.targetLocation, sourceRack: assetEntityInstance.sourceRack, targetRack: assetEntityInstance.targetRack, sourceRackPosition: assetEntityInstance.sourceRackPosition, targetRackPosition: assetEntityInstance.targetRackPosition, usize: assetEntityInstance.usize, manufacturer: assetEntityInstance.manufacturer, fiberCabinet: assetEntityInstance.fiberCabinet, hbaPort: assetEntityInstance.hbaPort, hinfo: assetEntityInstance.hinfo, ipAddress: assetEntityInstance.ipAddress, kvmDevice: assetEntityInstance.kvmDevice, kvmPort: assetEntityInstance.kvmPort, newOrOld: assetEntityInstance.newOrOld, nicPort: assetEntityInstance.nicPort, powerPort: assetEntityInstance.powerPort, remoteMgmtPort: assetEntityInstance.remoteMgmtPort, truck: assetEntityInstance.truck, project:assetEntityInstance.project.name, projectId:assetEntityInstance.project.id, assetType:assetEntityInstance.assetType, assetTypeId:assetEntityInstance.assetType.id, assetTag:assetEntityInstance.assetTag, assetName:assetEntityInstance.assetName, serialNumber:assetEntityInstance.serialNumber, , application:assetEntityInstance.application ]
       
        } else {
        	items = [id:assetEntityInstance.id, model: assetEntityInstance.model, sourceLocation: assetEntityInstance.sourceLocation, targetLocation: assetEntityInstance.targetLocation, sourceRack: assetEntityInstance.sourceRack, targetRack: assetEntityInstance.targetRack, sourceRackPosition: assetEntityInstance.sourceRackPosition, targetRackPosition: assetEntityInstance.targetRackPosition, usize: assetEntityInstance.usize, manufacturer: assetEntityInstance.manufacturer, fiberCabinet: assetEntityInstance.fiberCabinet, hbaPort: assetEntityInstance.hbaPort, hinfo: assetEntityInstance.hinfo, ipAddress: assetEntityInstance.ipAddress, kvmDevice: assetEntityInstance.kvmDevice, kvmPort: assetEntityInstance.kvmPort, newOrOld: assetEntityInstance.newOrOld, nicPort: assetEntityInstance.nicPort, powerPort: assetEntityInstance.powerPort, remoteMgmtPort: assetEntityInstance.remoteMgmtPort, truck: assetEntityInstance.truck, project:assetEntityInstance.project.name, projectId:assetEntityInstance.project.id, assetTag:assetEntityInstance.assetTag, assetName:assetEntityInstance.assetName, serialNumber:assetEntityInstance.serialNumber, application:assetEntityInstance.application ]
        }*/
        def entityAttributeInstance =  EavEntityAttribute.findAll(" from com.tdssrc.eav.EavEntityAttribute eav where eav.eavAttributeSet = $assetEntityInstance.attributeSet.id order by eav.sortOrder ")
        
        entityAttributeInstance.each{
        
        	if( it.attribute.attributeCode != "moveBundle"){
        		items << [label:it.attribute.frontendLabel, attributeCode:it.attribute.attributeCode, value:assetEntityInstance.(it.attribute.attributeCode) ? assetEntityInstance.(it.attribute.attributeCode) : ""]
        	}
        }
        render items as JSON
        
    }
    
    //update ajax overlay
    def updateAssetEntity = {
    		 
    	def assetItems = []
    	def assetEntityParams = params.assetEntityParams.split(",")
    	def map = new HashMap()
    	assetEntityParams.each{
    		def assetParam = it.split(":")
    		if(assetParam.length > 1){
    			map.put(assetParam[0],assetParam[1] )
    		}
    	}
        def assetEntityInstance = AssetEntity.get( params.id )
        if(assetEntityInstance) {
        	assetEntityInstance.properties = map
            if(!assetEntityInstance.hasErrors() && assetEntityInstance.save()) {
            	def entityAttributeInstance =  EavEntityAttribute.findAll(" from com.tdssrc.eav.EavEntityAttribute eav where eav.eavAttributeSet = $assetEntityInstance.attributeSet.id order by eav.sortOrder ")
            	entityAttributeInstance.each{
                	if( it.attribute.attributeCode != "moveBundle"){
                		assetItems << [id:assetEntityInstance.id, attributeCode:it.attribute.attributeCode, value:assetEntityInstance.(it.attribute.attributeCode) ? assetEntityInstance.(it.attribute.attributeCode) : ""]
                	}
                }
            }
        }
        render assetItems as JSON

    }
    
    def getAttributes = {
    	def attributeSetId = params.attribSet
        def items = []
    	def entityAttributeInstance = []
    	
        if(attributeSetId != null &&  attributeSetId != ""){
    		def attributeSetInstance = EavAttributeSet.findById( attributeSetId )
    		//entityAttributeInstance =  EavEntityAttribute.findAllByEavAttributeSetOrderBySortOrder( attributeSetInstance )
    		entityAttributeInstance =  EavEntityAttribute.findAll(" from com.tdssrc.eav.EavEntityAttribute eav where eav.eavAttributeSet = $attributeSetId order by eav.sortOrder ")
        }
    	entityAttributeInstance.each{
    		items<<[ label:it.attribute.frontendLabel, attributeCode:it.attribute.attributeCode ]
    	}
    	render items as JSON
    }
    def getAssetAttributes = {
    	def assetId = params.assetId
        def items = []
    	def entityAttributeInstance = []
    	
        if(assetId != null &&  assetId != ""){
    		def assetEntity = AssetEntity.findById( assetId )
    		//entityAttributeInstance =  EavEntityAttribute.findAllByEavAttributeSetOrderBySortOrder( attributeSetInstance )
    		entityAttributeInstance =  EavEntityAttribute.findAll(" from com.tdssrc.eav.EavEntityAttribute eav where eav.eavAttributeSet = $assetEntity.attributeSet.id order by eav.sortOrder ")
        }
    	entityAttributeInstance.each{
    		items<<[ label:it.attribute.frontendLabel, attributeCode:it.attribute.attributeCode ]
    	}
    	render items as JSON
    }
}
