import java.io.*
import jxl.*
import jxl.write.*
import jxl.read.biff.*
import org.springframework.web.multipart.*
import org.springframework.web.multipart.commons.*
import grails.converters.JSON
import org.jsecurity.SecurityUtils
import com.tdssrc.eav.*
import org.codehaus.groovy.grails.commons.ApplicationHolder
class AssetEntityController {	
    //TODO : Fix indentation
	def missingHeader = ""
	def added = 0
	def skipped = []
	def partyRelationshipService
	def stateEngineService
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
                //check for column
                def col = sheet.getColumns()
                for ( int c = 0; c < col; c++ ) {
                    def cellContent = sheet.getCell( c, 0 ).contents
                    sheetColumnNames.put(cellContent, c)
                }
                def checkCol = checkHeader( list, sheetColumnNames )
                // Statement to check Headers if header are not found it will return Error message
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
                        def colNo = 0
                        for (int index = 0; index < col; index++) {
                        	if(sheet.getCell( index, 0 ).contents == "Server"){
                        		colNo = index
                        	}
                        }
                        for ( int r = 1; r < sheet.rows; r++ ) {
                        	def server = sheet.getCell( colNo, r ).contents
                        	if(server){
	                        	for( int cols = 0; cols < col; cols++ ) {
	                        		def dataTransferAttributeMapInstance = DataTransferAttributeMap.findByColumnName(sheet.getCell( cols, 0 ).contents)
	                            	if( dataTransferAttributeMapInstance != null ) {
	                                    dataTransferValue = new DataTransferValue()
	                                    eavAttributeInstance = dataTransferAttributeMapInstance.eavAttribute
	                                    dataTransferValue.importValue = sheet.getCell( cols, r ).contents
	                                    dataTransferValue.rowId = r
	                                    dataTransferValue.dataTransferBatch = dataTransferBatch
	                                    dataTransferValue.eavAttribute = eavAttributeInstance
	                                    if( sheetColumnNames.containsKey("assetId") && (sheet.getCell( 0, r ).contents != "") ) {
		                                    	
	                                        dataTransferValue.assetEntityId = Integer.parseInt(sheet.getCell( 0, r ).contents)
	                                    }
	                                    if ( dataTransferValue.save() ) {
	                                        added = r
	                                    } else {
	                                        skipped += ( r +1 )
	                                    }
                                	}
                                }
                            }
	                	
                        }
                        for( int i=0;  i < sheetNamesLength; i++ ) {
	    	        	    	
                            if(sheetNames[i] == "Comments"){
                                def commentSheet = workbook.getSheet(sheetNames[i])
                                for( int rowNo = 1; rowNo < commentSheet.rows; rowNo++ ) {
                                	def dataTransferComment
                                	def commentId = commentSheet.getCell(1,rowNo).contents
                                	def assetCommentId
                                	if( commentId != "" && commentId != null ) {
                                		assetCommentId = Integer.parseInt(commentId)
                                		dataTransferComment = DataTransferComment.findByCommentId(commentId)
                                	}
                                	if( dataTransferComment == null ) {
                                		dataTransferComment = new DataTransferComment()
                                	}
                                	dataTransferComment.commentId = assetCommentId
                                    dataTransferComment.assetId = Integer.parseInt(commentSheet.getCell(0,rowNo).contents)                                 
                                    dataTransferComment.commentType = commentSheet.getCell(3,rowNo).contents
                                    dataTransferComment.comment = commentSheet.getCell(4,rowNo).contents
                                    dataTransferComment.rowId = rowNo
                                    dataTransferComment.dataTransferBatch = dataTransferBatch
                                    dataTransferComment.save()
                                }
                            }
                        }
                    }

                } // generate error message
                workbook.close()
                if (skipped.size() > 0) {
                    flash.message = " File Uploaded Successfully with ${added} records. and  ${skipped} Records skipped Please click the Manage Batches to review and post these changes."
                } else {
                    flash.message = " File uploaded successfully with ${added} records.  Please click the Manage Batches to review and post these changes."
                }
                redirect( action:assetImport, params:[projectId:projectId] )
	              
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
        def assetEntityInstance
        if( bundleSize == 1 && bundle[0] == "" ) {
            asset = AssetEntity.findAllByProject( project )
        } else {
            asset = AssetEntity.findAll( "from AssetEntity m where m.project = project and m.moveBundle in ( $bundleList )" )
        }
	        
        //get template Excel
        def workbook
        def book
        try {
            // Statements to get context details
            /*def tempProtocol = request.getProtocol()
            def protocol = tempProtocol.substring(0,tempProtocol.indexOf("/"))
            def serverName = request.getServerName()
            def serverPort = request.getServerPort()
            def contextPath = request.getContextPath()
            // construct application URL
            def appUrl = protocol + "://" + serverName + ":" + serverPort + contextPath
            // get connection
            def filenametoSet = dataTransferSetInstance.templateFilename
            def templateFilePath = appUrl + filenametoSet
            def url = new URL( templateFilePath )
            HttpURLConnection con = url.openConnection()*/
            
            def filenametoSet = dataTransferSetInstance.templateFilename
            File file =  ApplicationHolder.application.parentContext.getResource(filenametoSet).getFile()
            // Going to use temporary file because we were getting out of memory errors constantly on staging server
            WorkbookSettings wbSetting = new WorkbookSettings()
            wbSetting.setUseTemporaryFileDuringWrite(true)
            workbook = Workbook.getWorkbook( file, wbSetting )
	            
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
                        //Add assetId for walkthrough template only.
                        if( sheetColumnNames.containsKey("assetId") ) {
                            def integerFormat = new WritableCellFormat (NumberFormats.INTEGER)
                            def addAssetId = new Number(0, r, (asset[r-1].id))
                            sheet.addCell( addAssetId )
                        }
                        for ( int coll = 0; coll < columnNameListSize; coll++ ) {
                            def addContentToSheet
	                                                        	
                            if ( asset[r-1].(dataTransferAttributeMap.eavAttribute.attributeCode[coll]) == null ) {
                                addContentToSheet = new Label( map[columnNameList.get(coll)], r, "" )
                            } else {
                                addContentToSheet = new Label( map[columnNameList.get(coll)], r, String.valueOf(asset[r-1].(dataTransferAttributeMap.eavAttribute.attributeCode[coll])) )
                            }
                            sheet.addCell( addContentToSheet )
	                            
	                            
                        }
                    } 
                    
                    //update data from Asset Comment table to EXCEL
                    
                    for( int sl=0;  sl < sheetNamesLength; sl++ ) {
                    	def commentIt = new ArrayList()
                        if(sheetNames[sl] == "Comments"){
                        	def commentSheet = book.getSheet("Comments") 
                        	asset.each{
                        		commentIt.add(it.id)                        		
                            }
                        	def commentList = new StringBuffer()
                            def commentSize = commentIt.size()
                            for ( int k=0; k< commentSize ; k++ ) {
	                     	    if( k != commentSize - 1) {
                                    commentList.append( commentIt[k] + "," )
	                            } else {
                                    commentList.append( commentIt[k] )
	                            }
                            }
                            def assetcomment = AssetComment.findAll("from AssetComment cmt where cmt.assetEntity in ($commentList)")
                            def assetId
                            def commentType
                            def comment
                            def commentId
                            def assetName
                            for(int cr=1 ; cr<=assetcomment.size() ; cr++){
                            	assetId = new Label(0,cr,String.valueOf(assetcomment[cr-1].assetEntity.id))
                                commentSheet.addCell(assetId)
                                commentId = new Label(1,cr,String.valueOf(assetcomment[cr-1].id))
                                commentSheet.addCell(commentId)
                                assetName = new Label(2,cr,String.valueOf(assetcomment[cr-1].assetEntity.assetName))
                                commentSheet.addCell(assetName)
                                commentType = new Label(3,cr,String.valueOf(assetcomment[cr-1].commentType))
                                commentSheet.addCell(commentType)
                                comment = new Label(4,cr,String.valueOf(assetcomment[cr-1].comment))
                                commentSheet.addCell(comment)
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
        	def attributeOptions = EavAttributeOption.findAllByAttribute( it.attribute )
    		def options = []
    		attributeOptions.each{option ->
    			options<<[option:option.value]
    		}
        	if( it.attribute.attributeCode != "moveBundle"){
        		items << [label:it.attribute.frontendLabel, attributeCode:it.attribute.attributeCode, frontendInput:it.attribute.frontendInput, options : options, value:assetEntityInstance.(it.attribute.attributeCode) ? assetEntityInstance.(it.attribute.attributeCode) : ""]
        	}
        }
        render items as JSON
        
    }
    
    //update ajax overlay
    def updateAssetEntity = {
    		 
    	def assetItems = []
    	def assetEntityParams = params.assetEntityParams
    	if(assetEntityParams){
	    	def assetEntityParamsList = assetEntityParams.split(",")
	    	def map = new HashMap()
	    	assetEntityParamsList.each{
	    		def assetParam = it.split(":")
	    		if(assetParam.length > 1){
	    			map.put(assetParam[0],assetParam[1] )
	    		} else {
	    			map.put(assetParam[0],"" )
	    		}
	    	}
	        def assetEntityInstance = AssetEntity.get( params.id )
	        if(assetEntityInstance) {
	        	assetEntityInstance.properties = map
	            if(!assetEntityInstance.hasErrors() && assetEntityInstance.save()) {
	            	
	            	def entityAttributeInstance =  EavEntityAttribute.findAll(" from com.tdssrc.eav.EavEntityAttribute eav where eav.eavAttributeSet = $assetEntityInstance.attributeSet.id order by eav.sortOrder ")
	            	entityAttributeInstance.each{
	            		
	                	if( it.attribute.attributeCode != "moveBundle"){
	                		assetItems << [id:assetEntityInstance.id, attributeCode:it.attribute.attributeCode, frontendInput:it.attribute.frontendInput, value:assetEntityInstance.(it.attribute.attributeCode) ? assetEntityInstance.(it.attribute.attributeCode) : ""]
	                	}
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
    		def attributeOptions = EavAttributeOption.findAllByAttribute( it.attribute )
    		def options = []
    		attributeOptions.each{option ->
    			options<<[option:option.value]
    		}
    		if( it.attribute.attributeCode != "moveBundle"){
    			items<<[ label:it.attribute.frontendLabel, attributeCode:it.attribute.attributeCode, frontendInput:it.attribute.frontendInput, options : options ]
    		}
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
    		if( it.attribute.attributeCode != "moveBundle"){
    			items<<[ attributeCode:it.attribute.attributeCode, frontendInput:it.attribute.frontendInput ]
    		}
    	}
    	render items as JSON
    }
    /*
     *   will return data for auto complete fields
     */
    def getAutoCompleteDate = {
    	def autoCompAttribs = params.autoCompParams
    	def data = []
    	if(autoCompAttribs){
    		def autoCompAttribsList = autoCompAttribs.split(",")
    		def currProj = getSession().getAttribute( "CURR_PROJ" )
            def projectId = currProj.CURR_PROJ
    		def project = Project.findById( projectId )
    		autoCompAttribsList.each{
    			def assetEntity = AssetEntity.executeQuery( "select distinct $it from AssetEntity where owner = $project.client.id" ) 
    			data<<[value:assetEntity , attributeCode : it]
	    	}
    	}
    	render data as JSON
    }
    /* 
     * get comments for selected asset entity
     */
    def listComments = {
        def assetEntityInstance = AssetEntity.get( params.id )
        def assetCommentsInstance = AssetComment.findAllByAssetEntity( assetEntityInstance )
        def assetCommentsList = []
    	assetCommentsInstance.each {
            assetCommentsList <<[ commentInstance : it, assetEntityId : it.assetEntity.id ]
        }
        render assetCommentsList as JSON
    }
    /*
     *  To save the Comment record 
     */
    def saveComment = {
    	def assetComments = []
    	def assetCommentInstance = new AssetComment(params)
    	if(!assetCommentInstance.hasErrors() && assetCommentInstance.save()) {
    		render assetCommentInstance as JSON
        } else {
        	render assetComments as JSON
        }
    }
    /*
     *  return the commet record
     */
    def showComment = {
        def assetComment = AssetComment.get(params.id)
        render assetComment as JSON
    }
    /*
     *  update comments
     */
    def updateComment = {
    	def assetCommentInstance = AssetComment.get(params.id)
    	assetCommentInstance.properties = params
    	if(!assetCommentInstance.hasErrors()) {
    		assetCommentInstance.save()
        }
    	render assetCommentInstance as JSON
    }
    /*
     * 	 delete the comment record 
     */
    def deleteComment = {
        def assetCommentInstance = AssetComment.get(params.id)
        if(assetCommentInstance){
            assetCommentInstance.delete()
        }
        def assetEntityInstance = AssetEntity.get( params.assetEntity )
        def assetCommentsInstance = AssetComment.findAllByAssetEntityAndIdNotEqual( assetEntityInstance, params.id )
        def assetCommentsList = []
        assetCommentsInstance.each {
            assetCommentsList <<[ commentInstance : it, assetEntityId : it.assetEntity.id ]
        }
        render assetCommentsList as JSON
    }
    /*
     * 	 To show the dashboard link page and processing
     */
    def dashboardView = {
        def projectId = params.projectId
        def bundleId = params.moveBundle
        def assetList
        def bundleTeams = []
        def totalAsset
        def completed = new HashMap()
        def projectInstance = Project.findById( projectId )
        def moveBundleInstanceList = MoveBundle.findAll("from MoveBundle mb where mb.project = ${projectInstance.id} order by mb.name asc")
        def moveBundleInstance
        if(bundleId){
            moveBundleInstance = MoveBundle.findById(bundleId)
        } else {
            moveBundleInstance = MoveBundle.findByProject(projectInstance)
        }
        def orderDesc = params.order;
        if(params.sort == "team"){
            totalAsset = AssetEntity.findAll("from AssetEntity ae where ae.moveBundle = ${moveBundleInstance.id} order by ae.sourceTeam.name $orderDesc ")
        }else if(params.sort == "statTimer"){
            totalAsset = AssetEntity.findAll("from AssetEntity ae where ae.moveBundle = ${moveBundleInstance.id} order by ae.moveBundle.startTime $orderDesc ")	
        }else if(params.sort == "loc"){
            totalAsset = AssetEntity.findAll("from AssetEntity ae where ae.moveBundle = ${moveBundleInstance.id} order by ae.sourceTeam.currentLocation $orderDesc ")	
        }else{
            totalAsset = AssetEntity.findAllByMoveBundle(moveBundleInstance,params)
        }
        def projectTeamList = ProjectTeam.findAll("from ProjectTeam pt where pt.moveBundle = ${moveBundleInstance.id} order by pt.name asc")
        def arrAsset
        projectTeamList.each{
            def teamMembers = partyRelationshipService.getBundleTeamMembersDashboard(it.id)
            def member = teamMembers.delete((teamMembers.length()-1),teamMembers.length())
            bundleTeams <<[team:it,members:member]
        }
        def cleanedId = stateEngineService.getStateId("STD_PROCESS","Cleaned")
        def rerackedId = stateEngineService.getStateId("STD_PROCESS","Reracked")
        def onTruckId = stateEngineService.getStateId("STD_PROCESS","OnTruck")
        def stagedId = stateEngineService.getStateId("STD_PROCESS","Staged")
        
        def sourceCleaned = ProjectAssetMap.findAll("from ProjectAssetMap where currentStateId >= $cleanedId and asset in (select id from AssetEntity  where moveBundle = ${moveBundleInstance.id} )" ).size()
        def targetCleaned = ProjectAssetMap.findAll("from ProjectAssetMap where currentStateId >= $rerackedId and asset in (select id from AssetEntity  where moveBundle = ${moveBundleInstance.id} )" ).size()
        def sourceMover = ProjectAssetMap.findAll("from ProjectAssetMap where currentStateId >= $onTruckId and asset in (select id from AssetEntity  where moveBundle = ${moveBundleInstance.id} )" ).size()
        def targetMover = ProjectAssetMap.findAll("from ProjectAssetMap where currentStateId >= $stagedId and asset in (select id from AssetEntity  where moveBundle = ${moveBundleInstance.id} )" ).size()
        completed.put("totalAssets", totalAsset.size() )
        completed.put("sourceCleaned", sourceCleaned )
        completed.put("targetCleaned", targetCleaned )
        completed.put("sourceMover", sourceMover )
        completed.put("targetMover", targetMover )
        return[ moveBundleInstanceList: moveBundleInstanceList, projectId:projectId, bundleTeams:bundleTeams, totalAsset:totalAsset,moveBundleInstance:moveBundleInstance, completed:completed]
        		
    }
    /*
     * 	 Get asset details part in dashboard page
     */
    def assetDetails = {
        def assetId = params.assetId
        def assetTeam = []
        def assetDetail = AssetEntity.findById(assetId)
        def teamName = assetDetail.sourceTeam.name
        def map = new HashMap()
        map.put("assetDetail",assetDetail)
        map.put("teamName",teamName)
        assetTeam<<map
        render assetTeam as JSON
        		
    }
}
